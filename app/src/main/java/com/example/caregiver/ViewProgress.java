package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.caregiver.model.Task;
import com.example.caregiver.services.EmailService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewProgress extends AppCompatActivity {
    private String caregiveeName;
    private String caregiveeID;
    private String caregiverEmail;
    private String[] allCaregiverEmails;
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_progress);

        // get our fields from the extras
        caregiveeName = getIntent().getStringExtra("caregiveeName");
        caregiveeID = getIntent().getStringExtra("caregiveeID");
        caregiverEmail = getIntent().getStringExtra("caregiverEmail");

        // Get all caregiver emails
        queryAllCaregiverEmails();

        // Set the text to use the correct caregivee name
        TextView nameText = findViewById(R.id.progressNameText);
        nameText.setText("Progress chart for " + caregiveeName);

        // Load all caregivee tasks
        Task taskModelObject = new Task();
        taskModelObject.getAllTasks(caregiveeID, new App.TaskCallback() {
            @Override
            public void onDataReceived(List<Task> tasks){
                renderTaskList(tasks);
            }
        });
    }


    private void queryAllCaregiverEmails()
    {
        final DatabaseReference ref = database.child("/users/" + caregiveeID);
        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)@Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                List<String> emails = new ArrayList<>();
                for (DataSnapshot caregiver : snapshot.child("caregivers").getChildren())
                {
                    String caregiverID = caregiver.getKey();
                    final DatabaseReference caregiverRef = database.child("/users/" + caregiverID);
                    caregiverRef.child("email").get().addOnSuccessListener((snap) -> {
                        emails.add(snap.getValue().toString());
                    });
                }
                allCaregiverEmails = emails.toArray(new String[0]);
            }@Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("error", "Can't query caregiver emails for this caregivee");
            }
        });
    }
        

    /**
     * Render the list on the left to display tasks a caregivee have completed
     * @param taskList A list of task objects the caregivee have completed.
     */
    protected void renderTaskList(List<Task> taskList){
        final ListView taskListView =  findViewById(R.id.taskList);
        List <Map< String,  String >> data = new ArrayList < Map < String, String >> ();
        for (Task t: taskList) {
            Map< String, String > taskItem = new HashMap< String,
                    String >(2);
            taskItem.put("title", t.taskName);
            if (t.timeCompleted != -1){
                String sub = "Time completed: " + t.timeCompleted + " seconds";
                taskItem.put("subtitle", sub);
            } else {
                taskItem.put("subtitle", "Time completed: incomplete");
            }
            data.add(taskItem);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this, data, android.R.layout.simple_list_item_2, new String[]{
                "title",
                "subtitle"
        },
                new int[]{
                        android.R.id.text1,
                        android.R.id.text2
                });
        taskListView.setAdapter(adapter);
    }


    public void emailSelf(View view)
    {
        EmailService emailService = new EmailService(this);
        String emailBody = generateProgressReport();
        emailService.sendEmail(new String[]{caregiverEmail}, "Caregiver progress report: " + caregiveeName, emailBody);
    }

    public void emailAllCaregivers(View view)
    {
        EmailService emailService = new EmailService(this);
        String emailBody = generateProgressReport();
        emailService.sendEmail(allCaregiverEmails, "Caregiver progress report: " + caregiveeName, emailBody);
    }

    private String generateProgressReport()
    {
        String report = "Progress report for " + caregiveeName + ":\n";
        report += "\n";

        ListAdapter taskListAdapter = ((ListView)findViewById(R.id.taskList)).getAdapter();

        int numLines = taskListAdapter.getCount();
        for (int i = 0; i < numLines; i++)
        {
            report += ((Map<String, String>)taskListAdapter.getItem(i)).get("title") + "\n";
            report += "\t" + ((Map<String, String>)taskListAdapter.getItem(i)).get("subtitle") + "\n";
            report += "\n";
        }

        return report;
    }
}