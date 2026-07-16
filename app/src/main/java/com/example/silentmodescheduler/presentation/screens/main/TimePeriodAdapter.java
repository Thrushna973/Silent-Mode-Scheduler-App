package com.example.silentmodescheduler.presentation.screens.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.silentmodescheduler.data.model.TimePeriod;
import com.example.silentmodescheduler.databinding.ItemTimePeriodBinding;
import java.util.ArrayList;
import java.util.List;

public class TimePeriodAdapter extends RecyclerView.Adapter<TimePeriodAdapter.ViewHolder> {
    private final List<TimePeriod> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(TimePeriod item);
        void onDeleteClick(TimePeriod item);
    }

    public TimePeriodAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateItems(List<TimePeriod> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimePeriodBinding binding = ItemTimePeriodBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTimePeriodBinding binding;

        ViewHolder(ItemTimePeriodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(TimePeriod item) {
            binding.tvPeriodName.setText(item.getName());
            binding.tvPeriodTime.setText(item.getStartTime() + " - " + item.getEndTime());
            binding.btnEdit.setOnClickListener(v -> listener.onEditClick(item));
            binding.btnDelete.setOnClickListener(v -> listener.onDeleteClick(item));
        }
    }
}
