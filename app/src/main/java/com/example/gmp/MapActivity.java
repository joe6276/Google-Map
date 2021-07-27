package com.example.gmp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String TAG = " MApActivity";
    private static final int PERMISSION_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private Boolean MLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button save;
    private Button retrieve;

    private EditText mSearchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getLocationPermissions();
        save= findViewById(R.id.save);
        mSearchText= findViewById(R.id.input);

        retrieve= findViewById(R.id.retrieve);
        retrieve.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      retrieve();
                    }
                }
        );

        initialize();
    }
    private  void initialize(){
        mSearchText.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if(actionId== EditorInfo.IME_ACTION_DONE
                        ||actionId== EditorInfo.IME_ACTION_SEARCH
                        || event.getAction()== KeyEvent.ACTION_DOWN
                        || event.getAction()== KeyEvent.KEYCODE_ENTER){
                            geoLocate();
                        }
                        return false;
                    }
                }
        );
        hideKeyboard();
    }

    private void hideKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void geoLocate() {
        String search_string= mSearchText.getText().toString();
        Geocoder geocoder= new Geocoder(MapActivity.this);
        List<Address> address= new ArrayList<>();

        try {
            address= geocoder.getFromLocationName(search_string,1);
            if(address.size()>0){
                Address address1= address.get(0);
                Log.d("MapActivity","found "+address1.toString());
                moveCamera(new LatLng(address1.getLatitude(),address1.getLongitude()),DEFAULT_ZOOM,address1.getAddressLine(0));
                Log.d("MapActivity","found "+address1.getLatitude());
                Log.d("MapActivity","found "+address1.getLongitude());
                
            }

        } catch (Exception e) {
            Toast.makeText(MapActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();
        }


    }
    private void retrieve() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Location");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               Double latitude= dataSnapshot.child("latitude").getValue(Double.class);
               Double longitude= dataSnapshot.child("longitude").getValue(Double.class);
               Toast.makeText(MapActivity.this,
                       "The Latitude from Db is "+latitude +"and the Longitude is "+longitude,Toast.LENGTH_LONG).show();

                    try {
                        Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                        List<Address> addresses =geocoder.getFromLocation(latitude, longitude, 1);
                        addresses = geocoder. getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        String city = addresses.get(0).getLocality();
                        String state = addresses.get(0).getAdminArea();
                        String country = addresses.get(0).getCountryName();


                        Toast.makeText(MapActivity.this,address,Toast.LENGTH_LONG).show();
                        Toast.makeText(MapActivity.this,city,Toast.LENGTH_LONG).show();
                        Toast.makeText(MapActivity.this,state,Toast.LENGTH_LONG).show();
                        Toast.makeText(MapActivity.this,country,Toast.LENGTH_LONG).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_LONG).show();
        mMap = googleMap;

        if (MLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                     Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    private void InitMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }


    private void getDeviceLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {

            if (MLocationPermissionGranted) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(
                        new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()){



                                    Location currentlocation=(Location) task.getResult();
                                    moveCamera(new LatLng(currentlocation.getLatitude(),currentlocation.getLongitude()),DEFAULT_ZOOM,"CL");
                                    LocationHelper helper= new LocationHelper(
                                            currentlocation.getLongitude(),currentlocation.getLatitude()
                                    );

                                    FirebaseDatabase.getInstance().getReference("Location").setValue(helper).addOnCompleteListener(
                                            new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        //Toast.makeText(MapActivity.this,"Saved",Toast.LENGTH_LONG).show();
                                                    }

                                                }
                                            }
                                    );
                                }else{
                                    Toast.makeText(MapActivity.this, "Location not found",Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                );
            }



        }catch (Exception e){
Toast.makeText(MapActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }



    private void moveCamera(LatLng latLng, float zoom , String title){

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        MarkerOptions options= new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(options);
    }

    private void getLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                MLocationPermissionGranted = true;
                InitMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MLocationPermissionGranted = false;
        switch (requestCode){
            case PERMISSION_CODE:{
                if(grantResults.length>0){
                    for(int i =0 ; i<grantResults.length; i++){
                    if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                        MLocationPermissionGranted=false;
                        return;
                    }
                    }
                    MLocationPermissionGranted = true;
                    InitMap();
                }
            }
        }
    }


}
