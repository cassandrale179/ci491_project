package com.example.caregiver;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import androidx.annotation.RequiresApi;
import androidx.annotation.NonNull;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link my_caregivee#newInstance} factory method to
 * create an instance of this fragment.
 */


public class my_caregivee extends Fragment {

    // Global reference to Firebase
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    // This hashmap store the caregivee id (key) and their name (value)
    HashMap<String, String> caregiveeInfo = new HashMap<>();

//    public void replaceFragment(Fragment someFragment) {
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//        transaction.replace(R.id.my_caregivee_fragment, someFragment);
//        //transaction.addToBackStack(null);
//        transaction.commit();
//    }

    /**
     * Return list of caregivees associated with the caregiver.
     */
//    public void queryCaregivees() {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String userId = preferences.getString("userId", "");
//        DatabaseReference ref = database.child("users/" + userId);
//
//        ref.addValueEventListener(new ValueEventListener() {@Override
//        public void onDataChange(@NonNull DataSnapshot snapshot) {
//            String value = snapshot.child("caregivees").getValue().toString();
//            List < String > caregivees = Arrays.asList(value.split("\\s*,\\s*"));
//            for (int i = 0; i < caregivees.size(); i++) {
//                getCaregiveeNameAndTask(caregivees.get(i), caregivees.size());
//            }
//        }@Override
//        public void onCancelled(@NonNull DatabaseError error) {
//            Log.d("error", "Can't query caregivees for this caregiver");
//        }
//        });
//    }
//
//    /**
//     * Query data for each caregivee.
//     * @param caregiveeId the id of the caregivee
//     * @param size the size of the caregivees list
//     */
//    protected void getCaregiveeNameAndTask(String caregiveeId, int size) {
//        DatabaseReference ref = database.child("users/" + caregiveeId);
//        ref.addValueEventListener(new ValueEventListener() {
//            @RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String name = dataSnapshot.child("name").getValue().toString();
//                Object taskObject = dataSnapshot.child("rooms").getValue();
//                if (taskObject != null) {
//                    Gson gson = new Gson();
//                    String tasksJson = gson.toJson(taskObject);
//                    List<Task> tasks = createRoomAndTaskObject(caregiveeId, tasksJson);
//                    taskList.put(caregiveeId, tasks);
//                }
//                caregiveeInfo.put(caregiveeId, name);
//
//                // TODO: hacky way of display the list. Need to use async.
//                if (caregiveeInfo.size() == size){
//                    displayCaregivee();
//                }
//            }@Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d("failure", "Unable to obtain data for this caregivee " + caregiveeId);
//            }
//        });
//    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_caregivee, null);
        ExpandableListView elv = (ExpandableListView) v.findViewById(R.id.exp_list_view);
        elv.setAdapter(new my_caregiveeAdapter());

        elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick (ExpandableListView elv, View view,
                                         int i, int i1, long id){
                if (i1 == 0) { //View Profile
                    Fragment fragment = new ProfileFragment();
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack();
                    fragmentManager.beginTransaction()
                            .replace(R.id.my_caregivee_fragment, fragment)
                            .commit();
                } else if (i1 == 1) { // Change Role
                    Fragment fragment = new BeaconFragment(); //Change this later
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.popBackStack();
                    fragmentManager.beginTransaction()
                            .replace(R.id.my_caregivee_fragment, fragment)
                            .commit();
                } else if (i1 == 2) { //Set Task
                    Intent intent = new Intent(view.getContext(), AddTask.class);
                    startActivity(intent);
                } else if (i1 == 3) {//See Progress
                    startActivity(new Intent(view.getContext(), ProfileFragment.class));
                } else if (i1 == 4) {//Remove Caregivee
                    startActivity(new Intent(view.getContext(), ProfileFragment.class));
                }
                return false;
            }
        });
        return v;
    }


    public class my_caregiveeAdapter extends BaseExpandableListAdapter {

        private String[] groups = {"User1", "User2"};

        private String[][] children = {
                {"    View Profile", "    Change Role", "    Set Tasks", "    See Progress", "    Remove Caregivee"},
                {"    View Profile", "    Change Role", "    Set Tasks", "    See Progress", "    Remove Caregivee"},
        };

        @Override
        public int getGroupCount() {
            return groups.length;
        }

        @Override
        public int getChildrenCount(int i) {
            return children[i].length;
        }

        @Override
        public Object getGroup(int i) {
            return groups[i];
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
            textView.setTextSize(25);
            textView.setPadding(0, 30, 0, 30);
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
            textView.setTextSize(15);
            textView.setPadding(0, 30, 0, 30);

            //view.setOnClickListener(new View.OnClickListener() {

            return textView;
        }



//                @Override
//                public void onClick(View view) {
//
//
//                }

//            @Override
////            public boolean OnCLick(ExpandableListView elv, View view, int groupPosition, int childPosition, long id)
//            public boolean OnCLick(ExpandableListView parent, View view, int i, int i1, long id){
////                View v = inflater.inflate(R.layout.fragment_my_caregivee, null);
//                ExpandableListView elv = (ExpandableListView) view.findViewById(R.id.exp_list_view);
//            }


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