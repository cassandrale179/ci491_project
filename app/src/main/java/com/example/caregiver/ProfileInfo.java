package com.example.caregiver;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileInfo extends Fragment {

    public ProfileInfo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileInfo newInstance(String param1, String param2) {
        ProfileInfo fragment = new ProfileInfo();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * This function set the hint text on user profile
     * and display their current name, email and password.
     */
    public void displayUserInfo(View view, String userId){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("users/" + userId);

        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
             String name = dataSnapshot.child("name").getValue().toString();
             String email = dataSnapshot.child("email").getValue().toString();

            EditText nameField = (EditText) view.findViewById(R.id.profileName);
            nameField.setHint(name);

            EditText emailField = (EditText) view.findViewById(R.id.profileEmail);
            emailField.setHint(email);

            EditText oldPasswordField = (EditText) view.findViewById(R.id.profileOldPassword);

            EditText newPasswordField =  (EditText) view.findViewById(R.id.profileNewPassword);
            newPasswordField.setHint("New Password");

            EditText newConfirmField = (EditText) view.findViewById(R.id.profileNewPassword2);
            newConfirmField.setHint("Confirm Password");

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
        return view;
    }
}