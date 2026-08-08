package com.alya.aplikasilansia.ui.news;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.NewsRepository;

import java.util.List;

public class NewsViewModel extends ViewModel {
    private NewsRepository newsRepository;
    private MutableLiveData<List<News>> newsLiveData;
    public MutableLiveData<String> errorLiveData;

    public NewsViewModel() {
        newsRepository = new NewsRepository();
        newsLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        fetchAllNews();
    }

    public MutableLiveData<List<News>> getNewsLiveData() {
        return newsLiveData;
    }

    private void fetchAllNews() {
        newsLiveData = newsRepository.fetchAllNews();
    }
}
