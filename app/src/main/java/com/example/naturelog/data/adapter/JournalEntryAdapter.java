package com.example.naturelog.data.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.naturelog.R;
import com.example.naturelog.data.db.JournalEntry;

import java.util.ArrayList;
import java.util.List;

public class JournalEntryAdapter extends RecyclerView.Adapter<JournalEntryAdapter.EntryViewHolder> {

    private List<JournalEntry> entries = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(JournalEntry entry);
        void onItemLongClick(JournalEntry entry);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public JournalEntry getEntryAt(int position) {
        return entries.get(position);
    }


    public void setEntries(List<JournalEntry> newEntries) {
        entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_journal_entry, parent, false);  // ✅ Using custom layout
        return new EntryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView tvSpecies, tvDate;

        EntryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSpecies = itemView.findViewById(R.id.tvSpecies);
            tvDate = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(JournalEntry entry) {
            tvSpecies.setText(entry.speciesName);
            tvDate.setText(entry.timestamp);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(entry);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onItemLongClick(entry);
                    return true;
                }
                return false;
            });
        }
    }
}
