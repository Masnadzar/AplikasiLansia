package com.alya.aplikasilansia.ui.reminder;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;

// BARU: halaman "Riwayat Pengingat" -- menampilkan seluruh reminder yang SUDAH
// pernah trigger/bunyi, diurutkan dari yang paling baru. Diakses dari ReminderActivity
// lewat tombol "Riwayat Pengingat".
public class ReminderHistoryActivity extends AppCompatActivity {

    private ReminderHistoryViewModel viewModel;
    private ReminderHistoryAdapter adapter;
    private RecyclerView rvHistory;
    private TextView tvNoHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_history);

        findViewById(R.id.btn_back_reminder_history).setOnClickListener(v -> finish());

        tvNoHistory = findViewById(R.id.tv_no_history);
        rvHistory = findViewById(R.id.rv_reminder_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderHistoryAdapter();
        rvHistory.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ReminderHistoryViewModel.class);
        viewModel.getHistoryLiveData().observe(this, histories -> {
            if (histories != null && !histories.isEmpty()) {
                tvNoHistory.setVisibility(View.GONE);
                rvHistory.setVisibility(View.VISIBLE);
                adapter.updateList(histories);
            } else {
                tvNoHistory.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh setiap kali halaman dibuka lagi, supaya reminder yang baru saja
        // trigger saat halaman ini tidak sedang dibuka tetap ikut tampil.
        if (viewModel != null) {
            viewModel.fetchHistory();
        }
    }
}