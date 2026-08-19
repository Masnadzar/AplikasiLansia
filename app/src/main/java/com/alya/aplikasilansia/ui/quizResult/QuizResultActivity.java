package com.alya.aplikasilansia.ui.quizResult;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.GdsInterpretation;

public class QuizResultActivity extends AppCompatActivity {

    private ProgressBar statsProgressBar;
    private TextView numberScoreTextView, resultScoreTextView;
    private TextView tvKesimpulanText, tvSaranText;
    private View viewAccentBar;
    private Button backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // Initialize views
        statsProgressBar = findViewById(R.id.stats_progressbar);
        numberScoreTextView = findViewById(R.id.number_score);
        resultScoreTextView = findViewById(R.id.result_score);
        tvKesimpulanText = findViewById(R.id.tv_kesimpulan_text);
        tvSaranText = findViewById(R.id.tv_saran_text);
        viewAccentBar = findViewById(R.id.view_accent_bar);
        backButton = findViewById(R.id.btn_back_result);

        // Retrieve and display quiz result
        int totalScore = getIntent().getIntExtra("total_score", 0);
        int maxScore = 15;
        String classifiedScore = getIntent().getStringExtra("classified_score");

        // Set score to ProgressBar and TextViews
        updateDoughnutChart(totalScore, maxScore);
        resultScoreTextView.setText("Tingkat depresi Anda: \n " + classifiedScore);

        // Tambahan: kesimpulan & saran berdasarkan skor
        displayInterpretation(totalScore);

        // Set back button listener
        backButton.setOnClickListener(v -> finish());
    }

    private void displayInterpretation(int totalScore) {
        GdsInterpretation.Result interpretation = GdsInterpretation.getInterpretation(totalScore);

        tvKesimpulanText.setText(interpretation.kesimpulan);
        tvSaranText.setText(interpretation.saran);

        int color = getColorForLevel(interpretation.level);
        viewAccentBar.setBackgroundColor(color);
    }

    private int getColorForLevel(int level) {
        switch (level) {
            case 0:
                return ContextCompat.getColor(this, R.color.level0);
            case 1:
                return ContextCompat.getColor(this, R.color.level1);
            case 2:
                return ContextCompat.getColor(this, R.color.level2);
            case 3:
                return ContextCompat.getColor(this, R.color.level3);
            default:
                return ContextCompat.getColor(this, R.color.level0);
        }
    }

    private void updateDoughnutChart(int totalScore, int maxScore) {
        statsProgressBar.setProgress(totalScore);

        // Determine color based on the total score
        int color;
        if (totalScore <= 4) {
            color = ContextCompat.getColor(this, R.color.level0);
        } else if (totalScore > 4 && totalScore <= 8) {
            color = ContextCompat.getColor(this, R.color.level1);
        } else if (totalScore > 8 && totalScore <= 11) {
            color = ContextCompat.getColor(this, R.color.level2);
        } else if (totalScore > 11) {
            color = ContextCompat.getColor(this, R.color.level3);
        } else {
            color = ContextCompat.getColor(this, R.color.level0);
        }

        // Apply the color to the progress bar
        statsProgressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        // Update score display as a string
        numberScoreTextView.setText(String.valueOf(totalScore));
    }
}