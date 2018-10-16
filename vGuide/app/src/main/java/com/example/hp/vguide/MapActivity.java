package com.example.hp.vguide;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hp.vguide.models.DirectionFinder;
import com.example.hp.vguide.models.DirectionFinderListener;
import com.example.hp.vguide.models.Route;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


import com.example.hp.vguide.models.PlaceInfo;

/**
 * Created by User on 10/2/2017.
 */

MapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener,DirectionFinderListener {

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));

            if (!success) {
                Log.e("Mapactivity", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Mapactivity", "Can't find style. Error: ", e);
        }

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();

        }
    }

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private ImageView mInfo;
    private Button btnFindPath;



    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    int PROXIMITY_RADIUS = 10000;
    double latitude,longitude;

    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;

    private PolylineOptions polylineOption1 = new PolylineOptions();
    private PolylineOptions polylineOption2 = new PolylineOptions();
    private PolylineOptions polylineOption3 = new PolylineOptions();

    private Polyline line1;
    private Polyline line2;
    private Polyline line3;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mInfo=(ImageView) findViewById(R.id.place_info);
        btnFindPath = (Button) findViewById(R.id.btnFindPath);

        mDrawerlayout= (DrawerLayout) findViewById(R.id.drawer);
        mToggle= new ActionBarDrawerToggle(this,mDrawerlayout,R.string.open,R.string.close);
        mDrawerlayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nv);
        setupDrawerContent(nvDrawer);

        getLocationPermission();
    }


    public void selectIterDrawer(MenuItem menuItem){
        Fragment myFragment=null;
        Class fragmentClass;
        switch (menuItem.getItemId()){
            case R.id.profile:
                fragmentClass = Profile.class;
                break;
            case R.id.search:
                fragmentClass = Search.class;
                break;
            case R.id.help:
                fragmentClass = Help.class;
                break;
            case R.id.settings:
                fragmentClass = Settings.class;
                break;
            case R.id.logout:
                fragmentClass = Logout.class;
                break;
            default:
                fragmentClass = Profile.class;
        }

        try {
            myFragment = (Fragment) fragmentClass.newInstance();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flcontent,myFragment).commit();
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mToggle.setDrawerIndicatorEnabled(false);
        mDrawerlayout.closeDrawers();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectIterDrawer(item);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (mToggle.onOptionsItemSelected(item)){
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
//        return false;

        return super.onOptionsItemSelected(item);


    }

    private void init(){
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);


        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    mMap.clear();
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked place info");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }else{
                        Log.d(TAG, "onClick: place info: " + mPlace.toString());
                        mMarker.showInfoWindow();
                    }
                }catch (NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException: " + e.getMessage() );
                }
            }
        });

        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                sendRequest();
            }
        });

        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener()
        {
            @Override
            public void onPolylineClick(Polyline polyline)
            {
                line1.setColor(Color.GRAY);
                line1.setZIndex(1f);

                if (line3 == null){
                    line2.setColor(Color.GRAY);
                    line2.setZIndex(1f);

                } else {
                    line2.setColor(Color.GRAY);
                    line2.setZIndex(1f);

                    line3.setColor(Color.GRAY);
                    line3.setZIndex(1f);
                }

                polyline.setColor(Color.BLUE);
                polyline.setZIndex(2f);

                List<LatLng> points = polyline.getPoints();

                for (int i=0;i<points.size();i=i+40){
//                    Log.d(TAG, "onPolylineClick:"+points.get(i).latitude);
//                    Log.d(TAG, "onPolylineClick:"+points.get(i).longitude);

                    latitude=points.get(i).latitude;
                    longitude=points.get(i).longitude;

                    Object dataTransfer[] = new Object[2];
                    GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                    String type = "natural_feature";
                    String url = getUrl(latitude, longitude, type);
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = url;

                    getNearbyPlacesData.execute(dataTransfer);
                    Toast.makeText(MapActivity.this, "Showing Nearby Places", Toast.LENGTH_SHORT).show();
                }






            }
        });





        hideSoftKeyboard();


    }

    private void sendRequest() {
        String origin = "jaffna";
        String destination = mSearchText.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();


        int size1=0;
        int size2=0;
        int size3=0;
        Integer time;
        int minTime;

//        for (Route route : routes){
//            int j=1;
//
//            if (j==1){
//                size1=route.duration.value;
//            }else if(j==2){
//                size2=route.duration.value;
//            }else if(j==3){
//                size3=route.duration.value;
//            }
//
//            j++;
//        }

//        minTime=Math.min(size1,Math.min(size2,size3));
//        minTime=5;
//        time=5;




//        polylineOption1.clickable(true);
//        polylineOption2.clickable(true);
//        polylineOption3.clickable(true);

        int j=1;

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            routes.size();

            if (j==1){
                size1=route.duration.value;
                for (int i = 0; i < route.points.size(); i++)
                    polylineOption1.add(route.points.get(i));

            }else if(j==2){
                size2=route.duration.value;
                for (int i = 0; i < route.points.size(); i++)
                    polylineOption2.add(route.points.get(i));

            }else if(j==3){
                size3=route.duration.value;
                for (int i = 0; i < route.points.size(); i++)
                    polylineOption3.add(route.points.get(i));
            }

            j++;

//            PolylineOptions polylineOptions = new PolylineOptions().
//                    geodesic(true).
//                    color(Color.BLUE).
//                    width(15);
//
//            PolylineOptions greypolylineOptions = new PolylineOptions().
//                    geodesic(true).
//                    color(Color.GRAY).
//                    width(15);

//            time=route.duration.value;


//            if (minTime!=time){
//                for (int i = 0; i < route.points.size(); i++)
//                    greypolylineOptions.add(route.points.get(i));
//
//                polylinePaths.add(mMap.addPolyline(greypolylineOptions));
//            }else{
//                for (int i = 0; i < route.points.size(); i++)
//                    polylineOptions.add(route.points.get(i));
//
//                polylinePaths.add(mMap.addPolyline(polylineOptions));
//            }
//            time++;

        }

