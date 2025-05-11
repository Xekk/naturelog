package com.example.naturelog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.naturelog.data.db.AppDatabase;
import com.example.naturelog.data.db.JournalEntry;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.Executors;

public class EntryDetailActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView speciesNameText, sciNameText, summaryText, timestampText;
    private double latitude, longitude;
    private JournalEntry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);

        imageView = findViewById(R.id.imageView);
        speciesNameText = findViewById(R.id.speciesNameText);
        sciNameText = findViewById(R.id.sciNameText);
        summaryText = findViewById(R.id.summaryText);
        timestampText = findViewById(R.id.timestampText);

        int entryId = getIntent().getIntExtra("entry_id", -1);

        Log.d("EntryDetailActivity", "Received entry_id: " + entryId);
        if (entryId == -1) {
            Toast.makeText(this, "Invalid entry ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (entryId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                JournalEntry entry = AppDatabase.getInstance(this).journalEntryDao().getEntryById(entryId);
                runOnUiThread(() -> populateEntry(entry));
            });
        }
    }

    private void populateEntry(JournalEntry entry) {
        speciesNameText.setText(entry.speciesName);
        sciNameText.setText(entry.scientificName);
        summaryText.setText(entry.wikipediaSummary);
        String locationText = String.format(Locale.getDefault(),
                "Date: %s\nLocation: lat: %.5f, long: %.5f",
                entry.timestamp, entry.latitude, entry.longitude);

        timestampText.setText(locationText);

        File imageFile = new File(entry.photoPath);
        if (imageFile.exists()) {
            imageView.setImageURI(Uri.fromFile(imageFile));
        }

        latitude = entry.latitude;
        longitude = entry.longitude;

        this.entry = entry; // Save entry for use in button click

        Button btnSearchWeb = findViewById(R.id.btnSearchWeb);
        btnSearchWeb.setOnClickListener(v -> {
            String query = entry.speciesName != null && !entry.speciesName.isEmpty()
                    ? entry.speciesName
                    : entry.scientificName;

            if (query != null && !query.isEmpty()) {
                String searchUrl = "https://www.google.com/search?q=" + Uri.encode(query);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "No search term available", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnOpenMap = findViewById(R.id.btnOpenMap);
        btnOpenMap.setOnClickListener(v -> {
            String mapUrl = String.format(Locale.getDefault(),
                    "https://www.google.com/maps?q=%.5f,%.5f", latitude, longitude);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
            startActivity(browserIntent);

        });





    }
}

