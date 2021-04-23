package com.example.caregiver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileInfo extends Fragment {

    SharedPreferences preferences;
    View view;

    // Variables pointing to the field names
    public EditText nameField;
    public EditText emailField;
    public EditText newPasswordField;
    public EditText confirmPasswordField;
    public TextView errorMessage;
    public TextView notesField;
    public TextView caregiveeLabel;

    // Variables pointing to the user
    public String currentEmail;
    public String currentName;
    public String currentNotes;

    // Color for error and success message
    int red;
    int green;

    private AlertDialog.Builder builder;
    private static final int PICK_IMAGE_REQUEST = 234;
    private static final int CAPTURED_IMAGE_REQUEST = 1024;
    String currentPhotoPath;
    private StorageReference storageReference;
    private StorageReference storageReference1;
    private Uri filePath;
    private ImageView imageView;
    String Id;
    String caregiveeId;

    public class ProfileUser {
        public String name;
        public String email;

        public ProfileUser(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

    public ProfileInfo() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ProfileInfo newInstance() {
        ProfileInfo fragment = new ProfileInfo();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * This function populate the text fields on the profile info page
     */
    public void displayUserInfo() {
        currentName = preferences.getString("userName", "Name");
        nameField.setHint(currentName);

        currentEmail = preferences.getString("userEmail", "Email");
        emailField.setHint(currentEmail);

        currentNotes = preferences.getString("userNotes", "Notes about medication.");
        notesField.setHint(currentNotes);

        String role = preferences.getString("userRole", "");

        Id =  preferences.getString("userId", "");

        if (role.equals("caregiver")) {
            notesField.setVisibility(view.GONE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        view = inflater.inflate(R.layout.fragment_profile_info, container, false);

        // Get button id, text fields id and set listeners
        Button updateButton = (Button) view.findViewById(R.id.profileUpdateButton);
        Button logoutButton = (Button) view.findViewById(R.id.logOutButton);
        Button backButton = (Button) view.findViewById(R.id.backButton);
        updateButton.setOnClickListener(updateUserInfoListener);
        logoutButton.setOnClickListener(logOutListener);
        nameField = (EditText) view.findViewById(R.id.profileName);
        emailField = (EditText) view.findViewById(R.id.profileEmail);
        notesField = (EditText) view.findViewById(R.id.profileNotes);
        newPasswordField = (EditText) view.findViewById(R.id.profileNewPassword);
        confirmPasswordField = (EditText) view.findViewById(R.id.profileConfirmPassword);
        caregiveeLabel = (TextView) view.findViewById(R.id.profileTextLabel);
        errorMessage = (TextView) view.findViewById(R.id.profileInfoMessage);
        red = view.getResources().getColor(R.color.red);
        green = view.getResources().getColor(R.color.green);



        // This page is opened when user clicked on "View Profile" from the homepage.
        Bundle args = this.getArguments();
        if (args != null) {
            String otherName = args.getString("otherName");
            String otherNotes = args.getString("otherNotes");
            String otherEmail = args.getString("otherEmail");
            if (otherNotes == null) {
                otherNotes = "Notes for medications...";
            }
            nameField.setHint(otherName);
            emailField.setHint(otherEmail);
            notesField.setHint(otherNotes);

            // Hide buttons and password field
            updateButton.setVisibility(view.GONE);
            logoutButton.setVisibility(view.GONE);
            backButton.setVisibility(view.VISIBLE);
            backButton.setOnClickListener(backtoHomePage);
            confirmPasswordField.setVisibility(view.GONE);
            newPasswordField.setVisibility(view.GONE);

            // Set title and subtitle on profil einfo page
            TextView caregiveeTitle = (TextView) view.findViewById(R.id.profileTitle);
            caregiveeTitle.setText(otherName);
            caregiveeTitle.setVisibility(view.VISIBLE);

            caregiveeLabel.setText("View your caregivee profile below.");
        }

        // Called this when user open page from the navigation bar
        else {
            caregiveeLabel.setText("View or edit your profile below.");
            displayUserInfo();
        }

        //Initialized the storage reference
        storageReference1 = FirebaseStorage.getInstance().getReference()
                .child(Id).child("ProfilePicture");
        //Populate the imageview with the associated image
        storageReference1.getBytes(1024*1024*5)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        imageView.setImageBitmap(bitmap);
                    }
                });

        //Initialized the storage reference
        storageReference = FirebaseStorage.getInstance().getReference();

        //navigate to Profile Picture
        TextView profileImageTextView = view.findViewById(R.id.ProfilePicTextView);

        imageView = (ImageView) view.findViewById(R.id.profileImage);

        builder = new AlertDialog.Builder(getActivity());

        profileImageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // add a list
                String[] options = {"Gallery", "Click"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) { //Gallery
                            showFileChooser();
                        } else if (which == 1) {//Click
                            dispatchTakePictureIntent();
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.setTitle("Upload Image from Gallery or Click an Image");
                alert.show();
            }
        });

        return view;
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an Image"), PICK_IMAGE_REQUEST);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) { //check if
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURED_IMAGE_REQUEST);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURED_IMAGE_REQUEST) {
            File f = new File(currentPhotoPath);
            imageView.setImageURI(Uri.fromFile(f));
            filePath = Uri.fromFile(f);

        }
    }

    //this method will upload the file
    private void uploadFile() {
        //if there is a file to upload
        Log.e("Filepath",filePath.toString());
        if (filePath != null) {
            storageReference = FirebaseStorage.getInstance().getReference();
            //displaying a progress dialog while upload is going on
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            String uploadingFolderFilename = Id;

            String uploadingFilename = uploadingFolderFilename+("/")+"ProfilePicture";

            StorageReference riversRef = storageReference.child(uploadingFilename);
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //if the upload is successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();

                            //and displaying a success toast
                            Toast.makeText(getActivity().getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //if the upload is not successfull
                            //hiding the progress dialog
                            progressDialog.dismiss();
                            //and displaying error message
                            Toast.makeText(getActivity().getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            //calculating progress percentage
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            //displaying percentage in progress dialog
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        //if there is not any file
        else {
            Toast.makeText(getActivity().getApplicationContext(), "File Not Uploaded ", Toast.LENGTH_LONG).show();
            //you can display an error toast
        }
    }

    /**
     * Render the error and success message field.
     *
     * @param sourceString The text message to be displayed.
     * @param color        The color for the text message (red for error, green for success).
     */
    public void displayMessage(String sourceString, int color) {
        errorMessage.setText(Html.fromHtml(sourceString));
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setTextColor(color);
    }

    /**
     * Handle user password update.
     *
     * @param user            The current user who is logged in the app
     * @param newPassword     User new password
     * @param confirmPassword User new password (should be same as newPassword)
     */
    public void changePassword(
            FirebaseUser user, @NonNull String newPassword, @NonNull String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            displayMessage("Password do not match.", red);
            return;
        }
        if (newPassword.length() < 6 || confirmPassword.length() < 6) {
            displayMessage("Your password must be longer than 6 characters", red);
            return;
        }
        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    displayMessage("Successfully change your password", green);
                } else {
                    displayMessage("Cannot update password.", red);
                }
            }
        });
    }

    /**
     * Handle user email update.
     *
     * @param user  The current user who is logged in the app
     * @param email User new email
     */
    public void changeEmail(FirebaseUser user, @NonNull String email) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        user.updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    displayMessage("Successfully change your email", green);
                    rootRef.child("users").child(user.getUid()).child("email").setValue(email);
                    currentEmail = email;
                    emailField.setHint(currentEmail);
                } else {
                    displayMessage("Cannot update email.", red);
                }
            }
        });
    }

    /**
     * User must be logged in and has enter their password before they can modify profile.
     */
    public void askForOldPassword() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setMessage("Please input your current password below.");
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alert.setView(input);
        alert.setPositiveButton("Change Profile", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String password = input.getText().toString();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            updateUserInformation(user);
                            displayMessage("Your profile is updated!", green);
                            displayUserInfo();
                        } else {
                            displayMessage("Your old password is not correct.", red);
                        }
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alert.show();
    }

    /**
     * Updates user information after user has verified their old password.
     *
     * @param user The current logged in Firebase user
     */
    public void updateUserInformation(@NonNull FirebaseUser user) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        SharedPreferences.Editor editor = preferences.edit();

        String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        String newPassword = newPasswordField.getText().toString();
        String confirmPassword = confirmPasswordField.getText().toString();
        String notes = notesField.getText().toString();

        if (name != null && !name.isEmpty()) {
            rootRef.child("users").child(user.getUid()).child("name").setValue(name);
            editor.putString("userName", name);
        }
        if (!email.isEmpty()) {
            changeEmail(user, email);
            editor.putString("userEmail", email);
        }
        if (!newPassword.isEmpty() && !confirmPassword.isEmpty()) {
            changePassword(user, newPassword, confirmPassword);
        }

        if (!notes.isEmpty()) {
            rootRef.child("users").child(user.getUid()).child("notes").setValue(notes);
            editor.putString("userNotes", notes);
        }
        editor.commit();
        uploadFile();
    }

    /**
     * Function to update user information. It is called when clicked on Update button.
     */
    private View.OnClickListener updateUserInfoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            askForOldPassword();
        }
    };

    /**
     * Function to logout
     */
    private View.OnClickListener logOutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.commit();
            Intent i = new Intent(v.getContext(), MainActivity.class);
            startActivity(i);
            getActivity().finish();
        }
    };

    /*
     * Function to go back. It is called when clicked on the Back button.
     */
    private View.OnClickListener backtoHomePage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(v.getContext(), Dashboard.class);
            startActivity(i);
        }
    };
}