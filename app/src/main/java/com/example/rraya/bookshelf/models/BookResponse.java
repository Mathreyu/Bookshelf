package com.example.rraya.bookshelf.models;

import java.util.List;

/**
 * Created by rraya on 12/16/16.
 */

public class BookResponse {
    public List<Book> items;

    public List<Book> getItems() {
        return items;
    }

    public void setItems(List<Book> items) {
        this.items = items;
    }
}
