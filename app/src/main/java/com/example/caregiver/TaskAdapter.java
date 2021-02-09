package com.example.caregiver;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.ListAdapter;

import java.time.Duration;
import java.time.Period;

public class TaskAdapter extends ArrayAdapter<Task> {

    public TaskAdapter(Context context, int layout, Task[] array)
    {
        super(context, layout, array);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        Task t = getItem(position);
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        // add a vertical LinearLayout to our row, which will hold the Task name and duration
        LinearLayout vert = new LinearLayout(getContext());
        vert.setOrientation(LinearLayout.VERTICAL);
        vert.setGravity(Gravity.LEFT);
        row.addView(vert);

        // add the Task name to vert
        TextView taskName = new TextView(getContext());
        taskName.setText(t.getName());
        taskName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        taskName.setTypeface(null, Typeface.BOLD);
        vert.addView(taskName);

        // add the Duration text to vert
        TextView duration = new TextView(getContext());
        if (t.getTimeCompleted() == null)
        {
            duration.setText("N/A");
        }
        else {
            duration.setText("Time completed: " + durationToString(t.getTimeCompleted()));
        }
        duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        vert.addView(duration);

        //add the Completion status to the right side of the row
        TextView status = new TextView(getContext());
        if (t.getStatus() == TaskStatus.InProgress)
        {
            status.setText("In Progress");
        }
        else
        {
            status.setText(t.getStatus().toString());
        }
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        row.setGravity(Gravity.RIGHT);
        row.addView(status);

        return row;
    }

    public String durationToString(Duration d)
    {
        // Helper method to convert Duration to human-readable strings. Since toString() yields
        // an ISO-8601 format time, which is not easily readable.

        long hours = (long)Math.floor(d.toHours());
        long minutes = (long)Math.floor(d.toMinutes()) - (60 * hours);
        long seconds = (d.toMillis() / 1000) - (3600 * hours) - (60 * minutes);

        String s = "";
        if (hours != 0) {
            s = s.concat(hours + " hr, ");
        }
        if (minutes != 0 || hours != 0) {
            s = s.concat(minutes + " min, ");
        }

        s = s.concat(seconds + " s");


        return s;
    }
}
