package com.alya.aplikasilansia.ui.newreminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;

import java.util.List;

public class IconReminderAdapter extends RecyclerView.Adapter<IconReminderAdapter.ImageViewHolder> {
    private Context mContext;
    private List<IconReminder> mList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public IconReminderAdapter(Context context, List<IconReminder> iconList){
        mContext = context;
        mList = iconList;
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_icon_reminder, parent, false);
        return new ImageViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull IconReminderAdapter.ImageViewHolder holder, int position) {
        IconReminder currentItem = mList.get(position);
        holder.mImageView.setImageResource(currentItem.getSrcIcon());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;

        public ImageViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.item_icon_reminder);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

}
