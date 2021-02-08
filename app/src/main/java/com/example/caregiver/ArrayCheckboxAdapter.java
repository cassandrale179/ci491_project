package com.example.caregiver;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ArrayCheckboxAdapter<T> extends ArrayAdapter<T> {

    public ArrayCheckboxAdapter(Context context, int layout, T[] array)
    {
        super(context, layout, array);
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        LinearLayout row = new LinearLayout(this.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        // Add the checkbox
        CheckBox checkbox = new CheckBox(this.getContext());
        row.addView(checkbox);

        // Add the descriptive text
        EditText text = new EditText(this.getContext());
        text.setText(this.getItem(position).toString());
        row.addView(text);

        return row;
    }
}
