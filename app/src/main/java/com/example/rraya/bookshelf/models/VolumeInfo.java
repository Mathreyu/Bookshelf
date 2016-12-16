package com.example.rraya.bookshelf.models;

/**
 * Created by rraya on 12/16/16.
 */

public class VolumeInfo {

    public String title;
    public String cover;
    public String publishedDate;
    public String author;
    public String Description;

    public VolumeInfo(String title, String cover, String publishedDate, String author, String description) {
        this.title = title;
        this.cover = cover;
        this.publishedDate = publishedDate;
        this.author = author;
        Description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
