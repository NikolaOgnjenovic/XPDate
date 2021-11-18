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

        /*new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();

                ArticleList articleListClass = new ArticleList(AllArticles.this);
                ArrayList<Article> articleList = articleListClass.getArticleList();
                articleList.remove(position);
                recyclerViewAdapter.notifyItemRemoved(position);
                recyclerViewAdapter.notifyItemRangeChanged(position, articleList.size());
                articleListClass.saveArticles();
                recyclerView.removeViewAt(position);
                recyclerViewAdapter.notifyDataSetChanged();
            }
        }).attachToRecyclerView(recyclerView);
        */
    }

    //Launch main activity on back pressed
    @Override
    public void onBackPressed() {
        startActivity(new Intent(AllArticles.this, MainActivity.class));
    }
}