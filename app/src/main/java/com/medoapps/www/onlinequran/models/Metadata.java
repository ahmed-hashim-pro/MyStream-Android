package com.medoapps.www.onlinequran.models;

import java.util.List;

public class Metadata {
    //header metadata
    private String uri, //uri of audio file
            format; //audio file format
    private int sampleRate, //number of samples taken per second
            trackLength; //track length in seconds
    private long fileSize; //file size in bytes
    private boolean isLossless; //if the audio codec is lossless or lossy

    //audio metadata
    public String title,
            album,
            track,
            year,
            rating,
            comment,
            lyrics;

    public byte[] coverArtList;
    public List artistList,
            albumArtistList,
            genreList;

    public Metadata(String uri, String format, int sampleRate, int trackLength, long fileSize, boolean isLossless, String title, String album, String track, String year, String rating, String comment, String lyrics, byte[] coverArtList, List artistList, List albumArtistList, List genreList) {
        this.uri = uri;
        this.format = format;
        this.sampleRate = sampleRate;
        this.trackLength = trackLength;
        this.fileSize = fileSize;
        this.isLossless = isLossless;
        this.title = title;
        this.album = album;
        this.track = track;
        this.year = year;
        this.rating = rating;
        this.comment = comment;
        this.lyrics = lyrics;
        this.coverArtList = coverArtList;
        this.artistList = artistList;
        this.albumArtistList = albumArtistList;
        this.genreList = genreList;
    }

    public String getUri() {
        return uri;
    }

    public String getFormat() {
        return format;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public int getTrackLength() {
        return trackLength;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isLossless() {
        return isLossless;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public String getTrack() {
        return track;
    }

    public String getYear() {
        return year;
    }

    public String getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getLyrics() {
        return lyrics;
    }

    public byte[] getCoverArtList() {
        return coverArtList;
    }

    public List getArtistList() {
        return artistList;
    }

    public List getAlbumArtistList() {
        return albumArtistList;
    }

    public List getGenreList() {
        return genreList;
    }
}