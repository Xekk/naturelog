package com.example.naturelog;

import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private WebView webView;
    private static final String INATURALIST_TOKEN_URL = "https://www.inaturalist.org/users/api_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        webView = findViewById(R.id.webViewLogin);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(INATURALIST_TOKEN_URL);

        findViewById(R.id.btnPasteToken).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip() && clipboard.getPrimaryClip().getItemCount() > 0) {
                CharSequence clipText = clipboard.getPrimaryClip().getItemAt(0).getText();
                if (clipText != null && clipText.toString().startsWith("Bearer")) {
                    saveToken(clipText.toString());
                } else {
                    Toast.makeText(this, "No valid token found in clipboard", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveToken(String token) {
        // Save to SharedPreferences or EncryptedSharedPreferences
        getSharedPreferences("secrets", MODE_PRIVATE)
                .edit()
                .putString("api_token", token)
                .apply();

        Toast.makeText(this, "Token saved!", Toast.LENGTH_SHORT).show();

        // Navigate back or forward
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

