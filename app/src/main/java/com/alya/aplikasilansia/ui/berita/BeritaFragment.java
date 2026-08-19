package com.alya.aplikasilansia.ui.berita;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alya.aplikasilansia.R;
import com.alya.aplikasilansia.ui.news.NewsContentActivity;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class BeritaFragment extends Fragment implements BeritaAdapter.OnItemClickListener {

    private BeritaViewModel beritaViewModel;
    private BeritaAdapter adapter;

    private RecyclerView rvBerita;
    private ChipGroup chipGroupFilter;
    private TextView tvEmptyState;

    public BeritaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beritaViewModel = new ViewModelProvider(this).get(BeritaViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_berita, container, false);

        rvBerita = view.findViewById(R.id.rv_berita_list);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        setupRecyclerView();
        setupFilterChips();
        observeViewModel();

        return view;
    }

    private void setupRecyclerView() {
        rvBerita.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new BeritaAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this);
        rvBerita.setAdapter(adapter);
    }

    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipBerita) {
                beritaViewModel.setFilter(BeritaViewModel.Filter.BERITA);
            } else if (checkedId == R.id.chipTips) {
                beritaViewModel.setFilter(BeritaViewModel.Filter.TIPS);
            } else {
                beritaViewModel.setFilter(BeritaViewModel.Filter.SEMUA);
            }
        });
    }

    private void observeViewModel() {
        beritaViewModel.getFilteredLiveData().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                rvBerita.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            } else {
                rvBerita.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
                adapter.updateList(list);
            }
        });
    }

    @Override
    public void onItemClick(BeritaItem item) {
        // Reuse NewsContentActivity yang sudah ada untuk halaman detail,
        // baik untuk item Berita maupun Tips (source dikosongkan utk Tips)
        Intent intent = new Intent(getActivity(), NewsContentActivity.class);
        intent.putExtra("news_name", item.getTitle());
        intent.putExtra("news_date", item.getDate());
        intent.putExtra("news_category", item.getCategory());
        intent.putExtra("news_source", item.getSource()); // null utk Tips, sudah di-handle NewsContentActivity
        intent.putExtra("news_image", item.getImage() != null ? item.getImage().toString() : "");
        intent.putExtra("news_content", item.getContent());
        startActivity(intent);
    }
}