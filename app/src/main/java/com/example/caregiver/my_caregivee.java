package com.example.caregiver;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link my_caregivee#newInstance} factory method to
 * create an instance of this fragment.
 */


public class my_caregivee extends Fragment {

    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Get user info
        String userId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("userId", "");


        View v = inflater.inflate(R.layout.fragment_my_caregivee, null);
        ExpandableListView elv = (ExpandableListView) v.findViewById(R.id.exp_list_view);
        elv.setAdapter(new my_caregiveeAdapter(userId));

        return v;
    }

    public class my_caregiveeAdapter extends BaseExpandableListAdapter {

        private ArrayList<String> caregiveeIDs;
        private HashMap<String, String> caregiveeIDtoNameMap;

        private String[][] children = {
                { "    View Profile", "    Set Tasks", "    See Progress", "    Remove Caregivee"},
                { "    View Profile", "    Set Tasks", "    See Progress", "    Remove Caregivee"},
        };

        public my_caregiveeAdapter(String userID)
        {
            caregiveeIDs = new ArrayList<>();
            caregiveeIDtoNameMap = new HashMap<>();
            final DatabaseReference user = database.child("/users/" + userID);
            final DatabaseReference caregivees = user.child("caregivees");
            ValueEventListener valueEventListener = new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot caregiveeSnapshot: snapshot.getChildren()) {
                        String id = Objects.requireNonNull(caregiveeSnapshot.getKey());
                        String name = Objects.requireNonNull(caregiveeSnapshot.getValue()).toString();
                        caregiveeIDs.add(id);
                        caregiveeIDtoNameMap.put(id, name);
                    }
                    notifyDataSetChanged();

                }@Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("FAIL", "getCaregivees:onCancelled", databaseError.toException());
                }
            };
            caregivees.addValueEventListener(valueEventListener);
        }


        @Override
        public int getGroupCount() {
            return caregiveeIDs.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return children[i].length;
        }

        @Override
        public Object getGroup(int i) {
            return caregiveeIDtoNameMap.get(caregiveeIDs.get(i));
        }

        @Override
        public Object getChild(int i, int i1) {
            return children[i][i1];
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {

            TextView textView = new TextView(my_caregivee.this.getActivity());
            //Initialize and assign
            textView.setText(getGroup(i).toString());
            //Initialize string
            String sGroup = String.valueOf(getGroup(i));
            //Set text on text view
            textView.setText(sGroup);
            textView.setTextSize(30);
            //Set text style Bold
            textView.setTypeface(null, Typeface.BOLD);
            //Set text colour
            textView.setTextColor(Color.BLACK);
            //Return View
            return textView;

        }

        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
            TextView textView = new TextView(my_caregivee.this.getActivity());
            textView.setText(getChild(i, i1).toString());
//            textView.setText(getGroup(i).toString());
            //Initialize string
            String sChild = String.valueOf(getChild(i, i1));
            //Set text on text view
            textView.setText(sChild);
            textView.setTextSize(20);
            //Set text style Bold
            //textView.setTypeface(null, Typeface.BOLD);
            //Set text colour
            textView.setTextColor(Color.BLACK);
            textView.setOnClickListener(v -> {
                switch(i1) {
                    case 0:
                        ((Dashboard)getActivity()).replaceActiveFragment(new ProfileFragment());
                        break;
                    case 1:
                        ((Dashboard)getActivity()).replaceActiveFragment(new SetTasksFragment());
                        break;
                    case 2:
                        Intent intent = new Intent(getContext(), ViewProgress.class);
                        startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(getContext(), RemoveCaregivee.class);
                        startActivity(intent);
                        break;
                }
            });
            return textView;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }

    }

}

//public class my_caregivee extends AppCompatActivity {
//
//    //Initialize
//    ExpandableListView expandableListView ;
//    ArrayList<String> listGroup = new ArrayList<>();
//    HashMap<String,ArrayList<String>> listChild = new HashMap<>();
//    MainAdapter adapter;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.fragment_my_caregivee);
//
//        //Assign variable
//        expandableListView = findViewById(R.id.exp_list_view);
//
//        //Use for loop
//        for (int g=0;g<=5;g++){
//            //Add values in group list
//            listGroup.add("Username"+g);
//            //Initialize (group) array list
//            ArrayList<String> arrayList = new ArrayList<>();
//            //Use for loop
//            for(int c=0;c<=5;c++){
//                //Add values in (child) array list
//                arrayList.add("Item"+c);
//            }
//            //Put values in child list
//            listChild.put(listGroup.get(g),arrayList);
//        }
//
//        //Initialize adapter
//        adapter = new MainAdapter(listGroup,listChild);
//        //Set adapter
//        expandableListView.setAdapter(adapter);
//    }
//
//    /*// TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public my_caregivee() {
//        // Required empty public constructor
//    }
//
//    *//**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment my_caregivee.
//     *//*
//    // TODO: Rename and change types and number of parameters
//    public static my_caregivee newInstance(String param1, String param2) {
//        my_caregivee fragment = new my_caregivee();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }*/
//
//    /*@Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_my_caregivee, container, false);
//    }*/
//}