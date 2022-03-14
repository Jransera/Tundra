package com.example.tundra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RankListAdapter extends RecyclerView.Adapter<RecyclerViewHolder> {
    private List<rank<Integer,Long>> rankings;

    public RankListAdapter(List<rank<Integer,Long>> lst) {
        this.rankings = lst;
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.frame_textview;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        rank<Integer,Long> curr = rankings.get(position);
        Long ms = curr.getR();
        Long days = ms/86400000;
        Long hours = ms/3600000 - days * 24;
        Long minutes = ms/60000 - days * 1440 - hours * 60;
        holder.getView().setText("Rank " + curr.getL() + ": " + days + "Days " + hours + "Hours " + minutes + "Minutes");
    }

    @Override
    public int getItemCount() {
        return rankings.size();
    }
}
