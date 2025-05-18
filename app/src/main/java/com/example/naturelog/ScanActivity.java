package com.example.naturelog;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.naturelog.network.WikiSummaryResponse;
import com.example.naturelog.network.WikipediaApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.example.naturelog.network.iNaturalistApi;
import com.example.naturelog.network.ScoreResponse;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.naturelog.data.db.AppDatabase;
import com.example.naturelog.data.db.JournalEntry;
import androidx.room.Room;

import java.util.concurrent.Executors;

public class ScanActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;
    private FusedLocationProviderClient fusedLocationClient;

    private double currentLat = 0.0;
    private double currentLon = 0.0;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Uri photoUri;
    private ImageView imgPreview;
    private Button btnTakePhoto;
    private String currentPhotoPath;

    private TextView tvResult;

    private TextView tvLocation;

    private Button btnSearchWeb;

    private ProgressBar progressBar;

    private Button btnSave;

    private boolean photoSavedToJournal = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermissions();
        requestCameraPermission();

        imgPreview = findViewById(R.id.imgPreview);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSearchWeb = findViewById(R.id.btnSearchWeb);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setEnabled(true);
        btnSave.setAlpha(1.0f);
        progressBar = findViewById(R.id.progressBar);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                getUserLocation();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveJournalEntry();
            }
        });


        btnSearchWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvResult != null && tvResult.getText() != null) {
                    String text = tvResult.getText().toString();
                    String[] lines = text.split("\n", 2); // First line contains name
                    String query = lines.length > 0 ? lines[0] : "nature species";

                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q=" + Uri.encode(query)));
                    startActivity(intent);
                } else {
                    Toast.makeText(ScanActivity.this, "Nothing to search yet.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SharedPreferences prefs = getSharedPreferences("inaturalist_prefs", MODE_PRIVATE);
        boolean tokenInvalid = prefs.getBoolean("token_invalid", false);
        if (tokenInvalid) {
            Toast.makeText(this, "Your API token is invalid. Please enter a new one.", Toast.LENGTH_LONG).show();
            new AlertDialog.Builder(this)
                    .setTitle("Invalid Token")
                    .setMessage("Your iNaturalist API token is invalid. Would you like to update it?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        startActivity(new Intent(this, TokenGuideActivity.class));
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnTakePhoto.setEnabled(!isLoading);
        Button btnSearchWeb = findViewById(R.id.btnSearchWeb);
        Button btnSave = findViewById(R.id.btnSave);
        btnSearchWeb.setEnabled(!isLoading);
        btnSave.setEnabled(!isLoading);
    }
    private void requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
      }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return; // Permissions are not granted
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLat = location.getLatitude();
                            currentLon = location.getLongitude();

                            Toast.makeText(ScanActivity.this,
                                    "Location: " + currentLat + ", " + currentLon,
                                    Toast.LENGTH_LONG).show();
                            // Now you can store or use this location
                        } else {
                            Toast.makeText(ScanActivity.this,
                                    "Unable to fetch location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Location permission required!", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Camera permission required!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        if (currentPhotoPath != null && !photoSavedToJournal) {
            File previousPhoto = new File(currentPhotoPath);
            if (previousPhoto.exists()) {
                previousPhoto.delete();
            }
        }

        // Reset state
        photoSavedToJournal = false;
        currentPhotoPath = null;
        btnSave.setAlpha(1.0f);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath(); // ✅ real file path
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imgPreview.setImageURI(photoUri);
            File photoFile = new File(currentPhotoPath); // ✅ correct file

            if (photoFile.exists()) {

                sendImageToApi(photoFile, currentLat, currentLon);
            } else {
                Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show();
            }
            Log.d("ScanActivity", "Photo file exists: " + photoFile.exists() + ", path: " + photoFile.getAbsolutePath());
            sendImageToApi(photoFile, currentLat, currentLon);
        }
    }

    private void fetchWikipediaSummary(String title, String displayName) {
        Retrofit wikiRetrofit = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/api/rest_v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WikipediaApi wikiApi = wikiRetrofit.create(WikipediaApi.class);

        Call<WikiSummaryResponse> call = wikiApi.getSummary(title);

        call.enqueue(new Callback<WikiSummaryResponse>() {
            @Override
            public void onResponse(Call<WikiSummaryResponse> call, Response<WikiSummaryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String summary = response.body().extract;
                    String info = displayName + " (" + title + ")\n\n" + summary;
                    tvResult.setText(info);
                } else {
                    tvResult.setText(displayName + " (" + title + ")\n\nNo summary found.");
                }
            }

            @Override
            public void onFailure(Call<WikiSummaryResponse> call, Throwable t) {
                tvResult.setText(displayName + " (" + title + ")\n\nFailed to fetch summary.");
            }
        });
    }

    private void saveJournalEntry() {
        TextView tvResult = findViewById(R.id.tvResult);
        String resultText = tvResult.getText().toString();

        if (resultText.startsWith("Scan result will")) {
            Toast.makeText(this, "Nothing to save yet!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract common and scientific name
        String[] lines = resultText.split("\n", 2);
        String[] nameParts = lines[0].split(" \\(");
        String speciesName = nameParts[0];
        String scientificName = nameParts.length > 1 ? nameParts[1].replace(")", "") : "";

        JournalEntry entry = new JournalEntry();
        entry.speciesName = speciesName;
        entry.scientificName = scientificName;
        entry.wikipediaSummary = lines.length > 1 ? lines[1] : "";
        entry.photoPath = currentPhotoPath;
        entry.latitude = currentLat;
        entry.longitude = currentLon;
        entry.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.journalEntryDao().insert(entry);


            runOnUiThread(() -> {
                Toast.makeText(this, "Saved to journal!", Toast.LENGTH_SHORT).show();
                Button btnSave = findViewById(R.id.btnSave);
                btnSave.setEnabled(false);
                btnSave.setAlpha(0.5f); // Optional: dim it to show it's inactive

                photoSavedToJournal = true; // mark as saved
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!photoSavedToJournal && currentPhotoPath != null) {
            File photoFile = new File(currentPhotoPath);
            if (photoFile.exists()) {
                photoFile.delete();
            }
        }
    }

    private String getValidApiToken() {
        SharedPreferences prefs = getSharedPreferences("inaturalist_prefs", MODE_PRIVATE);
        String token = prefs.getString("api_token", null);

        if (token == null || prefs.getBoolean("token_invalid", false)) {
            Toast.makeText(this, "Using fallback API token. Please set a valid one.", Toast.LENGTH_SHORT).show();
            token = getString(R.string.inat_token); // fallback
        }

        return token;
    }


    private void sendImageToApi(File imageFile, double lat, double lng) {
        setLoadingState(true);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        String token = getValidApiToken();

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)  // ⏱ connection timeout
                .readTimeout(20, TimeUnit.SECONDS)     // ⏱ max time waiting for server response
                .writeTimeout(20, TimeUnit.SECONDS)    // ⏱ max time for sending data
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + token) // ✅ correct format
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.inaturalist.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        iNaturalistApi api = retrofit.create(iNaturalistApi.class);

        RequestBody requestFile = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestFile);

        Log.d("ScanActivity", "Lat: " + lat + ", Lng: " + lng);
        Toast.makeText(this, "Lat: " + lat + ", Lng: " + lng, Toast.LENGTH_SHORT).show();

        Call<ScoreResponse> call = api.identifySpecies(body, lat, lng);
        call.enqueue(new Callback<ScoreResponse>() {
            @Override
            public void onResponse(Call<ScoreResponse> call, Response<ScoreResponse> response) {
                if (!response.isSuccessful()) {
                    Log.e("ScanActivity", "Response code: " + response.code());
                }
                setLoadingState(false);  // 👈 Hide spinner
                tvResult = findViewById(R.id.tvResult);
                tvLocation = findViewById(R.id.tvLocation);

                if (response.code() == 401) {
                    SharedPreferences prefs = getSharedPreferences("inaturalist_prefs", MODE_PRIVATE);
                    prefs.edit().putBoolean("token_invalid", true).apply();
                    Toast.makeText(ScanActivity.this, "Invalid token. Please update it.", Toast.LENGTH_LONG).show();
                }


                if (response.isSuccessful() && response.body() != null) {
                    ScoreResponse.Result top = response.body().results.get(0);
                    String summary = top.taxon.wikipedia_summary;
                    if (summary == null || summary.isEmpty()) {
                        fetchWikipediaSummary(top.taxon.name, top.taxon.preferred_common_name);
                    } else {
                        String info = top.taxon.preferred_common_name + " (" + top.taxon.name + ")\n\n" + summary;
                        tvResult.setText(info);
                    }
                    tvLocation.setText("Lat: " + lat + ", Lng: " + lng);

                } else {
                    Toast.makeText(ScanActivity.this, "Failed to get identification", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ScoreResponse> call, Throwable t) {

                setLoadingState(false);  // 👈 Hide on failure
                
                if (t instanceof java.net.SocketTimeoutException) {
                    Toast.makeText(ScanActivity.this, "Request timed out. Please try again.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ScanActivity.this, "API error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }

                Log.e("ScanActivity", "API failure: " + t.getMessage(), t);

            }
        });
    }

}
