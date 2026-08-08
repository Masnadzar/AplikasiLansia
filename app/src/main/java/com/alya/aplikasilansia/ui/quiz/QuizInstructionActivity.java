package com.alya.aplikasilansia.ui.quiz;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.alya.aplikasilansia.R;
import com.google.android.material.button.MaterialButton;

public class QuizInstructionActivity extends AppCompatActivity {

    private MaterialButton btnBackInstruction;
    private MaterialButton btnStartInstruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_instruction);

        // Initialize views
        btnBackInstruction = findViewById(R.id.btn_back_instruction);
        btnStartInstruction = findViewById(R.id.btn_start_instruction);

        // Set click listeners
        btnBackInstruction.setOnClickListener(view -> finish());

        btnStartInstruction.setOnClickListener(view -> {
            Intent intent = new Intent(QuizInstructionActivity.this, QuizActivity.class);
            startActivity(intent);
        });
    }
}