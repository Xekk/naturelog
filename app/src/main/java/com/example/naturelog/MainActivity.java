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

//        Button btnOpenINat = findViewById(R.id.btnOpenINat);
//        Button btnPasteToken = findViewById(R.id.btnPasteToken);
//
//        btnOpenINat.setOnClickListener(v -> {
//            String url = "https://www.inaturalist.org/users/api_token";
//            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            startActivity(browserIntent);
//        });
//
//        btnPasteToken.setOnClickListener(v -> {
//            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//            if (clipboard != null && clipboard.hasPrimaryClip()) {
//                ClipData clip = clipboard.getPrimaryClip();
//                if (clip != null && clip.getItemCount() > 0) {
//                    CharSequence pasted = clip.getItemAt(0).coerceToText(this);
//                    if (pasted != null && pasted.toString().startsWith("Bearer ")) {
//                        saveToken(pasted.toString());
//                        Toast.makeText(this, "Token saved successfully!", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(this, "Clipboard doesn't contain a valid token", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });




    }
}
