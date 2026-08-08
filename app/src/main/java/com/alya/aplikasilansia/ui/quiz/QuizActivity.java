package com.alya.aplikasilansia.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.Question;
import com.alya.aplikasilansia.ui.quizResult.QuizResultActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class QuizActivity extends AppCompatActivity {

    private QuizViewModel quizViewModel;
    private TextView questionText;
    private Button buttonYes, buttonNo, buttonNext, buttonPrevious, buttonEndQuiz, buttonBackQuiz;
    private RecyclerView recyclerView;
    private QuestionAdapter questionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionText = findViewById(R.id.questionText);
        buttonYes = findViewById(R.id.buttonYes);
        buttonNo = findViewById(R.id.buttonNo);
        buttonNext = findViewById(R.id.buttonNext);
        buttonPrevious = findViewById(R.id.buttonPrevious);
        buttonEndQuiz = findViewById(R.id.buttonEndQuiz);
        recyclerView = findViewById(R.id.recyclerView_quiz);
        buttonBackQuiz = findViewById(R.id.btn_back_quiz);

        // Set layout manager for the RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(gridLayoutManager);
        quizViewModel = new ViewModelProvider(this).get(QuizViewModel.class);
        questionAdapter = new QuestionAdapter(position -> quizViewModel.selectQuestion(position));
        recyclerView.setAdapter(questionAdapter);

        quizViewModel.getQuestionsLiveData().observe(this, questions -> {
            if (questions != null && !questions.isEmpty()) {
                displayQuestion(questions.get(0));
            }
        });
        // observe live data item question list for recycler view
        quizViewModel.getItemQuestionLiveData().observe(this, questionList -> {
            questionAdapter.updateQuestionList(questionList);
        });

        quizViewModel.getCurrentQuestion().observe(this, this::displayQuestion);

        quizViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && !isLoading) {
                Log.d("QuizActivity", "Quiz loading completed.");
            }
        });
        quizViewModel.getQuizCompleted().observe(this, quizCompleted -> {
            if (quizCompleted != null && quizCompleted) {
                Toast.makeText(this, "Berhasil menyelesaikan quiz!", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity or navigate to another screen
            }
        });

        quizViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        // Observe user answers LiveData in your activity/fragment
        buttonYes.setOnClickListener(v -> {
            quizViewModel.updateUserAnswer(quizViewModel.getCurrentQuestionIndex(), true); // Update user's answer to true (Yes)
            moveToNextQuestion();
        });

        buttonNo.setOnClickListener(v -> {
            quizViewModel.updateUserAnswer(quizViewModel.getCurrentQuestionIndex(), false); // Update user's answer to false (No)
            moveToNextQuestion();
        });

        buttonNext.setOnClickListener(v -> moveToNextQuestion());

        buttonPrevious.setOnClickListener(v -> moveToPreviousQuestion());

        buttonEndQuiz.setOnClickListener(v -> {
            if (!quizViewModel.areAllQuestionsAnswered()) {
                incompleteFormDialog();
                return;
            } else {
                showLogoutDialog();
            }
        });

        buttonBackQuiz.setOnClickListener(v -> finish());
    }
    private void incompleteFormDialog(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.incomplete_form_dialog, null);

        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Set the custom background drawable with rounded corners
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        // Adjust dialog size programmatically after showing it
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 80% of screen width
        params.height = WindowManager.LayoutParams.WRAP_CONTENT; // Adjust height as needed
        dialog.getWindow().setAttributes(params);

        // Get the buttons from the custom layout and set click listeners
        Button btnClose = dialogView.findViewById(R.id.btn_close_incomplete);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void displayQuestion(Question question) {

        if (question != null) {
            questionText.setText(question.getText());
            int currentIndex = quizViewModel.getCurrentQuestionIndex();
            int totalQuestions = quizViewModel.getTotalQuestions();

            if (currentIndex == totalQuestions - 1) {
                buttonEndQuiz.setVisibility(View.VISIBLE);
                buttonNext.setVisibility(View.GONE);
            } else {
                buttonEndQuiz.setVisibility(View.GONE);
                buttonNext.setVisibility(View.VISIBLE);
            }
        }
    }
    private void moveToNextQuestion() {
        int nextIndex = quizViewModel.getCurrentQuestionIndex() + 1;
        if (nextIndex < quizViewModel.getTotalQuestions()) {
            quizViewModel.selectQuestion(nextIndex);
        }
    }
    private void moveToPreviousQuestion() {
        int prevIndex = quizViewModel.getCurrentQuestionIndex() - 1;
        if (prevIndex >= 0) {
            quizViewModel.selectQuestion(prevIndex);
        }
    }
    // Replace with actual quiz ID generation logic
    private String generateQuizId() {
        // Use the current time with a specific format as the quiz ID
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mmss");
        return sdf.format(new Date());
    }

    private boolean getUserAnswerForQuestion(int questionIndex) {
        List<Boolean> userAnswers = quizViewModel.getUserAnswers().getValue();
        List<Question> questions = quizViewModel.getQuestionsLiveData().getValue();

        if (questions != null && userAnswers != null && questionIndex < questions.size() && questionIndex < userAnswers.size()) {
            Question question = questions.get(questionIndex);
            Boolean userAnswer = userAnswers.get(questionIndex);
            return userAnswer != null && userAnswer.equals(question.isCorrectAnswer());
        }
        Log.d("QuizActivity", "Storing answers for questions: " + questions + " with user answers: " + userAnswers + " for question index: " + questionIndex);
        return false;
    }
    public void showLogoutDialog() {
        // Inflate the custom layout/view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.confirm_quiz_dialog, null);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Create the AlertDialog
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Set the custom background drawable with rounded corners
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.custom_corner_rounded);

        // Adjust dialog size programmatically after showing it
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 90% of screen width
        params.height = WindowManager.LayoutParams.WRAP_CONTENT; // Adjust height as needed
        dialog.getWindow().setAttributes(params);

        // Get the buttons from the custom layout and set click listeners
        Button buttonConfirm = dialogView.findViewById(R.id.btn_quiz_confirmed);
        Button buttonCancel = dialogView.findViewById(R.id.btn_quiz_cancel);

        buttonConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectAndEndQuiz();
                dialog.dismiss(); // Ensure dialog is dismissed after logout
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    private void collectAndEndQuiz() {
                // Collect user answers
                List<Question> answeredQuestions = quizViewModel.getQuestionsLiveData().getValue();
                if (answeredQuestions != null) {
                    Map<String, Boolean> userAnswers = new HashMap<>();
                    for (int i = 0; i < answeredQuestions.size(); i++) {
                        boolean userAnswer = getUserAnswerForQuestion(i);
                        userAnswers.put("question_" + i, userAnswer);
                    }

                    if (userAnswers.size() == answeredQuestions.size()) {
                        // Check if all questions are answered
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String quizId = generateQuizId();
                        quizViewModel.endQuiz(userId, quizId, userAnswers);

                        int totalScore = quizViewModel.calculateTotalScore();
                        int maxScore = 15;
                        String classifiedScore = quizViewModel.classifyScore(totalScore);

                        Intent intent = new Intent(this, QuizResultActivity.class);
                        intent.putExtra("total_score", totalScore);
                        intent.putExtra("max_score", maxScore);
                        intent.putExtra("classified_score", classifiedScore);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(QuizActivity.this, "Please answer all questions before ending the quiz.", Toast.LENGTH_SHORT).show();
                    }
                }
    }
}