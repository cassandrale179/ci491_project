package com.example.caregiver;

import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import static android.graphics.Color.BLACK;

public class AddCaregivee extends AppCompatActivity {

    private LinearLayout vertLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_caregivee);
        vertLayout = (LinearLayout) findViewById(R.id.vertLayout);
    }


    public void onAddMoreClicked(View view) {
        // Create new horizontal linear layout
        LinearLayout newLine = new LinearLayout(this);
        newLine.setOrientation(LinearLayout.HORIZONTAL);
        newLine.setLayoutParams(((LinearLayout)view.getParent()).getLayoutParams());

        // Create a textbox and add it to newLine
        EditText text = new EditText(this);
        text.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.85f));
        text.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        text.setHint("Email");
        newLine.addView(text);

        // Create a button and add it to newLine
        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.15f));
        deleteButton.setBackgroundColor(BLACK);
        deleteButton.setImageResource(R.drawable.ic_baseline_delete_white);
        newLine.addView(deleteButton);

        vertLayout.addView(newLine, vertLayout.getChildCount() - 2); // Insert the new line after the last Email textbox

    }
}