package com.mrmi.groceryhelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {
    private final Context context;

    //Group titles
    private final List<String> listDataGroup;

    //Child data
    private final HashMap<String, List<String>> listDataChild;

    private final boolean childHasDeleteButton;
    public ExpandableListViewAdapter(Context context, List<String> listDataGroup, HashMap<String, List<String>> listChildData, boolean childHasDelete) {
        this.context = context;
        this.listDataGroup = listDataGroup;
        this.listDataChild = listChildData;
        this.childHasDeleteButton = childHasDelete;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return Objects.requireNonNull(this.listDataChild.get(this.listDataGroup.get(groupPosition))).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (childHasDeleteButton) {
                convertView = layoutInflater.inflate(R.layout.list_row_child_delete, null);
            } else {
                convertView = layoutInflater.inflate(R.layout.list_row_child, null);
            }

        }

        TextView textViewChild = convertView.findViewById(R.id.textViewChild);
        textViewChild.setText(childText);
        textViewChild.setTypeface(ResourcesCompat.getFont(context, R.font.open_sans));
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return Objects.requireNonNull(this.listDataChild.get(this.listDataGroup.get(groupPosition))).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataGroup.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataGroup.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_row_group, null);
        }

        TextView textViewGroup = convertView.findViewById(R.id.categoryGroupTextView);
        textViewGroup.setText(headerTitle);
        textViewGroup.setTypeface(ResourcesCompat.getFont(context, R.font.open_sans_bold));
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
} 