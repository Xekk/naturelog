package com.example.naturelog.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "journal_entries")
public class JournalEntry {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String speciesName;
    public String scientificName;
    public String wikipediaSummary;
    public String photoPath; //field for image
    public double latitude;
    public double longitude;
    public String timestamp;
}
