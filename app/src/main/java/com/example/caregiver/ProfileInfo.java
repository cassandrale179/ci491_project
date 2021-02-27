package com.example.caregiver;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.Html;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        if (role.equals("caregiver")){
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
        confirmPasswordField = (EditText) view.findViewById(R.id.profileNewPassword2);
        caregiveeLabel = (TextView) view.findViewById(R.id.profileTextLabel);
        errorMessage = (TextView) view.findViewById(R.id.profileInfoMessage);
        red = view.getResources().getColor(R.color.red);
        green = view.getResources().getColor(R.color.green);

        // This page is opened when user clicked on "View Profile" from the homepage.
        Bundle args = this.getArguments();
        if (args != null){
            String otherName = args.getString("otherName");
            String otherNotes = args.getString("otherNotes");
            String otherEmail = args.getString("otherEmail");
            if (otherNotes == null){
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
        return view;
    }

    /**
     * Render the error and success message field.
     * @param sourceString The text message to be displayed.
     * @param color The color for the text message (red for error, green for success).
     */
    public void displayMessage(String sourceString, int color) {
        errorMessage.setText(Html.fromHtml(sourceString));
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setTextColor(color);
    }

    /**
     * Handle user password update.
     * @param user The current user who is logged in the app
     * @param newPassword User new password
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
        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener < Void > () {
            @Override
            public void onComplete(@NonNull Task < Void > task) {
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
     * @param user The current user who is logged in the app
     * @param email User new email
     */
    public void changeEmail(FirebaseUser user, @NonNull String email){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        user.updateEmail(email).addOnCompleteListener(new OnCompleteListener < Void > () {
            @Override
            public void onComplete(@NonNull Task < Void > task) {
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
                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener < Void > () {
                    @Override
                    public void onComplete(@NonNull Task < Void > task) {
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

        if (!notes.isEmpty()){
            rootRef.child("users").child(user.getUid()).child("notes").setValue(notes);
            editor.putString("userNotes", notes);
        }
        editor.commit();
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
            Intent i = new Intent(v.getContext(), Login.class);
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