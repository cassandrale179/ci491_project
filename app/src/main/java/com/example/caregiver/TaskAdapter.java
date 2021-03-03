package com.example.caregiver;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.Duration;

public class TaskAdapter extends ArrayAdapter<ViewProgress.Task> {
    public TaskAdapter(Context context, int layout, ViewProgress.Task[] array)
    {
        super(context, layout, array);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        ViewProgress.Task t = getItem(position);
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // add a vertical LinearLayout to our row, which will hold the Task name and duration
        LinearLayout vert = new LinearLayout(getContext());
        vert.setOrientation(LinearLayout.VERTICAL);
        vert.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        vert.setGravity(Gravity.LEFT);
        row.addView(vert);

        // add the Task name to vert
        TextView taskName = new TextView(getContext());
        taskName.setText(t.getName());
        taskName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        taskName.setTypeface(null, Typeface.BOLD);
        taskName.setGravity(Gravity.LEFT);
        vert.addView(taskName);

        // add the Duration text to vert
        TextView duration = new TextView(getContext());
        duration.setText("Time completed: " + durationToString(t.getTimeCompleted()));
        duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        duration.setGravity(Gravity.LEFT);
        vert.addView(duration);

        // add an inner row to hold the dot and status
        LinearLayout innerRow = new LinearLayout(getContext());
        innerRow.setOrientation(LinearLayout.HORIZONTAL);
        innerRow.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        innerRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
        row.addView(innerRow);

        //add the colored status dot to the right side of the row
        ImageView dot = new ImageView(getContext());
        switch (t.getStatus()) {
            case Completed:
                dot.setImageResource(R.drawable.status_dot_green);
                break;
            case Incomplete:
                dot.setImageResource(R.drawable.status_dot_red);
                break;
            case InProgress:
                dot.setImageResource(R.drawable.status_dot_yellow);
                break;
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        dot.setLayoutParams(lp);
        innerRow.addView(dot);

        //add the Completion status to the right side of the row
        TextView status = new TextView(getContext());
        if (t.getStatus() == ViewProgress.TaskStatus.InProgress)
        {
            status.setText("In Progress");
        }
        else
        {
            status.setText(t.getStatus().toString());
        }
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        status.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //status.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        innerRow.addView(status);

        return row;
    }

    public String durationToString(Duration d)
    {

        if (d == null) {
            return "N/A";
        }

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