package com.alya.aplikasilansia.ui.berita;

import android.net.Uri;

/**
 * Model untuk collection Firestore "tips".
 * Field mengikuti struktur yang sudah dibuat di TipsForm.jsx (admin panel):
 * title, category, content, date, image.
 */
public class Tips {
    private String title;
    private String date;
    private String category;
    private String content;
    private Uri image;

    public Tips(String title, String date, String category, String content, Uri image) {
        this.title = title;
        this.date = date;
        this.category = category;
        this.content = content;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public Uri getImage() {
        return image;
    }
}