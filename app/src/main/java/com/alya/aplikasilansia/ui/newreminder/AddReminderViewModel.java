package com.alya.aplikasilansia.ui.newreminder;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.ReminderRepository;
import com.google.firebase.auth.FirebaseUser;

public class AddReminderViewModel extends ViewModel {
    private ReminderRepository reminderRepository;
    public MutableLiveData<FirebaseUser> reminderLiveData;
    public MutableLiveData<String> errorLiveData;
    public MutableLiveData<String> updateResultLiveData;

    public AddReminderViewModel() {
        reminderRepository = new ReminderRepository();
        reminderLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        updateResultLiveData = new MutableLiveData<>();
    }

    public interface OnReminderCreatedListener {
        void onCreated(String reminderId);
    }

    // DIUBAH: tambah parameter listener untuk meneruskan reminderId ke Activity
    public void createReminder(String title, String day, String time, String desc, String timestamp, Integer icon, OnReminderCreatedListener listener){
        reminderRepository.createReminder(title, day, time, desc, timestamp, icon, reminderLiveData, errorLiveData,
                reminderId -> {
                    if (listener != null) listener.onCreated(reminderId);
                });
    }
}