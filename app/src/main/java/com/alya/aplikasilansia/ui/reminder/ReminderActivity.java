package com.alya.aplikasilansia.ui.reminder;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.LoginActivity;
import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.data.Reminder;
import com.alya.aplikasilansia.ui.newreminder.AddReminderActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity implements View.OnClickListener, DeleteReminderFragment.OnReminderDeletedListener {

    Spinner filterSpinner;
    RecyclerView filteredReminderRV;
    FilteredReminderAdapter adapter;
    private Reminder firstTodayReminder; // Define as a member variable
    ReminderViewModel reminderViewModel;
    private TextView tvReminderName, tvReminderDate, tvNoData, tvOnGoing;
    private ImageView ImgReminder;
    private String selectedFilter, reminderId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Button btnFromReminder = findViewById(R.id.btn_back_reminder);
            btnFromReminder.setOnClickListener(this);

            Button btnCreateReminder = findViewById(R.id.btn_add_reminder);
            btnCreateReminder.setOnClickListener(this);

            filterSpinner = findViewById(R.id.dropdown_reminder_filter);
            setupSpinner(filterSpinner);

            filteredReminderRecyclerView();

            reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);

            tvOnGoing = findViewById(R.id.tvOngoingReminder);
            tvReminderName = findViewById(R.id.tv_reminder_name);
            tvReminderDate = findViewById(R.id.tv_reminder_date);
            ImgReminder = findViewById(R.id.img_reminder);
            tvNoData = findViewById(R.id.tv_no_data);
            tvNoData.setVisibility(View.GONE);

            filterData();
            getFirstReminder();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back_reminder) {
            finish();
        } else if (v.getId() == R.id.btn_add_reminder) {
            Intent intent = new Intent(ReminderActivity.this, AddReminderActivity.class);
            startActivity(intent);
            finish();
        }
    }
    private void getFirstReminder() {
        reminderViewModel.getFirstReminderLiveData().observe(this, firstReminder -> {
            if (firstReminder != null) {
                updateFirstTodayReminderUI(firstReminder);
            }
        });
    }
    private void filterData() {
        reminderViewModel.getReminderLiveData().observe(this, reminders -> {
            if (reminders != null) {
                tvNoData.setVisibility(View.GONE);
                List<Object> filteredItems = filterReminders(reminders);
                adapter.updateList(filteredItems);
                if (reminders.size() == 0) {
                    String textOnGoing = "Belum ada pengingat";
                    String textName = "Buat Pengingatmu!";
                    String textDate = "Belum ada pengingat terjadwal.";
                    tvOnGoing.setText(textOnGoing);
                    tvReminderName.setText(textName);
                    tvReminderDate.setText(textDate);
                    ImgReminder.setImageResource(R.drawable.ic_remind_pumpkin);
                }
            } else {

                Toast.makeText(ReminderActivity.this, "No data fetched", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private List<Object> filterReminders(List<Reminder> reminders) {
        List<Object> items = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Sort reminders by date
        reminders.sort((r1, r2) -> {
            try {
                Date date1 = sdf.parse(r1.getTimestamp());
                Date date2 = sdf.parse(r2.getTimestamp());
                return date1.compareTo(date2);
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + e.getMessage());
                return 0;
            }
        });

        // Log the sorted reminders to verify sorting
        for (Reminder reminder : reminders) {
            Log.d(TAG, "Reminder: " + reminder.getTitle() + " - " + reminder.getTimestamp());
        }
        // Initialize date comparisons
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        boolean addedTodayHeader = false;
        boolean addedTomorrowHeader = false;
        boolean addedUpcomingHeader = false;
        boolean itemsAdded = false;

        for (Reminder reminder : reminders) {
            try {
                Date reminderDate = sdf.parse(reminder.getTimestamp());
                Calendar reminderCalendar = Calendar.getInstance();
                reminderCalendar.setTime(reminderDate);

                if (firstTodayReminder == null) {
                    if (isSameDay(today, reminderCalendar)) {
                        if (reminderCalendar.after(Calendar.getInstance())) {
                            firstTodayReminder = reminder;
                            Log.d(TAG, "First today reminder set: " + firstTodayReminder.getTitle());                        }
                            continue;
                    }
                }

                if (selectedFilter == null || selectedFilter.equals("Semua Pengingat")) {
                    if (isSameDay(today, reminderCalendar)) {
                        if (!addedTodayHeader) {
                            items.add("Hari Ini");
                            addedTodayHeader = true;
                        }
                    } else if (isSameDay(tomorrow, reminderCalendar)) {
                        if (!addedTomorrowHeader) {
                            items.add("Besok");
                            addedTomorrowHeader = true;
                        }
                    } else if (reminderCalendar.after(tomorrow)) {
                        if (!addedUpcomingHeader) {
                            items.add("Yang Akan Datang");
                            addedUpcomingHeader = true;
                        }
                    }

                    items.add(reminder);
                    itemsAdded = true;


                } else if (selectedFilter.equals("Hari Ini") && isSameDay(today, reminderCalendar)) {
                    if (reminderCalendar.after(Calendar.getInstance())) {
                            if (!addedTodayHeader) {
                                items.add("Hari Ini");
                                addedTodayHeader = true;
                            }
                        items.add(reminder);
                        itemsAdded = true;
                        }


                } else if (selectedFilter.equals("Besok") && isSameDay(tomorrow, reminderCalendar)) {
                    if (!addedTomorrowHeader) {
                        items.add("Besok");
                        addedTomorrowHeader = true;
                    }
                    items.add(reminder);
                    itemsAdded = true;

                } else if (selectedFilter.equals("Yang Akan Datang") && reminderCalendar.after(tomorrow)) {
                    if (!addedUpcomingHeader) {
                        items.add("Yang Akan Datang");
                        addedUpcomingHeader = true;
                    }
                    items.add(reminder);
                    itemsAdded = true;

                }


            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String noDataText = "Tidak ada pengingat terjadwal";
        String noDataFilter = "Tidak ada hasil yang ditemukan untuk filter yang dipilih";
        if (!itemsAdded) {
            if (selectedFilter == null || selectedFilter.equals("Semua Pengingat")) {
                tvNoData.setText(noDataText);
            } else if (selectedFilter.equals("Hari Ini")) {
                tvNoData.setText(noDataFilter);
            } else if (selectedFilter.equals("Besok")) {
                tvNoData.setText(noDataFilter);
            } else if (selectedFilter.equals("Yang Akan Datang")) {
                tvNoData.setText(noDataFilter);
            }
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            tvNoData.setVisibility(View.GONE);
        }

        return items;
    }

    private void updateFirstTodayReminderUI(Reminder firstTodayReminder) {
        if (firstTodayReminder != null) {
            Log.d(TAG, "Updating UI with first today reminder: " + firstTodayReminder.getTitle());
            tvReminderName.setText(firstTodayReminder.getTitle());
            tvReminderDate.setText(formatDate(firstTodayReminder.getTimestamp()));
            ImgReminder.setImageResource(firstTodayReminder.getIcon());
        } else {
            Log.d(TAG, "No first today reminder to update UI with");
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isTomorrow(Calendar cal1, Calendar cal2) {
        Calendar tomorrow = (Calendar) cal1.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        return tomorrow.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }


    private void setupSpinner(Spinner spinner) {
        String[] filterReminder = getResources().getStringArray(R.array.filter_reminder);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterReminder);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        filterSpinner.setAdapter(spinnerAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected item and filter the data
                selectedFilter = filterReminder[position];
                filterData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
//                filterData(selectedFilter);
            }

        });
    }
    private String formatDate(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("'Hari ini pukul' HH:mm", new Locale("id", "ID"));
        SimpleDateFormat tomorrowFormat = new SimpleDateFormat("'Besok pukul' HH:mm", new Locale("id", "ID"));
        SimpleDateFormat normalFormat = new SimpleDateFormat("EEEE, d MMMM 'pukul' HH:mm", new Locale("id", "ID"));

        try {
            Date date = sdf.parse(timestamp);
            Calendar calendar = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            calendar.setTime(date);

            if (isSameDay(calendar, today)) {
                return outputFormat.format(date);
            } else {
                Calendar tomorrow = (Calendar) today.clone();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                if (isSameDay(calendar, tomorrow)) {
                    return tomorrowFormat.format(date);
                } else {
                    return normalFormat.format(date);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return timestamp;
        }
    }

    private void filteredReminderRecyclerView() {
        filteredReminderRV = findViewById(R.id.rv_filtered_reminder);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        filteredReminderRV.setLayoutManager(layoutManager);
        adapter = new FilteredReminderAdapter(new ArrayList<>(), this); // Initialize with an empty list
        Log.d("ReminderActivity", "Adapter initialized: " + (adapter != null));
        filteredReminderRV.setAdapter(adapter);

    }

    @Override
    public void onReminderDeleted(String reminderId) {
        adapter.removeReminder(reminderId);
        reminderViewModel.deleteReminderData(reminderId);
        Log.d("ReminderActivity", "Reminder deleted, refreshing data");
        filterData();
        getFirstReminder();
        Log.d("ReminderActivity", "Reminder data refreshed");

    }

}