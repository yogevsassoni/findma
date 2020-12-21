package com.example.magshimim.findma;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.location.LocationManager.GPS_PROVIDER;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("server/saving-data/marker");


    //intitials
    private GoogleMap mMap;
    String str = "";
    String name;
    static LatLng destination = new LatLng(0,0);
    static LatLng latLng = new LatLng(0, 0);
    double latitude = 0;
    double longitude = 0;
    double desLat = 0;
    double desLon = 0;
    EditText nameInput;
    SearchView place;
    String toSearch;
    LocationManager locationManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Button sendRequest;

        desLon = 0;
        desLat = 0;
        Log.i("rofl","came to rofl4"); //permissions
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        2);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                        2);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //bulding the button
        sendRequest = (Button) findViewById(R.id.addMarker);
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                nameInput = (EditText) findViewById(R.id.editText);
                str = nameInput.getText().toString();
                System.out.println("rofl");
                System.out.println(this.getClass());
                Log.i("rofl","came to addmarker");
                FirebaseMarker addMarker = new FirebaseMarker();
                addMarker.setStr(str);
                addMarker.setLatitude(latitude);
                addMarker.setLongitude(longitude);
                DatabaseReference usersRef = ref.child("users");
                usersRef.push().setValue(addMarker);
                mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                Toast.makeText(getApplicationContext(),"added",Toast.LENGTH_SHORT).show();
                nameInput.clearComposingText();
            }
        });

        place = (SearchView) findViewById(R.id.place);
        place.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                toSearch = s;
                findMarker(toSearch);
                TaskRequestDirection taskRequestDirection = new TaskRequestDirection();

                String url = getDirection(latLng,destination);
                taskRequestDirection.execute(url);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mapFragment.getMapAsync(this);
        System.out.println(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Log.i("rofl","came to rof5");
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
        if (locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER))) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override

                public void onLocationChanged(Location location) {
                    str = "";
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    latLng = new LatLng(latitude, longitude);
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
            });
        } else if (locationManager.isProviderEnabled((GPS_PROVIDER))) {
            locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i("rofl","came to rof9");
                    str = "";
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    latLng = new LatLng(latitude, longitude);
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
            });
        }
    }



    private void addMarkersToMap(final GoogleMap map) { //adding the markers from the fire base


        ref.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    double latitude = (double) (snapshot.child("latitude").getValue());
                    double longitude = (double) ((snapshot.child("longitude").getValue()));
                    String name = (String) (snapshot.child("str").getValue());
                    LatLng location = new LatLng(latitude,longitude);
                    map.addMarker(new MarkerOptions().position(location).title(name)).showInfoWindow();

                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void findMarker(String s) //finding the marker with aql request
    {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        desLat = (double) (snapshot.child("latitude").getValue());
                        desLon = (double) ((snapshot.child("longitude").getValue()));
                        destination = new LatLng(desLat, desLon);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 50.2f));

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        Query query = ref.child("users").orderByChild("str").equalTo(s);
        query.addListenerForSingleValueEvent(valueEventListener);
    }

    private String getDirection(LatLng origin, LatLng dest) //getting the url for the request to the api
    {
        Log.i("rofl","creating the http");
        String str_org = "origin="+ origin.latitude + "," + origin.longitude;
        String str_dst = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=walking";
        String apiKey= "key=AIzaSyCvkWAWyUjXMl6cv8YHvxgnDJyKRkgafu8";
        String param = str_org + "&" + str_dst + "&" + sensor + "&" + mode + "&" + apiKey;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        Log.i("rofl","done creating the http");
        Log.i("rofl",url);
        return url;
    }

    private String requestDirections(String reqUrl) throws IOException { //getting the data from google api
        String responseString = "";
        Log.i("rofl","connecting to http");
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try
        {
            URL url =new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader  inputStreamReader  = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while((line = bufferedReader.readLine())!= null )
            {
                stringBuffer.append(line);

            }
            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(inputStream != null)
            {
                inputStream.close();

            }
            httpURLConnection.disconnect();
        }
        Log.i("rofl",responseString);
        return responseString;
    }

    public class TaskRequestDirection extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {

                responseString = requestDirections((strings[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;


        }

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            Log.i("rofl","came to add the call");
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String,Void,List<List<HashMap<String,String>>>>{ //getting the string of the points

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String,String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) { //takes every point and adds it to the poily line
            ArrayList points = null;
            Log.i("rofl","came to add the poly");
            PolylineOptions polylineOptions;
            polylineOptions = null;

            for(List<HashMap<String,String>> path : lists)
            {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                for (HashMap<String,String> point : path)
                {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    points.add(new LatLng(lat,lng));

                }
                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }
            if(polylineOptions!=null)
            {
                mMap.addPolyline(polylineOptions);

            }
            else{

            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return;
        }


        Log.i("rofl","came to rofl3");
        mMap.setMyLocationEnabled(true);
        addMarkersToMap(googleMap);
        Log.i("rofl","came to rofl5");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 50.2f));
        Log.i("rofl","came to rofl6");

    }

}