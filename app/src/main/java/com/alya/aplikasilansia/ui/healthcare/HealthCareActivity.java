package com.alya.aplikasilansia.ui.healthcare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.LoginActivity;
import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.ui.reminder.CustomSpinnerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HealthCareActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "HealthCareActivity";
    private FirebaseAuth mAuth;
    HealthCareViewModel healthCareViewModel;
    HealthCareAdapter adapter;
    private TextView tvPilihKota;
    private String selectedFilter;
    private Spinner spinnerHealthCare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_care);

        mAuth = FirebaseAuth.getInstance();
        healthCareViewModel = new ViewModelProvider(this).get(HealthCareViewModel.class);
        if (mAuth.getCurrentUser() == null) {
            // User is not logged in, redirect to LoginActivity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close RestrictedActivity
        } else {
            Button backFromHealtnBtn = findViewById(R.id.btn_back_healthcare);
            backFromHealtnBtn.setOnClickListener(this);
            spinnerHealthCare = findViewById(R.id.healthcare_searchbar);
            tvPilihKota = findViewById(R.id.tv_pilih_kota);
            tvPilihKota.setVisibility(View.GONE);
            spinnerHealthBar();
            setupHealthRecyclerView();
            getData();
        }
    }

    private void getData() {
        try {
            healthCareViewModel.getHealthCareLiveData().observe(this, healthCares -> {
                if (healthCares != null && !healthCares.isEmpty()) {
                    Log.d("HealthCareActivity", "Fetched healthcare data: " + healthCares);
                    List<HealthCare> filteredHealthCares = filterHealthCareData(healthCares);
                    if (filteredHealthCares != null && !filteredHealthCares.isEmpty()) {
                        adapter.updateList(filteredHealthCares);
                        Log.d("HealthCareActivity", "Filtered healthcare data available: " + filteredHealthCares.size() + " items");
                    } else {
                        Log.e("HealthCareActivity", "No filtered healthcare data available");
                    }
                } else {
                    Log.e("HealthCareActivity", "HealthCare data is null or empty");
                }
            });
        } catch (Exception e) {
            Log.e("HealthCareActivity", "Error fetching healthcare data", e);
        }
    }

    private List<HealthCare> filterHealthCareData(List<HealthCare> healthCares) {
        List<HealthCare> items = new ArrayList<>();
        try {
            for (HealthCare healthCare : healthCares) {
                if (healthCare != null) {
                    String city = healthCare.getCity();
                    Log.d("HealthCareActivity", "HealthCare Item: " + healthCare + ", City: " + city);
//                    Log.d("HealthCareActivity", "HealthCare Item: " + healthCare);
                    if (selectedFilter.equals(healthCare.getCity())) {
                        items.add(healthCare);
                    } else if (selectedFilter.equals("Pilih Kota Anda")) {
                        tvPilihKota.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e("HealthCareActivity", "HealthCare Item is null");
                }
            }
        } catch (Exception e) {
            Log.e("HealthCareActivity", "Error filtering healthcare data", e);
        }

        if (items.isEmpty()) {
            Log.e("HealthCareActivity", "No data found for the selected filter: " + selectedFilter);
        } else {
            Log.d("HealthCareActivity", "Data found for the selected filter: " + items.size() + " items");
        }

        return items;
    }


    private void setupHealthRecyclerView() {
        try {
            RecyclerView recyclerView = findViewById(R.id.rv_health_care);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new HealthCareAdapter(new ArrayList<>(), this);
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
            Toast.makeText(this, "Error setting up list view", Toast.LENGTH_SHORT).show();
        }
    }

    public void spinnerHealthBar(){
        String[] filterHealthCare = getResources().getStringArray(R.array.health_care);
        CustomSpinnerAdapter spinnerAdapter = new CustomSpinnerAdapter(
                this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.health_care)
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHealthCare.setAdapter(spinnerAdapter);
        spinnerHealthCare.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = filterHealthCare[position];
                getData();
                Log.e("HealthCareActivity", "FILTER: " +selectedFilter);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void onClick(View v){
        if (v.getId() == R.id.btn_back_healthcare) {
            finish();
        }
    }
}