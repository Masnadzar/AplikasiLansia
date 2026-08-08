package com.alya.aplikasilansia.ui.quizResult;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.alya.aplikasilansia.R;

public class QuizResultActivity extends AppCompatActivity {

    private ProgressBar statsProgressBar;
    private TextView numberScoreTextView,resultScoreTextView;
    private Button backButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // Initialize views
        statsProgressBar = findViewById(R.id.stats_progressbar);
        numberScoreTextView = findViewById(R.id.number_score);
        resultScoreTextView = findViewById(R.id.result_score);
        backButton = findViewById(R.id.btn_back_result);

        // Retrieve and display quiz result
        int totalScore = getIntent().getIntExtra("total_score", 0);
        int maxScore = 15;
        String classifiedScore = getIntent().getStringExtra("classified_score");
        // Set score to ProgressBar and TextViews
        updateDoughnutChart(totalScore, maxScore);
        resultScoreTextView.setText("Tingkat depresi Anda: \n " + classifiedScore);
        // Set back button listener
        backButton.setOnClickListener(v -> finish());
    }

    private void updateDoughnutChart(int totalScore, int maxScore) {
        statsProgressBar.setProgress(totalScore);

        // Determine color based on the total score
        int color;
        if (totalScore <=4 ) {
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