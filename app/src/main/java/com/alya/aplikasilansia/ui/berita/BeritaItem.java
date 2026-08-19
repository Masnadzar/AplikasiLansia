package com.alya.aplikasilansia.ui.berita;

import android.net.Uri;

/**
 * Wrapper model yang menyatukan News dan Tips supaya bisa ditampilkan
 * dalam satu RecyclerView (feed gabungan) dengan filter chip
 * Semua / Berita / Tips.
 */
public class BeritaItem {

    public enum Type {
        NEWS,
        TIPS
    }

    private Type type;
    private String title;
    private String date;
    private String category;
    private String content;
    private String source;   // null untuk Tips (Tips tidak punya field source)
    private Uri image;

    public BeritaItem(Type type, String title, String date, String category,
                      String content, String source, Uri image) {
        this.type = type;
        this.title = title;
        this.date = date;
        this.category = category;
        this.content = content;
        this.source = source;
        this.image = image;
    }

    public static BeritaItem fromNews(com.alya.aplikasilansia.ui.news.News news) {
        return new BeritaItem(
                Type.NEWS,
                news.getName(),
                news.getDate(),
                news.getCategory(),
                news.getNewsContent(),
                news.getSource(),
                news.getImage()
        );
    }

    public static BeritaItem fromTips(Tips tips) {
        return new BeritaItem(
                Type.TIPS,
                tips.getTitle(),
                tips.getDate(),
                tips.getCategory(),
                tips.getContent(),
                null,
                tips.getImage()
        );
    }

    public Type getType() {
        return type;
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

    public String getSource() {
        return source;
    }

    public Uri getImage() {
        return image;
    }

    /** Label badge kecil buat dibedain di UI, misal ikon lampu vs ikon tips */
    public boolean isTips() {
        return type == Type.TIPS;
    }
}