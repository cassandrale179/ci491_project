package com.example.caregiver;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ArrayCheckboxAdapter<T> extends ArrayAdapter<T> {

    private ArrayList<Integer> selectedPositions;

    public ArrayCheckboxAdapter(Context context, int layout, T[] array)
    {
        super(context, layout, array);
        selectedPositions = new ArrayList<>();
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        LinearLayout row = new LinearLayout(this.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Add the checkbox
        CheckBox checkbox = new CheckBox(this.getContext());
        checkbox.setOnClickListener(view -> {
            if (selectedPositions.contains(position))
            {
                selectedPositions.remove(position);
            }
            else
            {
                selectedPositions.add(position);
            }
        });
        row.addView(checkbox);

        // Add the descriptive text
        TextView text = new TextView(this.getContext());
        text.setText(this.getItem(position).toString());
        text.setTextSize(20);
        row.addView(text);

        return row;
    }

    public T[] getSelectedObjects()
    {
        ArrayList<T> retVal = new ArrayList<T>();
        for (int i : selectedPositions)
        {
            retVal.add(getItem(i));
        }
        return (T[])retVal.toArray();
    }
}
