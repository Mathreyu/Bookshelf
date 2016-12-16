package com.example.rraya.bookshelf.API;


import com.example.rraya.bookshelf.models.Book;
import com.example.rraya.bookshelf.models.BookResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by rraya on 12/16/16.
 */

public interface BooksAPI {
    String ENDPOINTLIST = "https://www.googleapis.com/books/v1/";

    @GET("volumes?q=android&startIndex=0&maxResults=10")
    Call<BookResponse> listBooks();

    @GET("volumes/{volume}")
    Call<Book> volumeInfo(@Path("volume") String volume);

}
