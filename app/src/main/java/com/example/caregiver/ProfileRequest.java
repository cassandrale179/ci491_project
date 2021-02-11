package com.example.caregiver;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileRequest#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileRequest extends Fragment {


    public ProfileRequest() {
        // Required empty public constructor
    }

    public static ProfileRequest newInstance(String param1, String param2) {
        ProfileRequest fragment = new ProfileRequest();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_request, container, false);

        // Add list
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("randall_flagg@gmail.com");
        arrayList.add("roland_deschain@gmail.com");
        arrayList.add("sue_snell@gmail.com");
        arrayList.add("margaret_white@gmail.com");
        arrayList.add("tanya_roberson@gmail.com");
        arrayList.add("teddy_weizak@gmail.com");

        final ListView list = view.findViewById(R.id.requestList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, arrayList);
        list.setAdapter(arrayAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("You want to approve " + arrayList.get(position).toString() + "?")
                        .setPositiveButton("Approve", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /** Add approved caregiver / caregivee to Firebase ... */
                            }
                        }).setNegativeButton("Cancel", null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return view;
    }
}