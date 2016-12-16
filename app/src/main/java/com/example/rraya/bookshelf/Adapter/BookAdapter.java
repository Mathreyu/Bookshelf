package com.example.rraya.bookshelf.Adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rraya.bookshelf.R;
import com.example.rraya.bookshelf.models.Book;

import java.util.List;

/**
 * Created by rraya on 12/16/16.
 */

public class BookAdapter extends BaseAdapter{
    private  List<Book> bookList;


    public BookAdapter(List<Book> bookList) {
        this.bookList = bookList;
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public Book getItem(int i) {
        return bookList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View item = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.book_item,viewGroup, false);
        TextView textView = (TextView) item.findViewById(R.id.book_title);
        textView.setText(bookList.get(i).getTitle());
        return item;
    }
}
