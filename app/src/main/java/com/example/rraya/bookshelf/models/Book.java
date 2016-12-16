package com.example.rraya.bookshelf.models;

/**
 * Created by rraya on 12/16/16.
 */

public class Book {
    VolumeInfo volumeInfo;
    public Book(VolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }

    public VolumeInfo getVolumeInfo() {
        return volumeInfo;
    }

    public void setVolumeInfo(VolumeInfo volumeInfo) {
        this.volumeInfo = volumeInfo;
    }
}
