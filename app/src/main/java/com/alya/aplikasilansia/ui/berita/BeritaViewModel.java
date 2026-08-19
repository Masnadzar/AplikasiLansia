package com.alya.aplikasilansia.ui.berita;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.alya.aplikasilansia.data.NewsRepository;
import com.alya.aplikasilansia.data.TipsRepository;
import com.alya.aplikasilansia.ui.news.News;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BeritaViewModel extends ViewModel {

    public enum Filter {
        SEMUA, BERITA, TIPS
    }

    private final NewsRepository newsRepository;
    private final TipsRepository tipsRepository;

    private final MutableLiveData<List<News>> newsSource;
    private final MutableLiveData<List<Tips>> tipsSource;

    private List<News> newsCache = new ArrayList<>();
    private List<Tips> tipsCache = new ArrayList<>();
    private List<BeritaItem> lastCombined = new ArrayList<>();

    // list yang sudah digabung & difilter, ini yang di-observe oleh Fragment
    private final MutableLiveData<List<BeritaItem>> filteredLiveData = new MutableLiveData<>();

    public final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private Filter currentFilter = Filter.SEMUA;

    private final Observer<List<News>> newsObserver = newsList -> {
        newsCache = (newsList != null) ? newsList : new ArrayList<>();
        recombine();
    };

    private final Observer<List<Tips>> tipsObserver = tipsList -> {
        tipsCache = (tipsList != null) ? tipsList : new ArrayList<>();
        recombine();
    };

    public BeritaViewModel() {
        newsRepository = new NewsRepository();
        tipsRepository = new TipsRepository();

        newsSource = newsRepository.fetchAllNews();
        tipsSource = tipsRepository.fetchAllTips();

        // observeForever WAJIB dipakai di ViewModel (bukan Fragment/Activity)
        // karena ViewModel tidak punya LifecycleOwner. Wajib di-removeObserver
        // di onCleared() supaya tidak leak.
        newsSource.observeForever(newsObserver);
        tipsSource.observeForever(tipsObserver);
    }

    private void recombine() {
        List<BeritaItem> combined = new ArrayList<>();
        for (News n : newsCache) {
            combined.add(BeritaItem.fromNews(n));
        }
        for (Tips t : tipsCache) {
            combined.add(BeritaItem.fromTips(t));
        }

        // urutkan dari terbaru (asumsi field "date" bisa dibandingkan sebagai
        // string sortable; kalau format tanggal News & Tips beda, sorting
        // bisa kurang akurat -- bisa distandarkan pakai Firestore Timestamp
        // kalau dibutuhkan presisi lebih)
        Collections.sort(combined, new Comparator<BeritaItem>() {
            @Override
            public int compare(BeritaItem a, BeritaItem b) {
                if (a.getDate() == null || b.getDate() == null) return 0;
                return b.getDate().compareTo(a.getDate());
            }
        });

        applyFilter(combined);
    }

    private void applyFilter(List<BeritaItem> source) {
        lastCombined = source;
        List<BeritaItem> result = new ArrayList<>();
        for (BeritaItem item : source) {
            switch (currentFilter) {
                case BERITA:
                    if (item.getType() == BeritaItem.Type.NEWS) result.add(item);
                    break;
                case TIPS:
                    if (item.getType() == BeritaItem.Type.TIPS) result.add(item);
                    break;
                case SEMUA:
                default:
                    result.add(item);
                    break;
            }
        }
        filteredLiveData.setValue(result);
    }

    public void setFilter(Filter filter) {
        this.currentFilter = filter;
        applyFilter(lastCombined);
    }

    public MutableLiveData<List<BeritaItem>> getFilteredLiveData() {
        return filteredLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        newsSource.removeObserver(newsObserver);
        tipsSource.removeObserver(tipsObserver);
    }
}