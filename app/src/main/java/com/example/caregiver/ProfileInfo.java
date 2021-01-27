package com.example.caregiver;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileInfo extends Fragment{

    // Variables pointing to the field names
    public EditText nameField;
    public EditText emailField;
    public EditText oldPasswordField;
    public EditText newPasswordField;
    public EditText confirmPasswordField;
    public TextView errorMessage;

    // Variables pointing to the user
    public String currentEmail;
    public String currentName;

    // Color for error and success message
    int red;
    int green;

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
     * @param view the view of the profile info page
     * @param userId the user id of the currently logged in user
     */
    public void displayUserInfo(View view, String userId){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("users/" + userId);

        // Attach a listener to read data of user (name, email, id)
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentName = dataSnapshot.child("name").getValue().toString();
                currentEmail = dataSnapshot.child("email").getValue().toString();
                EditText nameField = (EditText) view.findViewById(R.id.profileName);
                EditText emailField = (EditText) view.findViewById(R.id.profileEmail);
                nameField.setHint(currentName);
                emailField.setHint(currentEmail);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("failure", "Unable to obtain user information");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get current userId
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = preferences.getString("userId", "");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_info, container, false);
        displayUserInfo(view, userId);

        // Get button id, text fields id and set listeners
        Button updateButton = (Button) view.findViewById(R.id.profileUpdateButton);
        updateButton.setOnClickListener(updateUserInfoListener);
        nameField = (EditText) view.findViewById(R.id.profileName);
        emailField = (EditText) view.findViewById(R.id.profileEmail);
        oldPasswordField = (EditText) view.findViewById(R.id.profileOldPassword);
        newPasswordField = (EditText) view.findViewById(R.id.profileNewPassword);
        confirmPasswordField = (EditText) view.findViewById(R.id.profileNewPassword2);
        errorMessage =  (TextView) view.findViewById(R.id.profileInfoMessage);

        // Set color
        red = view.getResources().getColor(R.color.red);
        green = view.getResources().getColor(R.color.green);

        return view;
    }

    /**
     * Render the error message field.
     * @param sourceString The text message to be displayed
     */
    public void displayMessage(String sourceString, int color){
        errorMessage.setText(Html.fromHtml(sourceString));
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setTextColor(color);
    }

    /**
     * This function will be called if user want to change password
     * @param oldPassword User current password
     * @param newPassword User new password
     * @param confirmPassword User new password (should be same as newPassword)
     */
    public void handleChangePassword(String oldPassword, String newPassword, String confirmPassword){
        if (oldPassword.isEmpty() || oldPassword == null) {
            displayMessage("You need to input your old password first.", red);
            return;
        }
        if (!newPassword.equals(confirmPassword)){
            displayMessage("Your new and confirm password must be the same.", red);
            return;
        }
        if (currentEmail == null || currentEmail.isEmpty()) {
            displayMessage("Unable to get your current email", red);
            return;
        }

        // Re-authenticate user to ensure they enter the correct old password
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(currentEmail, oldPassword);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
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
                } else {
                    displayMessage("Your old password is not correct.", red);
                }
            }
        });
    }

    /**
     * Function to update user information. It is called when clicked on Update button.
     */
    private View.OnClickListener updateUserInfoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String name = nameField.getText().toString();
            String email = emailField.getText().toString();
            String oldPassword = oldPasswordField.getText().toString();
            String newPassword = newPasswordField.getText().toString();
            String confirmPassword = confirmPasswordField.getText().toString();
            if (newPassword != null && confirmPassword != null){
                handleChangePassword(oldPassword, newPassword, confirmPassword);
            }
            if (name != null && !name.isEmpty()){
                currentName = name;
            }
            if (email != null && !email.isEmpty()){
                currentEmail = email;
            }
        }
    };

}