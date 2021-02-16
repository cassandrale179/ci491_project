package com.example.caregiver;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ArrayCheckboxAdapter<T> extends ArrayAdapter<T> {

    private ArrayList<Integer> selectedPositions;

    public ArrayCheckboxAdapter(Context context, int layout, List<T> array)
    {
        super(context, layout, array);
        selectedPositions = new ArrayList<>();
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        LinearLayout row = new LinearLayout(this.getContext());
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Add the checkbox
        CheckBox checkbox = new CheckBox(this.getContext());
        checkbox.setScaleX(1.5f);
        checkbox.setScaleY(1.5f);
        checkbox.setHeight((int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getContext().getResources().getDisplayMetrics()));
        checkbox.setOnClickListener(view -> {
            if (selectedPositions.contains(position))
            {
                selectedPositions.remove(selectedPositions.indexOf(position));
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
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        row.addView(text);

        return row;
    }

    public ArrayList<T> getSelectedObjects()
    {
        ArrayList<T> retVal = new ArrayList<T>();
        for (int i : selectedPositions)
        {
            retVal.add(getItem(i));
        }
        return retVal;
    }
}
