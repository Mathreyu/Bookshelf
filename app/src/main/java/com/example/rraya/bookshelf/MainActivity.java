package com.example.rraya.bookshelf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.rraya.bookshelf.API.BooksAPI;
import com.example.rraya.bookshelf.Adapter.BookAdapter;
import com.example.rraya.bookshelf.models.Book;
import com.example.rraya.bookshelf.models.BookResponse;
import com.example.rraya.bookshelf.views.BookView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ListView bookShelf;
    List<Book> booksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bookShelf = (ListView) findViewById(R.id.bookShelf);
        getBookList();

        bookShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, BookView.class);
                intent.putExtra("authors", booksList.get(i).getVolumeInfo().getAuthors().get(0));
                intent.putExtra("publishedDate", booksList.get(i).getVolumeInfo().getPublishedDate());
                intent.putExtra("selfLink", booksList.get(i).getVolumeInfo().getSelfLink().getThumbnail());
                intent.putExtra("description", booksList.get(i).getVolumeInfo().getDescription());
                startActivity(intent);
            }
        });
    }

    public void getBookList(){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BooksAPI.ENDPOINTLIST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BooksAPI bookList = retrofit.create(BooksAPI.class);
        Call<BookResponse> callBookList = bookList.listBooks();

        callBookList.enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                BookResponse bookResponse;
                bookResponse = response.body();
                bookShelf.setAdapter(new BookAdapter(bookResponse.getItems()));
                booksList = bookResponse.getItems();
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }
}
