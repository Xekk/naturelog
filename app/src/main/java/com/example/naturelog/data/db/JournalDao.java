package com.example.naturelog.data.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface JournalDao {

    @Insert
    void insert(JournalEntry entry);

    @Query("SELECT * FROM journal_entries ORDER BY id ASC")
    List<JournalEntry> getAllEntries();

    @Query("SELECT * FROM journal_entries WHERE id = :entryId LIMIT 1")
    JournalEntry getEntryById(int entryId);  // 👈 This is the one you need

    @Delete
    void delete(JournalEntry entry);
}
