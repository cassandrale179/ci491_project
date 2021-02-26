package com.example.caregiver;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Main adapter for an expandable list (used for caregiver or caregivee app)
 */
public class MainAdapter extends BaseExpandableListAdapter {
    ArrayList<String> listGroup;
    HashMap<String,ArrayList<String>> listChild;

    public MainAdapter(ArrayList<String> listGroup, HashMap<String,ArrayList<String>> listChild){
        this.listGroup = listGroup;
        this.listChild = listChild;
    }

    @Override
    public int getGroupCount() {
        if (listGroup != null){
            return listGroup.size();
        }
        return 0;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (listChild != null &&  listChild.get(listGroup.get(groupPosition)) != null){
            return listChild.get(listGroup.get(groupPosition)).size();
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (listGroup != null){
            return listGroup.get(groupPosition);
        }
        return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (listGroup != null && listChild != null){
            return listChild.get(listGroup.get(groupPosition)).get(childPosition);
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(viewGroup.getContext()).inflate(
                android.R.layout.simple_expandable_list_item_1, viewGroup, false);
        TextView textView = view.findViewById(android.R.id.text1);
        String sGroup = String.valueOf(getGroup(groupPosition));
        textView.setText(sGroup);
        textView.setTypeface(null, Typeface.BOLD);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup viewGroup) {
        view =  LayoutInflater.from(viewGroup.getContext()).inflate(
                android.R.layout.simple_selectable_list_item, viewGroup, false);
        TextView textView = view.findViewById(android.R.id.text1);
        String sChild = String.valueOf(getChild(groupPosition, childPosition));
        textView.setText("    " + sChild);
        textView.setTypeface(null, Typeface.NORMAL);

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
