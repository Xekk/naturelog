package com.example.naturelog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnScan, btnJournal;

    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences("inaturalist_prefs", MODE_PRIVATE);
        prefs.edit().putString("api_token", token).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = findViewById(R.id.btnScan);
        btnJournal = findViewById(R.id.btnJournal);
        Button btnGetToken = findViewById(R.id.btnGetToken);

        btnGetToken.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TokenGuideActivity.class);
            startActivity(intent);
        });




        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ScanActivity.class));
            }
        });

        btnJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, JournalActivity.class));
            }
        });


    }
}
