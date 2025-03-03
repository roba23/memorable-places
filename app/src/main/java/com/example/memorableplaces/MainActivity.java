package com.example.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // LocationManager to access location services
    LocationManager locationManager;
    LocationListener locationListener;
    // Initial coordinates (will be updated later)
    double longitude = 50;
    double latitude = 50;

    // Lists to store place names, latitudes, and longitudes
    static ArrayList<String> places = new ArrayList<>();
    static ArrayList<String> latitudes = new ArrayList<>();
    static ArrayList<String> longitudes = new ArrayList<>();
    static ArrayAdapter<String> arrayAdapter;

    // Handle the result of the location permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if permission was granted
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // If permission is granted, request location updates
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    // onCreate method, where the activity is initialized
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge layout
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Adjust padding based on system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get stored places, latitudes, and longitudes from SharedPreferences
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        try {
            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places", ObjectSerializer.serialize(new ArrayList<>(Arrays.asList("Add place")))));
            latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes", ObjectSerializer.serialize(new ArrayList<>())));
            longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes", ObjectSerializer.serialize(new ArrayList<>())));
        } catch (IOException e) {
            Log.i("Ethiopia", "Failed to fetch the stored variables");
            throw new RuntimeException(e);
        }

        // Initialize LocationManager and LocationListener
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Update latitude and longitude whenever location changes
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        };

        // Request location permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            // If permission is granted, request location updates and get the last known location
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                latitude = lastKnownLocation.getLatitude();
                longitude = lastKnownLocation.getLongitude();
                Log.i("Ethiopia", "Last Known Location: " + latitude + ", " + longitude);
            } else {
                Log.i("Ethiopia", "No last known location available.");
            }
        }

        // Initialize the ListView and ArrayAdapter
        ListView myListView = findViewById(R.id.myListView);

        // If an address is passed through the intent, add it to the places list
        Intent intent = getIntent();
        if (intent.getStringExtra("address") != null) {
            places.add(intent.getStringExtra("address"));
        }

        // Set up ArrayAdapter for the ListView
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, places);
        myListView.setAdapter(arrayAdapter);

        // Handle item clicks on the ListView
        myListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent mapIntent = new Intent(getApplicationContext(), MapsActivity.class);
            // If the first item is clicked, use the current latitude and longitude
            if (position == 0) {
                mapIntent.putExtra("latitude", latitude);
                mapIntent.putExtra("longitude", longitude);
            } else {
                // Otherwise, use the saved latitude and longitude from the list
                mapIntent.putExtra("latitude", Double.parseDouble(latitudes.get(position - 1)));
                mapIntent.putExtra("longitude", Double.parseDouble(longitudes.get(position - 1)));
                Log.i("Ethiopia", "Latitude: " + latitudes.get(position - 1));
                Log.i("Ethiopia", "Longitude: " + longitudes.get(position - 1));
            }
            startActivity(mapIntent);
        });
    }
}
