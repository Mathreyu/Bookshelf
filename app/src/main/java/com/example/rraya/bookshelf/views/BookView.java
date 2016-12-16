package com.example.rraya.bookshelf.views;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rraya.bookshelf.R;
import com.example.rraya.bookshelf.models.Book;
import com.example.rraya.bookshelf.models.VolumeInfo;
import com.squareup.picasso.Picasso;

/**
 * Created by rraya on 12/16/16.
 */

public class BookView extends Activity{
    ImageView cover;
    TextView authors;
    TextView description;
    TextView date;
    String coverText;
    String descriptionText;
    String authorsText;
    String dateText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_book);
        cover = (ImageView) findViewById(R.id.selfLink);
        authors = (TextView) findViewById(R.id.authors);
        description = (TextView) findViewById(R.id.description);
        date = (TextView) findViewById(R.id.date);

        coverText = getIntent().getExtras().getString("selfLink");
        descriptionText = getIntent().getExtras().getString("description");
        authorsText = getIntent().getExtras().getString("authors");
        dateText = getIntent().getExtras().getString("publishedDate");

        Picasso.with(this).load(coverText).into(cover);
        date.setText(dateText);
        authors.setText(authorsText);
        description.setText(descriptionText);

    }
}
