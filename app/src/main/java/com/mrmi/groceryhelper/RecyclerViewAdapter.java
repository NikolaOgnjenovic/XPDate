package com.mrmi.groceryhelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
{
    Context context;
    ArrayList<Article> articleList;
    ArticleList articleListClass;

    //Constructor
    public RecyclerViewAdapter(Context contextArg)
    {
        context = contextArg;

        //Instantiate the ArticleList class
        articleListClass = new ArticleList(context);

        //Reference the article list
        articleList = articleListClass.getArticleList();
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.article_row, parent, false);

        //Return the ViewHolder inner class constructor using an inflated view of article_row
        return new ViewHolder(view);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView articleNameText, articleExpirationDateText, articleExpirationCounterText;
        ImageButton deleteArticleButton;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            //Find all of the text views and the button in the current row's itemView
            articleNameText = itemView.findViewById(R.id.ArticleNameTextView);
            articleExpirationDateText = itemView.findViewById(R.id.ArticleExpirationDate);
            articleExpirationCounterText = itemView.findViewById(R.id.ArticleExpirationCounter);
            deleteArticleButton = itemView.findViewById(R.id.DeleteArticleButton);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder holder, final int position)
    {
        Article article = articleList.get(position);

        //Set the text of the text views using the current article's index (position)
        holder.articleNameText.setText(article.getName());
        holder.articleExpirationDateText.setText(article.getExpirationDate());

        String expirationCounterText;
        expirationCounterText = article.getExpirationText(context);

        holder.articleExpirationCounterText.setText(expirationCounterText);

        holder.deleteArticleButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                articleList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, articleList.size());

                articleListClass.saveArticles();
                System.out.println("[MRMI]: Removed item at position " + ", arraylist size: " + articleList.size());
            }
        });
    }

    //Returns the number of articles
    @Override
    public int getItemCount()
    {
        return articleList.size();
    }
}
