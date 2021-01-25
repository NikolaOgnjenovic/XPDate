package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;


public class AllArticles extends AppCompatActivity
{
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_articles);

        recyclerView = findViewById(R.id.ArticleRecyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(this);
        recyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}