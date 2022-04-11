package com.mrmi.roktrajanja;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
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

    private final boolean inAllArticles;

    private final int[] mainMenuColors;

    public ExpandableListViewAdapter(Context context, List<String> listDataGroup, HashMap<String, List<String>> listChildData, boolean inAllArticles) {
        this.context = context;
        this.listDataGroup = listDataGroup;
        this.listDataChild = listChildData;
        this.inAllArticles = inAllArticles;

        mainMenuColors = new int[]{
                R.color.darkRed,
                R.color.red,
                R.color.orange,
                R.color.yellow,
                R.color.lightGreen,
                R.color.green
        };
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return Objects.requireNonNull(this.listDataChild.get(this.listDataGroup.get(groupPosition))).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //Child view inflation: the article
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (inAllArticles) {
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

    //Group view inflation: the category (expiring in < 7 days, < 30 days in MainActivity, article category in AllArticles)
    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        ImageButton addButton;

        //Inflate the view if it's null
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //If the adapter is called from the All Articles class, add the add button and it's functionality to each category (group view)
            if (inAllArticles) {
                convertView = layoutInflater.inflate(R.layout.list_row_group_add, null);
            } else {
                convertView = layoutInflater.inflate(R.layout.list_row_group, null);
            }
        }

        //Add button functionality if in all articles
        if (inAllArticles) {
            addButton = convertView.findViewById(R.id.addButton);
            addButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, AddArticle.class);
                intent.putExtra("ArticleCategoryId", groupPosition);
                context.startActivity(intent);
            });
            addButton.setFocusable(false);
        } else {
            //Change background of parent view if in main menu
            convertView.findViewById(R.id.linearLayout).getBackground().setColorFilter(context.getResources().getColor(mainMenuColors[groupPosition]), PorterDuff.Mode.SRC_ATOP);
        }

        //Set the group's text
        TextView textViewGroup = convertView.findViewById(R.id.categoryGroupTextView);
        textViewGroup.setText(headerTitle);
        textViewGroup.setTypeface(ResourcesCompat.getFont(context, R.font.open_sans_bold));
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}