package com.example.naturelog;

import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TokenGuideActivity extends AppCompatActivity {

    private EditText tokenInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_guide);

        tokenInput = findViewById(R.id.tokenInput);
        TextView stepText = findViewById(R.id.stepText);

        stepText.setText("Step-by-step:\n\n" +
                "1. Log in to https://www.inaturalist.org in your browser.\n" +
                "2. Visit https://www.inaturalist.org/users/api_token.\n" +
                "3. Copy the API token shown.\n" +
                "4. Paste it below and save.");

        Button btnOpenSite = findViewById(R.id.btnOpenSite);
        btnOpenSite.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.inaturalist.org/users/api_token"));
            startActivity(browserIntent);
        });

        Button btnPaste = findViewById(R.id.btnPaste);
        btnPaste.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClipDescription().hasMimeType("text/plain")) {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                tokenInput.setText(item.getText());
            } else {
                Toast.makeText(this, "Clipboard is empty or not plain text.", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnSaveToken = findViewById(R.id.btnSaveToken);
        btnSaveToken.setOnClickListener(v -> {
            String token = tokenInput.getText().toString().trim();
            if (!token.isEmpty()) {
                getSharedPreferences("inaturalist_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("api_token", token)
                        .putBoolean("token_invalid", false) // Reset any invalid flag
                        .apply();
                Toast.makeText(this, "Token saved!", Toast.LENGTH_SHORT).show();
                finish(); // Close activity after saving
            } else {
                Toast.makeText(this, "Please enter a valid token.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
