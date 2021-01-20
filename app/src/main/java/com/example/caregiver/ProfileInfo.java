package com.example.caregiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileInfo extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * This function set the hint text on user profile
     * and display their current name, email and password.
     */
    public void displayUserInfo(View view){
        EditText nameField = (EditText) view.findViewById(R.id.profileName);
        nameField.setHint("HintFirst HintLast");

        EditText emailField = (EditText) view.findViewById(R.id.profileEmail);
        emailField.setHint("Email");

        EditText oldPasswordField = (EditText) view.findViewById(R.id.profileOldPassword);

        EditText newPasswordField =  (EditText) view.findViewById(R.id.profileNewPassword);
        newPasswordField.setHint("New Password");

        EditText newConfirmField = (EditText) view.findViewById(R.id.profileNewPassword2);
        newConfirmField.setHint("Confirm Password");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_info, container, false);
        displayUserInfo(view);
        return view;
    }
}