//        minTime=Math.min(size1,Math.min(size2,size3));
//
//        if (minTime==size1){
//            if (routes.size()==1){
//                polylineOption1.geodesic(true).color(Color.BLUE).width(15);
//                polylinePaths.add(mMap.addPolyline(polylineOption1));
//            }else if (routes.size()==2){
//                polylineOption2.geodesic(true).color(Color.GRAY).width(15);
//                polylineOption1.geodesic(true).color(Color.BLUE).width(15);
//
//                polylinePaths.add(mMap.addPolyline(polylineOption2));
//                polylinePaths.add(mMap.addPolyline(polylineOption1));
//            }else{
//                polylineOption2.geodesic(true).color(Color.GRAY).width(15);
//                polylineOption3.geodesic(true).color(Color.GRAY).width(15);
//                polylineOption1.geodesic(true).color(Color.BLUE).width(15);
//
//                polylinePaths.add(mMap.addPolyline(polylineOption2));
//                polylinePaths.add(mMap.addPolyline(polylineOption3));
//                polylinePaths.add(mMap.addPolyline(polylineOption1));
//            }
//
//        }else if(minTime==size2){
//            if (routes.size()==2){
//                polylineOption1.geodesic(true).color(Color.GRAY).width(15);
//                polylineOption2.geodesic(true).color(Color.BLUE).width(15);
//
//                polylinePaths.add(mMap.addPolyline(polylineOption1));
//                polylinePaths.add(mMap.addPolyline(polylineOption2));
//
//            }else {
//                polylineOption1.geodesic(true).color(Color.GRAY).width(15);
//                polylineOption3.geodesic(true).color(Color.GRAY).width(15);
//                polylineOption2.geodesic(true).color(Color.BLUE).width(15);
//
//                polylinePaths.add(mMap.addPolyline(polylineOption1));
//                polylinePaths.add(mMap.addPolyline(polylineOption3));
//                polylinePaths.add(mMap.addPolyline(polylineOption2));
//            }
//
//        }else if (minTime==size3){
//            polylineOption1.geodesic(true).color(Color.GRAY).width(15);
//            polylineOption2.geodesic(true).color(Color.GRAY).width(15);
//            polylineOption3.geodesic(true).color(Color.BLUE).width(15);
//
//            polylinePaths.add(mMap.addPolyline(polylineOption1));
//            polylinePaths.add(mMap.addPolyline(polylineOption2));
//            polylinePaths.add(mMap.addPolyline(polylineOption3));
//        }

        if (routes.size()==1){
            polylineOption1.geodesic(true).color(Color.BLUE).width(15);

            line1=mMap.addPolyline((polylineOption1));
            line1.setClickable(true);
            line1.setTag(new String("line1"));
//            polylinePaths.add(mMap.addPolyline(polylineOption1));
        }else if (routes.size()==2){
            minTime=Math.min(size1,size2);

            if (minTime==size2){
                polylineOption1.geodesic(true).color(Color.GRAY).width(15);
                polylineOption2.geodesic(true).color(Color.BLUE).width(15);

                 line1=mMap.addPolyline((polylineOption1));
                 line1.setClickable(true);
                 line1.setTag(new String("line1"));

                 line2=mMap.addPolyline((polylineOption2));
                 line2.setClickable(true);
                 line2.setTag(new String("line2"));


//                polylinePaths.add(mMap.addPolyline(polylineOption1));
//                polylinePaths.add(mMap.addPolyline(polylineOption2));
            }else if (minTime==size1){
                polylineOption2.geodesic(true).color(Color.GRAY).width(15);
                polylineOption1.geodesic(true).color(Color.BLUE).width(15);

                 line2=mMap.addPolyline((polylineOption2));
                 line2.setClickable(true);
                 line2.setTag(new String("line2"));

                 line1=mMap.addPolyline((polylineOption1));
                 line1.setClickable(true);
                 line1.setTag(new String("line1"));

//                polylinePaths.add(mMap.addPolyline(polylineOption2));
//                polylinePaths.add(mMap.addPolyline(polylineOption1));
            }

        }else if (routes.size()==3){
            minTime=Math.min(size1,Math.min(size2,size3));

            if (minTime==size3){
                polylineOption1.geodesic(true).color(Color.GRAY).width(15);
                polylineOption2.geodesic(true).color(Color.GRAY).width(15);
                polylineOption3.geodesic(true).color(Color.BLUE).width(15);

                line1=mMap.addPolyline((polylineOption1));
                line1.setClickable(true);
                line1.setTag(new String("line1"));

                line2=mMap.addPolyline((polylineOption2));
                line2.setClickable(true);
                line2.setTag(new String("line2"));

                line3=mMap.addPolyline((polylineOption3));
                line3.setClickable(true);
                line3.setTag(new String("line3"));

//                polylinePaths.add(mMap.addPolyline(polylineOption1));
//                polylinePaths.add(mMap.addPolyline(polylineOption2));
//                polylinePaths.add(mMap.addPolyline(polylineOption3));
            }else if (minTime==size2){
                polylineOption1.geodesic(true).color(Color.GRAY).width(15);
                polylineOption3.geodesic(true).color(Color.GRAY).width(15);
                polylineOption2.geodesic(true).color(Color.BLUE).width(15);

                line1=mMap.addPolyline((polylineOption1));
                line1.setClickable(true);
                line1.setTag(new String("line1"));

                line3=mMap.addPolyline((polylineOption3));
                line3.setClickable(true);
                line3.setTag(new String("line3"));

                line2=mMap.addPolyline((polylineOption2));
                line2.setClickable(true);
                line2.setTag(new String("line2"));

//                polylinePaths.add(mMap.addPolyline(polylineOption1));
//                polylinePaths.add(mMap.addPolyline(polylineOption3));
//                polylinePaths.add(mMap.addPolyline(polylineOption2));
            }else if (minTime==size1){
                polylineOption2.geodesic(true).color(Color.GRAY).width(15);
                polylineOption3.geodesic(true).color(Color.GRAY).width(15);
                polylineOption1.geodesic(true).color(Color.BLUE).width(15);

                line2=mMap.addPolyline((polylineOption2));
                line2.setClickable(true);
                line2.setTag(new String("line2"));

                line3=mMap.addPolyline((polylineOption3));
                line3.setClickable(true);
                line3.setTag(new String("line3"));

                line1=mMap.addPolyline((polylineOption1));
                line1.setClickable(true);
                line1.setTag(new String("line1"));

//                polylinePaths.add(mMap.addPolyline(polylineOption2));
//                polylinePaths.add(mMap.addPolyline(polylineOption3));
//                polylinePaths.add(mMap.addPolyline(polylineOption1));
            }

        }


    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));

        }

        hideSoftKeyboard();
    }

    private String getUrl(double latitude , double longitude , String nearbyPlace)
    {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyChdVUDDTrvkHu6LgtLQq86ziWVy5SdUNE");

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));

        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placeInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);

            }catch (NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage() );
            }
        }else{
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText)
        {
            InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
        }
    }

     /*
        --------------------------- google places API autocomplete suggestions -----------------
     */

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace);

            places.release();
        }
    };
}
