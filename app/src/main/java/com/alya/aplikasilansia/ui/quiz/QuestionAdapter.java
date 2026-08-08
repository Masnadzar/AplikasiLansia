package com.alya.aplikasilansia.ui.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {
    private List<ItemQuestionModel> questions;

    private final OnQuestionClickListener onQuestionClickListener;

    public interface OnQuestionClickListener {
        void onQuestionClick(int position);
    }

    public QuestionAdapter(OnQuestionClickListener onQuestionClickListener) {
        this.questions = new ArrayList<>();
        this.onQuestionClickListener = onQuestionClickListener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.number_item, parent, false);
        return new QuestionViewHolder(view, onQuestionClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        holder.buttonQuestion.setText(String.valueOf(position + 1)); // This line sets the question number

        // Changing color if isQuestionAnswered are true
        if (questions.get(position).isQuestionAnswered()) {
            holder.buttonQuestion.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.buttonQuestion.setBackgroundTintList(ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.blue1));
        }
    }
    @Override
    public int getItemCount() {
        return questions.size();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public Button buttonQuestion;
        OnQuestionClickListener onQuestionClickListener;

        public QuestionViewHolder(@NonNull View itemView, OnQuestionClickListener onQuestionClickListener) {
            super(itemView);
            buttonQuestion = itemView.findViewById(R.id.buttonQuestion);
            this.onQuestionClickListener = onQuestionClickListener;
            buttonQuestion.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onQuestionClickListener.onQuestionClick(getAdapterPosition());
        }
    }

    public void updateQuestionList(List<ItemQuestionModel> newQuestionList) {
        final QuestionDiffCallback diffCallback = new QuestionDiffCallback(questions, newQuestionList);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        questions.clear();
        questions.addAll(newQuestionList);
        diffResult.dispatchUpdatesTo(this);
    }

    // Class for comparing is there something new in item recycler view or not
    public class QuestionDiffCallback extends DiffUtil.Callback {
        private final List<ItemQuestionModel> mOldQuestionList;
        private final List<ItemQuestionModel> mNewQuestionList;

        public QuestionDiffCallback(List<ItemQuestionModel> mOldQuestionList, List<ItemQuestionModel> mNewQuestionList) {
            this.mOldQuestionList = mOldQuestionList;
            this.mNewQuestionList = mNewQuestionList;
        }

        @Override
        public int getOldListSize() {
            return mOldQuestionList.size();
        }

        @Override
        public int getNewListSize() {
            return mNewQuestionList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldQuestionList.get(oldItemPosition).getId() == mNewQuestionList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            final ItemQuestionModel oldQuestionItem = mOldQuestionList.get(oldItemPosition);
            final ItemQuestionModel newQuestionItem = mNewQuestionList.get(newItemPosition);

            return oldQuestionItem.getId() == newQuestionItem.getId() && oldQuestionItem.isQuestionAnswered() == newQuestionItem.isQuestionAnswered();
        }
    }
}

