package com.example.rraya.bookshelf.models;

import java.util.List;

/**
 * Created by rraya on 12/16/16.
 */

public class VolumeInfo {

    public String title;
    public ImageLinks imageLinks;
    public String publishedDate;
    public List<String> authors;
    public String description;

    public VolumeInfo(String title, ImageLinks imageLinks, String publishedDate, List<String> authors, String description) {
        this.title = title;
        this.imageLinks = imageLinks;
        this.publishedDate = publishedDate;
        this.authors = authors;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ImageLinks getSelfLink() {
        return imageLinks;
    }

    public void setSelfLink(ImageLinks imageLinks) {
        this.imageLinks = imageLinks;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        description = description;
    }
}
