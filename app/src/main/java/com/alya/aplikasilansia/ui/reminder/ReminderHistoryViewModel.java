package com.alya.aplikasilansia.ui.reminder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.ReminderHistory;
import com.alya.aplikasilansia.data.ReminderRepository;

import java.util.List;

// BARU: ViewModel khusus untuk halaman "Riwayat Pengingat".
// Dipisah dari ReminderViewModel supaya tidak mencampur logika reminder yang
// masih terjadwal (akan datang) dengan reminder yang sudah pernah bunyi (riwayat).
public class ReminderHistoryViewModel extends ViewModel {
    private final ReminderRepository reminderRepository;
    private final MutableLiveData<List<ReminderHistory>> historyLiveData;

    public ReminderHistoryViewModel() {
        reminderRepository = new ReminderRepository();
        historyLiveData = new MutableLiveData<>();
        fetchHistory();
    }

    public LiveData<List<ReminderHistory>> getHistoryLiveData() {
        return historyLiveData;
    }

    public void fetchHistory() {
        reminderRepository.fetchReminderHistory().observeForever(historyLiveData::setValue);
    }
}