package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.memorableplaces.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private double longitude;
    private double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using ViewBinding
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get the SupportMapFragment and notify when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Retrieve latitude and longitude from the Intent
        Intent intent = getIntent();
        longitude = intent.getDoubleExtra("longitude", 0.0);
        latitude = intent.getDoubleExtra("latitude", 0.0);
    }

    /**
     * Called when the Google Map is ready to be used.
     * This method sets up the map type, adds a marker at the given coordinates,
     * and sets up a long-click listener to save new places.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set the map type to Hybrid for satellite + street map
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Retrieve SharedPreferences to store places
        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);

        // Log received coordinates for debugging
        Log.i("Ethiopia", "Longitude: " + longitude + ", Latitude: " + latitude);

        // Create LatLng object for the received coordinates
        LatLng userLocation = new LatLng(latitude, longitude);

        // Add marker at user's location
        mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));

        // Move the camera to user's location with zoom level 18
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18));

        // Set up a long-click listener to allow users to save locations
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(@NonNull LatLng latLng) {
                // Initialize Geocoder to convert coordinates into a readable address
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                // Clear previous markers
                mMap.clear();

                try {
                    // Fetch address information for the clicked location
                    List<Address> placeInfo = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 2);

                    if (placeInfo != null && !placeInfo.isEmpty()) {
                        Address address = placeInfo.get(0);  // Get the first address result
                        String addressLine = address.getAddressLine(0);

                        // Log the detected address for debugging
                        Log.i("Ethiopia", "Saved Location: " + addressLine);

                        // Add a marker at the new location with the detected address
                        mMap.addMarker(new MarkerOptions().position(latLng).title(addressLine));

                        // Store the new place in the MainActivity lists
                        MainActivity.places.add(addressLine);
                        MainActivity.longitudes.add(String.valueOf(latLng.longitude));
                        MainActivity.latitudes.add(String.valueOf(latLng.latitude));

                        // Save updated places data in SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("places", ObjectSerializer.serialize(MainActivity.places));
                        editor.putString("latitudes", ObjectSerializer.serialize(MainActivity.latitudes));
                        editor.putString("longitudes", ObjectSerializer.serialize(MainActivity.longitudes));
                        editor.apply();

                        // Notify adapter that data has changed so the UI updates
                        MainActivity.arrayAdapter.notifyDataSetChanged();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
