package com.durga.balaji66.googlemapsgoogleplaces;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.durga.balaji66.googlemapsgoogleplaces.models.PlacesInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityMap extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String TAG = MainActivity.class.getName();
    private int PLACE_PICKER_REQUEST = 1;

    private boolean mLocationPermissionGranted = false;
    private static final int LOCATION_REQUEST_PERMISSION_CODE = 1234;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168), new LatLng(71,136));
    private PlacesInfo mPlace;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;
    private PlacesAutocompleteAdapter mPlacesAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private Marker mMarker;

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private ImageView mInfo , mPlacePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mSearchText =findViewById(R.id.editTextSearch);
        mGps =findViewById(R.id.ic_gps);
        mInfo = findViewById(R.id.place_info);
        mPlacePicker = findViewById(R.id.place_marker);
        getLocationPermission();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void init()
    {
        Log.d(TAG,"initialization : entered to init method" );
        mGoogleApiClient =new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this,this)
                .build();
        mSearchText.setOnItemClickListener(mAutoItemClickListener);
        mPlacesAutocompleteAdapter =new PlacesAutocompleteAdapter(this,mGoogleApiClient,LAT_LNG_BOUNDS,null);

        mSearchText.setAdapter(mPlacesAutocompleteAdapter);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() ==KeyEvent.ACTION_DOWN || event.getAction() == KeyEvent.KEYCODE_ENTER)
                {
                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    if(mMarker.isInfoWindowShown())
                    {
                        mMarker.hideInfoWindow();
                    }
                    else{
                        mMarker.showInfoWindow();
                    }
                }
                catch (NullPointerException e)
                {
                    Log.e(TAG,"OnClick NullPointerException :" + e.getMessage());
                }
            }
        });

        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ActivityMap.this),PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG,"GooglePlayServicesNotAvailableException :" + e.getMessage());
                }
            }
        });
        hideSoftKeyBoard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST)
        {
            Place place = PlacePicker.getPlace(this,data);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient,place.getId());
            placeResult.setResultCallback(mUpdatePlaceDetailsCallBack);
        }
    }

    private void geoLocate()
    {
        Log.d(TAG,"geoLocate find a location : entered to geoLocate method" );
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(this);
        List<Address> list = new ArrayList<>();
        try {
            list =geocoder.getFromLocationName(searchString,1);
        }
        catch (IOException e)
        {
            Log.e(TAG, "geoLocate : IOException " + e);
        }
        if(list.size()> 0)
        {
            Address address = list.get(0);
            Log.d(TAG,"geoLocate find a location : " + address.toString());
            Toast.makeText(getApplicationContext(),address.toString(),Toast.LENGTH_LONG).show();
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM, address.getAddressLine(0));
        }

    }

    private void initializeMap() {
        Log.d(TAG, "initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(ActivityMap.this);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getting device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, " OnComplete : found location! ");
                        Location currentLocation = (Location) task.getResult();
                        moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My location");
                    } else {
                        Log.d(TAG, " OnComplete : current location is null ");
                        Toast.makeText(getApplicationContext(), "unable to get current location", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation Security Exception: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom,String title) {
        Log.d(TAG, "moveCamera to last lat :" + latLng.latitude + " lng : " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if(!title.equals("My location"))
        {
            MarkerOptions options =new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyBoard();

    }


    private void moveCamera(LatLng latLng, float zoom, PlacesInfo placesInfo) {
        Log.d(TAG, "moveCamera to last lat :" + latLng.latitude + " lng : " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(ActivityMap.this));

        if(placesInfo != null)
        {
            try
            {
                String snippet = "Address :" +placesInfo.getAddress() +"\n" +
                        "Phone Number :" + placesInfo.getPhoneNumber() +"\n" +
                        "Website :" + placesInfo.getWebSiteUri() +"\n" +
                        "Price Rating :" + placesInfo.getRating() +"\n" ;
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(placesInfo.getName())
                        .snippet(snippet);
                mMarker = mMap.addMarker(options);
                mMap.addMarker(options);
            }
            catch (NullPointerException e)
            {
                Log.e(TAG,"NullPointerException : " + e.getMessage());
            }
        }
        else {
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyBoard();

    }

    private void getLocationPermission() {
        Log.d(TAG, "asking for permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initializeMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_REQUEST_PERMISSION_CODE:
                Log.d(TAG, "getting location permission");
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                        Log.d(TAG, "location permission granted");
                        mLocationPermissionGranted = true;
                        // initialize Map
                        initializeMap();
                    }
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "map ready for display");
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);

            //this is for removing current location button.
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
    }

    private void hideSoftKeyBoard()
    {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /*
        ---------------------------------  Google places autoComplete Suggestions -----------------------------------
     */

    private AdapterView.OnItemClickListener mAutoItemClickListener =new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyBoard();
            final AutocompletePrediction item = mPlacesAutocompleteAdapter.getItem(position);
            final String placeId =item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient,placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallBack);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallBack = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess())
            {
                Log.d(TAG,"onResult : place query did not complete successfully :" + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try {
                mPlace = new PlacesInfo();
                //mPlace.setAttributions(place.getAttributions().toString());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                mPlace.setName(place.getName().toString());
                mPlace.setId(place.getId());
                mPlace.setLatLng(place.getLatLng());
                mPlace.setAddress(place.getAddress().toString());
                mPlace.setRating(place.getRating());
                mPlace.setWebSiteUri(place.getWebsiteUri());

                Log.d(TAG,"onResult : place details " + mPlace.toString());

            }
            catch (NullPointerException e)
            {
                Log.e(TAG," onResult : NullPointer Exception" +e.getMessage());
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,place.getViewport().getCenter().longitude),DEFAULT_ZOOM,mPlace);
            places.release();
        }
    };
}
