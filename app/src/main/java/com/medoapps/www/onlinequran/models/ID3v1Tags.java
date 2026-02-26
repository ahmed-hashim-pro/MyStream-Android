package com.medoapps.www.onlinequran.models;

public class ID3v1Tags {

    private final String title;
    private final String artist;
    private final String album;
    private final String year;
    private final String comment;
    private final int genreId;

    public ID3v1Tags(String title, String artist, String album, String year, String comment, int genreId) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.year = year;
        this.comment = comment;
        this.genreId = genreId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getYear() {
        return year;
    }

    public String getComment() {
        return comment;
    }

    public int getGenreId() {
        return genreId;
    }
}