package com.movinfo.messenger.model;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bson.types.ObjectId;

public class Movie {
    ObjectId id;
    private String name;
    private Date dateOpen;
    private byte[] poster;
    private SortedMap<Date, Screen> screenMap;
    private Date expireAt;

    public Movie(ObjectId id, String name, Date dateOpen, byte[] poster){
        this.id = id;
        this.name = name;
        this.dateOpen = dateOpen;
        this.poster = poster;
        this.screenMap = new TreeMap<>();
    }
    
    public ObjectId getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Date getDateOpen() {
        return dateOpen;
    }
    public void setDateOpen(Date dateOpen) {
        this.dateOpen = dateOpen;
    }
    public byte[] getPoster() {
        return poster;
    }
    public void setPoster(byte[] poster) {
        this.poster = poster;
    }
    public SortedMap<Date, Screen> getScreenMap() {
        return screenMap;
    }
    public void addScreen(Screen screen) {
        screenMap.put(screen.getScreenDate(), screen);
    }
    public Date getExpireAt() {
        return expireAt;
    }
    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }
}
