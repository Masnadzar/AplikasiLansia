package com.alya.aplikasilansia.ui.news;

import android.net.Uri;

public class News {
    private String name;
    private String date;
    private String category;
    private String newsContent;
    private String source;
    private Uri image;

    public News(String name, String date, String category, String source, Uri image, String newsContent) {
        this.name = name;
        this.date = date;
        this.category = category;
        this.source = source;
        this.image = image;
        this.newsContent = newsContent;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getNewsContent() {
        return newsContent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setNewsContent(String newsContent) {
        this.newsContent = newsContent;
    }

    public String getSource() {
        return source;
    }

    public Uri getImage() {
        return image;
    }

}
