package com.example.rraya.bookshelf;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.widget.ListView;

import com.example.rraya.bookshelf.API.BooksAPI;
import com.example.rraya.bookshelf.Adapter.BookAdapter;
import com.example.rraya.bookshelf.models.Book;
import com.example.rraya.bookshelf.models.BookResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    ListView bookShelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bookShelf = (ListView) findViewById(R.id.bookShelf);
        getBookList();
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
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });
    }
}
