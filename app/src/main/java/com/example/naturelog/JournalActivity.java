package com.example.naturelog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.naturelog.data.adapter.JournalEntryAdapter;
import com.example.naturelog.data.db.AppDatabase;
import com.example.naturelog.data.db.JournalEntry;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;

public class JournalActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JournalEntryAdapter adapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);
        db = AppDatabase.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewJournal);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        adapter = new JournalEntryAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new JournalEntryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(JournalEntry entry) {
                Intent intent = new Intent(JournalActivity.this, EntryDetailActivity.class);
                intent.putExtra("entry_id", entry.id);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(JournalEntry entry) {
                showDeleteConfirmation(entry);  // 👈 Show delete dialog
            }
        });

        loadJournalEntries();
    }

    private void loadJournalEntries() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<JournalEntry> entries = db.journalEntryDao().getAllEntries();
            runOnUiThread(() -> adapter.setEntries(entries));
        });
    }

    private void showDeleteConfirmation(JournalEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this journal entry?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        // 1. Delete from DB
                        db.journalEntryDao().delete(entry);

                        // 2. Delete associated photo
                        if (entry.photoPath != null) {
                            File photoFile = new File(entry.photoPath);
                            if (photoFile.exists()) {
                                photoFile.delete(); // Don't worry about result, just try it
                            }
                        }

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show();
                            loadJournalEntries();
                        });
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

}
