package com.example.sosbicicletta2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MappaFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback, LocationListener, IOnLoadLocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    //private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private boolean mPermissionDenied = false;

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private View mView;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Marker currentUser;
    private DatabaseReference DriverD;
    private List<LatLng> Driver;
    private IOnLoadLocationListener listener;
    private Location lastLocation;





    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_mappa, container, false);

        return mView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

                        /*SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapview);
                        mapFragment.getMapAsync(MappaFragment.this); */

                        initDriver();



                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getContext(), "Devi accettare i permessi", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();


    }

    private void initDriver() {

        DriverD =  FirebaseDatabase.getInstance()
                .getReference("DriverAvailable");


        listener = this;


                DriverD.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<LatLngDriver> latLngList = new ArrayList<>();
                        for (DataSnapshot locationSnapShot: dataSnapshot.getChildren()){
                            LatLngDriver latLng = locationSnapShot.getValue(LatLngDriver.class);
                            latLngList.add(latLng);
                        }
                        listener.onLoadLocationSuccess(latLngList);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onLoadLoactionFailed(error.getMessage());

                    }
                });
                DriverD.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //update
                        List<LatLngDriver> latLngList = new ArrayList<>();
                        for (DataSnapshot locationSnapShot: dataSnapshot.getChildren()){
                            LatLngDriver latLng = locationSnapShot.getValue(LatLngDriver.class);
                            latLngList.add(latLng);
                        }
                        listener.onLoadLocationSuccess(latLngList);

                        //clear map and add again



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mGoogleMap != null) {
                    /*if (currentUser != null) currentUser.remove();
                    currentUser = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(locationResult.getLastLocation().getLatitude(),
                                    locationResult.getLastLocation().getLongitude()))
                            .title("You"));*/
                    lastLocation = locationResult.getLastLocation();

                    LatLng latLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    //Dopo aver aggiunto il marker, sposta telecamera
                    /*mGoogleMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(latLng, 17.0f));*/

                }

            }


        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(200);
        locationRequest.setSmallestDisplacement(10f);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //MapsInitializer.initialize(requireContext()); //inzializza mappa
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        //enableMyLocation();
        if (fusedLocationProviderClient != null)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);

            for(LatLng latLng: Driver){
               /* mGoogleMap.addCircle(new CircleOptions().center(latLng).radius(5)
                        .strokeColor(Color.RED)
                        .fillColor(Color.RED)
                        .strokeWidth(5.0f).clickable(true)
                        );*/
                mGoogleMap.addMarker(new MarkerOptions().position(latLng).draggable(false).title("Driver: ").snippet("Telefono").icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_bike_18dp)));
                Log.d("diooo", "onMapReady: "+latLng);
            }


    }

    @Override
    public void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    public void onLocationChanged(Location location) {



    }







    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLoadLocationSuccess(List<LatLngDriver> latLngs) {
        Driver = new ArrayList<>();
        for (LatLngDriver latLngDriver : latLngs){
            LatLng convert = new LatLng(latLngDriver.getLatitude(),latLngDriver.getLongitude());
            Driver.add(convert);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(MappaFragment.this);

        if (mGoogleMap != null)
        {
            mGoogleMap.clear();
            //add pos utente


           // LatLng latLng = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
            //Dopo aver aggiunto il marker, sposta telecamera
           /* mGoogleMap.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(latLng, 17.0f));*/

            for(LatLng latLng1: Driver){
               /* mGoogleMap.addCircle(new CircleOptions().center(latLng).radius(5)
                        .strokeColor(Color.RED)
                        .fillColor(Color.RED)
                        .strokeWidth(5.0f).clickable(true)
                        );*/
                mGoogleMap.addMarker(new MarkerOptions().position(latLng1).title("Driver").draggable(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_bike_18dp)));
                Log.d("diooo", "onMapReady: "+latLng1);
            }


        }
    }

    @Override
    public void onLoadLoactionFailed(String message) {
        Toast.makeText(getContext(),""+message,Toast.LENGTH_SHORT).show();
    }
}




