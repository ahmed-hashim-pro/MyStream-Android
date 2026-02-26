package com.medoapps.www.onlinequran;

import java.io.Serializable;

public class Audio implements Serializable {



    private String data;
    private String title;
    private String album;
    private String artist;
    private String RecitesName;
    private String Rewayat;
    private String RealRecitesName;
    private String RecitesAYA;
    private Boolean IsRadio;

    public Audio(String data, String title, String album, String artist) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
    }

    public Audio(String data, String title, String album, String artist, String recitesName, String rewayat, String realRecitesName, String recitesAYA, Boolean isRadio) {
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        RecitesName = recitesName;
        Rewayat = rewayat;
        RealRecitesName = realRecitesName;
        RecitesAYA = recitesAYA;
        IsRadio = isRadio;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
    public String getRecitesName() {
        return RecitesName;
    }

    public void setRecitesName(String recitesName) {
        RecitesName = recitesName;
    }

    public String getRewayat() {
        return Rewayat;
    }

    public void setRewayat(String rewayat) {
        Rewayat = rewayat;
    }

    public String getRealRecitesName() {
        return RealRecitesName;
    }

    public void setRealRecitesName(String realRecitesName) {
        RealRecitesName = realRecitesName;
    }

    public String getRecitesAYA() {
        return RecitesAYA;
    }

    public void setRecitesAYA(String recitesAYA) {
        RecitesAYA = recitesAYA;
    }

    public Boolean getIsRadio() {
        return IsRadio;
    }

    public void setIsRadio(Boolean isRadio) {
        IsRadio = isRadio;
    }

}
