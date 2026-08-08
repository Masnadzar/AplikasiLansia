package com.alya.aplikasilansia.ui.news;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alya.aplikasilansia.R;
import com.bumptech.glide.Glide;

public class NewsContentActivity extends AppCompatActivity {
    private ImageView newsImage;
    private TextView tvTitle, tvDate, tvCategory, tvSource, tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_content);

        Button btnBackNews = findViewById(R.id.btn_back_news);

        newsImage = findViewById(R.id.newsContentImg);
        tvTitle = findViewById(R.id.tvCtnNewsTitle);
        tvDate = findViewById(R.id.tvCtnNewsDate);
        tvCategory = findViewById(R.id.tvCtnNewsCate);
        tvSource = findViewById(R.id.tv_ctn_source);
        tvContent = findViewById(R.id.tvNewsContent);

        tvSource.setPaintFlags(tvSource.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btnBackNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getDataFromIntent();
    }

    private void getDataFromIntent(){
        Intent intent = getIntent();
        if (intent != null) {
            String newsName = intent.getStringExtra("news_name");
            String newsDate = intent.getStringExtra("news_date");
            String newsCategory = intent.getStringExtra("news_category");
            String newsSource = intent.getStringExtra("news_source");
            String newsImageUri = intent.getStringExtra("news_image");
            String newsContent = intent.getStringExtra("news_content");

            tvTitle.setText(newsName);
            tvDate.setText(newsDate);
            tvCategory.setText(newsCategory);
            tvContent.setText(newsContent);

            if (newsImageUri != null && !newsImageUri.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(newsImageUri))
                        .placeholder(R.drawable.img_2)
                        .error(R.drawable.img_2)
                        .into(newsImage);
            } else {
                Glide.with(this)
                        .load(R.drawable.img_2)
                        .into(newsImage);
            }
            if (newsSource != null && !newsSource.isEmpty()) {
                tvSource.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentNews = new Intent(Intent.ACTION_VIEW);
                        intentNews.setData(Uri.parse(newsSource));
                        startActivity(intentNews);
                    }
                });
            } else {
                tvSource.setText("Source not available");
            }
        }
    }
}