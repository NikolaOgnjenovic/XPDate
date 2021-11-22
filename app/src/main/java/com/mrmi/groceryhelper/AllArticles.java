package com.mrmi.groceryhelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

public class AllArticles extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_articles);

        //Display all of the articles using the recyclerView and its adapter
        recyclerView = findViewById(R.id.ArticleRecyclerView);
        recyclerViewAdapter = new RecyclerViewAdapter(this);
        recyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    //Launch main activity on back pressed
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }
}