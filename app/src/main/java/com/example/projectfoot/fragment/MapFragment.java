package com.example.projectfoot.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.projectfoot.R;
import com.example.projectfoot.config.FirebaseConfig;
import com.example.projectfoot.helper.Permissions;
import com.example.projectfoot.model.Court;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {

    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private ValueEventListener valueEventListenerCourts;
    private Location userLocation;
    private DatabaseReference courtsRef;
    private ArrayList<Court> courtsList = new ArrayList<>();
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private final int PERMISSION_ID = 44;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {

            mMap = googleMap;
            userLocation = new Location("");
            //method to get the location
                getLastLocation();
                getCourtsFromFirebase();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Permissions.validatePermissions(permissions, getActivity(), 1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        courtsRef = FirebaseConfig.getFirebaseDatabase().child("courts");

        return view;
    }

    public void addCourtsOnMap(){
        Toast.makeText(getContext(), String.valueOf(courtsList.size()), Toast.LENGTH_SHORT).show();
        for(Court court: courtsList){

            String courtAddress = court.getAddress();
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

            try {
                //List<Address> listaEndereço = geocoder.getFromLocation(latitude, longitude, 1);
                List<Address> addressList = geocoder.getFromLocationName(courtAddress, 1);
                if(addressList != null && addressList.size() > 0){
                    Double lat = addressList.get(0).getLatitude();
                    Double lg = addressList.get(0).getLongitude();


                    LatLng latLngUser = new LatLng(lat, lg);
                    Marker marker = mMap.addMarker(new MarkerOptions().
                            position(latLngUser).
                            title(court.getName()).
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    assert marker != null;
                    mMap.setOnMarkerClickListener(this);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {

                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            if(userLocation != null) {
                                userLocation.setLatitude(location.getLatitude());
                                userLocation.setLongitude(location.getLongitude());
                                LatLng latLngUser = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(latLngUser).title("Meu local"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngUser, 15));
                            }
                        }
                    }
                });
            } else {
                Toast.makeText(getActivity(), R.string.turn_on_location, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();

            userLocation.setLatitude( Objects.requireNonNull(mLastLocation).getLatitude() );
            userLocation.setLongitude( mLastLocation.getLongitude() );

            LatLng latLngUser = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLngUser).title("User"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngUser, 15));
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(valueEventListenerCourts != null) courtsRef.removeEventListener(valueEventListenerCourts);
    }

    public void getCourtsFromFirebase(){
        valueEventListenerCourts = courtsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                cleanCourtsList();

                for(DataSnapshot data: snapshot.getChildren()){
                    Court court = data.getValue(Court.class);
                    courtsList.add(court);
                }

                addCourtsOnMap();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void cleanCourtsList(){
        courtsList.clear();
    }


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {

        Bundle bundle = new Bundle();
        bundle.putString("courtName",marker.getTitle());

        CourtDetailsFragment fragmentCourtDetails = new CourtDetailsFragment();
        fragmentCourtDetails.setArguments(bundle);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.viewPager, fragmentCourtDetails).commit();

        return true;
    }
}