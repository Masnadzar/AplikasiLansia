package com.alya.aplikasilansia.ui.healthcare;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.HealthCareRepository;

import java.util.List;

public class HealthCareViewModel extends ViewModel {
    private HealthCareRepository healthCareRepository;
    private MutableLiveData<List<HealthCare>> healthCareLiveData;
    public MutableLiveData<String> errorLiveData;

    public HealthCareViewModel() {
        healthCareRepository = new HealthCareRepository();
        healthCareLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        fetchAll();
    }

    public MutableLiveData<List<HealthCare>> getHealthCareLiveData() {
        return healthCareLiveData;
    }

    private void fetchAll() {
        healthCareLiveData = healthCareRepository.fetchHealthCare();
    }
}
