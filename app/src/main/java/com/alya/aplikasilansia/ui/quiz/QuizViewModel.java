package com.alya.aplikasilansia.ui.quiz;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.Question;
import com.alya.aplikasilansia.data.QuizHistoryItem;
import com.alya.aplikasilansia.data.QuizRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QuizViewModel extends ViewModel {
    private final QuizRepository repository;
    private final LiveData<List<Question>> questionsLiveData;
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private final LiveData<Question> currentQuestion;
    private final LiveData<Boolean> isLoading;
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> quizCompleted = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<Boolean>> userAnswers = new MutableLiveData<>();
    private final MutableLiveData<List<Boolean>> questionAnsweredStatus = new MutableLiveData<>();
    private final LiveData<List<QuizHistoryItem>> quizHistoryLiveData;


    // List for saving which question have been answered
    // true -> has been answered and
    // false -> still unanswered
    private final MutableLiveData<List<ItemQuestionModel>> isQuestionAnswered = new MutableLiveData<>(new ArrayList<>());


    public QuizViewModel() {
        repository = new QuizRepository();
        questionsLiveData = repository.getQuestionsLiveData();
        isLoading = repository.getIsLoading();
        quizHistoryLiveData = repository.getQuizHistoryLiveData();
        currentQuestion = Transformations.switchMap(currentQuestionIndex, index -> {
            List<Question> questions = questionsLiveData.getValue();
            if (questions != null && index >= 0 && index < questions.size()) {
                return new MutableLiveData<>(questions.get(index));
            } else {
                return new MutableLiveData<>(null);
            }
        });
        // Initialize user answers list with false values
        questionsLiveData.observeForever(questions -> {
            if (questions != null) {
                List<Boolean> initialStatus = new ArrayList<>(Collections.nCopies(questions.size(), false));
                questionAnsweredStatus.setValue(initialStatus);
                userAnswers.setValue(initialStatus);

                // Setting up list for background color conditional values
                List<ItemQuestionModel> isQuestionAnsweredInitial = new ArrayList<>();
                // Initial all item list will be false
                for (int i = 0; i < questions.size(); i++) {
                    isQuestionAnsweredInitial.add(new ItemQuestionModel(i, false ,questions.get(i)));
                }
                isQuestionAnswered.setValue(isQuestionAnsweredInitial);
            }
        });
    }
    public void fetchQuizHistory(String userId) {
        repository.fetchQuizHistory(userId);
    }

    public LiveData<List<QuizHistoryItem>> getQuizHistoryLiveData() {
        return quizHistoryLiveData;
    }
    public Integer calculateTotalScore() {
        int totalScore = 0;
        List<Boolean> answers = userAnswers.getValue();
        List<Question> questions = questionsLiveData.getValue();
        if (answers != null && questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                Question question = questions.get(i);
                Boolean userAnswer = answers.get(i);
                if (userAnswer != null && userAnswer.equals(question.isCorrectAnswer())) {
                    totalScore++;
                }
            }
        }
        Log.e("QuizViewModel", "Null values or mismatched sizes in calculateTotalScore");

        return totalScore;
    }
    public String classifyScore(int totalScore) {
        // Define the classification criteria
        if (totalScore <=4 ) {
            return "Normal";
        } else if (totalScore > 4 && totalScore <= 8) {
            return "Depresi Ringan";
        } else if (totalScore > 8 && totalScore <= 11) {
            return "Depresi Sedang";
        } else if (totalScore > 11) {
            return "Depresi Berat";
        } else {
            return "Normal";
        }
    }

    public LiveData<List<Question>> getQuestionsLiveData() {
        return questionsLiveData;
    }
    // Get live data value for itemQuestionLiveData
    public LiveData<List<ItemQuestionModel>> getItemQuestionLiveData() {
        return  isQuestionAnswered;
    }

    public LiveData<Question> getCurrentQuestion() {
        return currentQuestion;
    }
    public LiveData<List<Boolean>> getUserAnswers() {
        if (userAnswers.getValue() == null) {
            List<Boolean> initialAnswers = new ArrayList<>(Collections.nCopies(getTotalQuestions(), null));
            userAnswers.setValue(initialAnswers);
        }
        return userAnswers;
    }

    public boolean areAllQuestionsAnswered() {
        List<ItemQuestionModel> answeredStatus = isQuestionAnswered.getValue();
        if (answeredStatus != null) {
            for (ItemQuestionModel item : answeredStatus) {
                if (!item.isQuestionAnswered()) {
                    return false;
                }
            }
        }
        return true;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getQuizCompleted() {
        return quizCompleted;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void selectQuestion(int index) {
        currentQuestionIndex.setValue(index);
    }
    // Method to update user's answer for the current question
    public void updateUserAnswer(int index, boolean answer) {
        List<Boolean> currentAnswers = userAnswers.getValue();
        List<Boolean> answeredStatus = questionAnsweredStatus.getValue();
        List<ItemQuestionModel> isQuestionAnsweredOldValue = isQuestionAnswered.getValue();

        if (currentAnswers != null) {
            while (currentAnswers.size() <= index) {
                currentAnswers.add(null);
            }
            currentAnswers.set(index, answer);
            userAnswers.setValue(currentAnswers);
            questionAnsweredStatus.setValue(answeredStatus);
            if (isQuestionAnsweredOldValue != null) {
                ItemQuestionModel isQuestionAnsweredOldValueItem = isQuestionAnsweredOldValue.get(index);
                isQuestionAnsweredOldValue.set(index, new ItemQuestionModel(isQuestionAnsweredOldValueItem.getId(), true, isQuestionAnsweredOldValueItem.getQuestionData())); // Updating value that this question have been answered
                isQuestionAnswered.setValue(isQuestionAnsweredOldValue);
            }

        }
    }
    public LiveData<Integer> getTotalScore() {
        return score;
    }

    public int getTotalQuestions() {
        List<Question> questions = questionsLiveData.getValue();
        return (questions == null) ? 0 : questions.size();
    }

    public int getCurrentQuestionIndex() {
        Integer index = currentQuestionIndex.getValue();
        return (index == null) ? 0 : index;
    }
    public String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy  HH:mm", new Locale("id"));
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }
    public void endQuiz(String userId, String quizId, Map<String, Boolean> userAnswers) {
        int finalScore = calculateTotalScore();
        String classification = classifyScore(finalScore); // Get the classification
        String date = getCurrentDate(); // Get the current date

        Log.d("QuizViewModel", "Final score calculated: " + finalScore);
        Log.d("QuizViewModel", "Score classification: " + classification);

        repository.storeAnswers(userId, quizId, userAnswers, finalScore, classification, date, new QuizRepository.OnStoreAnswersCompleteListener() {
            @Override
            public void onSuccess() {
                quizCompleted.setValue(true); // Update quiz completion status
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue(error); // Handle error if storing answers fails
            }
        });
    }
}





