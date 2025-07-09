package com.example.ebook.model;

public class Book {
    private String _id;
    private String title;
    private String author;
    private String description;
    private boolean is_active;
    private String cover_url;
    private int views;
    private int favorites;

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return is_active;
    }

    public void setActive(boolean active) {
        this.is_active = active;
    }

    public String getCoverUrl() {
        return cover_url;
    }

    public void setCoverUrl(String cover_url) {
        this.cover_url = cover_url;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }
}
