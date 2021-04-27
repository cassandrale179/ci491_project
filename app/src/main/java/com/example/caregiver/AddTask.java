package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caregiver.model.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AddTask extends AppCompatActivity {

    // This stores the dropdown menu for caregivee names
    List<String> caregivee_spinner_options = new ArrayList<>();

    // This stores the corresponding caregivee id
    List<String> caregivee_spinner_ids = new ArrayList<>();

    // Key is the caregivee id, value is a list of rooms in that caregivee's house
    HashMap<String, List<String>> caregiveeRooms = new HashMap<>();

    // Set up global variables
    private Spinner caregiveeSpinner;
    private Spinner roomSpinner;
    public String selectedCaregiveeId;
    private EditText taskNameField;
    private EditText taskNotesField;
    private String caregiverId;
    private TextView errorMessage;
    AlertDialog.Builder builder;
    private ImageView imageView;

    private Uri filePath;
    String currentPhotoPath;

    //firebase storage reference
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 234;

    private static final int MULTIPLE_REQUEST = 123;
    private static final int CAPTURED_IMAGE_REQUEST = 1024;
    private String uploadingFolderFilename;
    private String uploadingFilename;

    /**
     * Render the error and success message field.
     *
     * @param sourceString The text message to be displayed.
     */
    public void displaySuccessMessage(String sourceString) {
        int green = ContextCompat.getColor(getApplicationContext(), R.color.green);
        errorMessage.setText(Html.fromHtml(sourceString));
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setTextColor(green);
    }

    public void displayErrorMessage(String sourceString) {
        int red = ContextCompat.getColor(getApplicationContext(), R.color.red);
        errorMessage.setText(Html.fromHtml(sourceString));
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setTextColor(red);
    }

    public void displayUserInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        caregiverId =  preferences.getString("userId", "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermissions(new String[] {Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, MULTIPLE_REQUEST);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize spinners
        caregiveeSpinner = (Spinner) findViewById(R.id.taskCaregivee);
        roomSpinner = (Spinner) findViewById(R.id.taskRoom);

        // Initialize fields
        taskNameField = (EditText) findViewById(R.id.taskName);
        taskNotesField = (EditText) findViewById(R.id.taskNotes);
        errorMessage = (TextView) findViewById(R.id.taskMessage);
        imageView = (ImageView) findViewById(R.id.addTaskLogo);
        storageReference = FirebaseStorage.getInstance().getReference();

        // Handling create spinner options
        displayUserInfo();
        queryLatestRooms();

        // navigate back to dashboard
        Button backButton = findViewById(R.id.taskCancelButton);
        backButton.setOnClickListener(view -> startActivity(new Intent(view.getContext(), Dashboard.class)));

        //navigate to upload media
        TextView uploadMedia = findViewById(R.id.UploadMediaTextView);




        builder = new AlertDialog.Builder(this);

        uploadMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Permission-123", grantResults.toString());
        //Log.d("Permission-12345", PackageManager.PERMISSION_GRANTED);

        switch(requestCode){
            case MULTIPLE_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }

                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Read - Files permission granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Read - Files permission denied", Toast.LENGTH_SHORT).show();
                }

                if (grantResults.length > 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Write - Files permission granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Write - Files permission denied", Toast.LENGTH_SHORT).show();
                }
        }

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
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) { //check if
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURED_IMAGE_REQUEST);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST   && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
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
    private void uploadFile(String taskuniqueID) {
        if (filePath != null) {
            //displaying a progress dialog while upload is going on
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading");
            progressDialog.show();

            uploadingFolderFilename = caregiverId;

            uploadingFilename = uploadingFolderFilename+("/")+taskuniqueID;

            StorageReference riversRef = storageReference.child(uploadingFilename);
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                        }
                    });
        }
        else {
            Toast.makeText(getApplicationContext(), "File Not Uploaded ", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Function to query the latest room under a caregivee id.
     */
    protected void queryLatestRooms(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String caregiveeNames = preferences.getString("caregiveeInfo", null);
        caregiverId = preferences.getString("userId", "");

        Gson gson = new Gson();
        HashMap<String, String> caregiveeInfo = gson.fromJson(caregiveeNames, HashMap.class);

        // This implement the asynchronous call method to get Tasks using Customized Call back
        // See: https://stackoverflow.com/questions/51402623/how-to-wait-for-an-asynchronous-method
        caregiveeInfo.forEach((id, name) -> {
            caregivee_spinner_options.add(name);
            caregivee_spinner_ids.add(id);
            Task taskModelObject = new Task();
            taskModelObject.getAllRooms(id, new App.RoomCallback() {
                @Override
                public void onDataReceived(List<String> rooms) {
                  if (rooms != null){
                      rooms.add("none");
                      caregiveeRooms.put(id, rooms);
                  } else {
                      caregiveeRooms.put(id, Arrays.asList("none"));
                  }

                  // Create spinner
                  if (caregiveeRooms.size() == caregiveeInfo.size()){
                      createSpinners();
                  }
                }
            });
        });
    }

    protected void createSpinners() {
        // Render list on the caregivee spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, caregivee_spinner_options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        caregiveeSpinner.setAdapter(adapter);

        // When user click on selected spinner, we want to get the caregivee id and their rooms.
        caregiveeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedCaregiveeId = caregivee_spinner_ids.get(pos);
                if (caregiveeRooms.containsKey(selectedCaregiveeId)){
                    List<String> rooms = caregiveeRooms.get(selectedCaregiveeId);
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (
                            AddTask.this, android.R.layout.simple_spinner_item, rooms);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    roomSpinner.setAdapter(adapter2);
                }
//                else {
//                    List<String> rooms = Arrays.asList("livingroom", "bedroom", "bathroom", "kitchen");
//                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (
//                            AddTask.this, android.R.layout.simple_spinner_item, rooms);
//                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    roomSpinner.setAdapter(adapter2);
//                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Uploads the newly created task to Firebase
     *
     * @param view The view of the Add Task page
     */
    public void CreateTask(View view) {

        if (taskNameField.getText().toString().isEmpty() ||
                roomSpinner.getSelectedItem() == null ||
                selectedCaregiveeId == null) {
            displayErrorMessage("Some fields are missing.");
            return;
        }

        String taskName = taskNameField.getText().toString();
        String taskNotes = "N/A";
        String room = roomSpinner.getSelectedItem().toString();
        String uniqueID = UUID.randomUUID().toString();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference taskRef = database
                .child("users")
                .child(selectedCaregiveeId)
                .child("rooms")
                .child(room)
                .child("tasks");
        if (taskNotesField.getText() != null) {
            taskNotes = taskNotesField.getText().toString();
        }

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put(uniqueID+"/name", taskName);
        userUpdates.put(uniqueID+"/notes", taskNotes);
        userUpdates.put(uniqueID+"/caregiverID", caregiverId);
        userUpdates.put(uniqueID+"/assignedStatus", true);
        userUpdates.put(uniqueID+"/completionStatus", "incomplete");

        taskRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    displayErrorMessage(databaseError.getMessage());
                } else {
                    displaySuccessMessage("Data saved successfully.");
                    if (view.getContext() != null) {
                        startActivity(new Intent(view.getContext(), Dashboard.class));
                    }
                }
            }
        });
        uploadFile(uniqueID);
    }
}
