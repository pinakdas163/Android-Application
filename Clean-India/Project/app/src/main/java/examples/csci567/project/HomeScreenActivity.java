package examples.csci567.project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HomeScreenActivity extends AppCompatActivity implements
        View.OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback,
        GeoQueryEventListener, ResultCallback<Status> {
    private static final String GEO_FIRE_REF =
            "https://pinak-cleanindia.firebaseio.com/Garbage";
    private static final String INFORMER_REF =
            "https://pinak-cleanindia.firebaseio.com/Garbage_Informer";
    private static final int ERROR_DIALOG_REQUEST = 10;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    private PendingIntent pendingIntent = null;
    //Firebase garbageRef=new Firebase(GEO_FIRE_REF);
    Firebase infoRef = new Firebase(INFORMER_REF);
    Firebase childRef;
    TextView mStatusTextView;
    String mAddressOutput;
    //Circle circle;
    private ArrayList<Geofence> geofenceList = new ArrayList<>();
    //ArrayList<LatLng> mGeofenceCoordinates=mGeofenceCoordinates = new ArrayList<LatLng>();
    private Map<String, Marker> markers;
    private Map<String, Circle> circles;
    private Map<String, String> addInfo;
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    User user;
    protected static GeoLocation INITIAL_CENTER;
    private AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        geoFire = new GeoFire(new Firebase(GEO_FIRE_REF));
        this.markers = new HashMap<String, Marker>();
        this.circles = new HashMap<String, Circle>();
        this.addInfo = new HashMap<String, String>();
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        if (IsPlayServicesAvailable()) {
            setContentView(R.layout.activity_map);
            mapFrag = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFrag.getMapAsync(this);
            Toast.makeText(this, "Connected to map", Toast.LENGTH_SHORT).show();
            Log.d("Maps", "Connected");
        } else {
            Toast.makeText(this, "Not connected to map", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.home_screen);
        }
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        mStatusTextView = (TextView) findViewById(R.id.textView);
        //maddress=(TextView) findViewById(R.id.address);
        mStatusTextView.setText("Welcome " + user.getDisplayName());
        Button addressButton = (Button) findViewById(R.id.button);
        addressButton.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
        infoRef.removeEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
               /* this.geoQuery.removeAllListeners();
                for (Marker marker: this.markers.values()) {
                    marker.remove();
                }
                this.markers.clear();*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // .enableAutoManage(this /* FragmentActivity */,
                //       this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        INITIAL_CENTER = new GeoLocation(location.getLatitude(), location.getLongitude());
                /*MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Current Position");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);*/

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        geoQuery = geoFire.queryAtLocation(INITIAL_CENTER, 3);
        //buildGoogleApiClient();
        geoQuery.addGeoQueryEventListener(this);
        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                            //mGoogleApiClient.connect();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    protected boolean IsPlayServicesAvailable() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to mapping services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (mLastLocation != null) {
            startIntentService();
        }
        if (mLastLocation == null) {
            Log.d("FAILED", "mLastLocation is null");
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                onClick(v);
            }
        }
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        mResultReceiver = new AddressResultReceiver(new Handler());
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            //System.out.println(mAddressOutput);
            //maddress.setText(mAddressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                // Log.d("result code", "address found");
                geoFire.getLocation(mAddressOutput, new LocationCallback() {
                    @Override
                    public void onLocationResult(String key, GeoLocation location) {
                        if (location != null) {
                            Toast.makeText(getApplicationContext(),
                                    "We are already informed about this location",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            fireBaseGarbAdd(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        System.err.println("There was an error getting the GeoFire location: "
                                + firebaseError);
                    }
                });
            }
        }
    }

    private void fireBaseGarbAdd(double getLat, double getLong) {
        geoFire.setLocation(mAddressOutput, new GeoLocation(getLat, getLong),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, FirebaseError error) {
                        if (error != null) {
                            System.err.println("There was an error saving the location to GeoFire: "
                                    + error);
                        } else {
                            System.out.println("Location saved on server successfully!");
                            addInfo.put(mAddressOutput, user.getDisplayName());
                            //childRef=infoRef.child("Locations");
                            infoRef.setValue(addInfo);
                            Toast.makeText(getApplicationContext(), "Thank's for informing",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        Marker marker = this.mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.latitude, location.longitude))
                .title(key));
        Circle circle = this.mGoogleMap.addCircle(new CircleOptions()
                .center(new LatLng(location.latitude, location.longitude)).radius(100)
                .fillColor(Color.argb(66, 255, 0, 255))
                .strokeColor(Color.argb(66, 0, 0, 0)));
        //implement();
        if (marker != null) {
            this.markers.put(key, marker);
            //
        }
        if (circle != null) {
            this.circles.put(key, circle);
            addToList(location, key);
        }
    }

    @Override
    public void onKeyExited(String key) {

        Marker marker = markers.get(key);
        ArrayList<String> geoList = new ArrayList<>();
        geoList.add(key);
        Circle circle = circles.get(key);
        if (circle != null) {
            circle.remove();
            circles.remove(key);
            if (mGoogleApiClient.isConnected()) {
                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, geoList)
                        .setResultCallback(this);
            }
        }
        if (marker != null) {
            marker.remove();
            markers.remove(key);
        }
        eventListener(key);
    }

    private void eventListener(final String key) {

        infoRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String ownerName = dataSnapshot.getValue(String.class);

                            if(Objects.equals(ownerName, user.getDisplayName()))
                            {
                                Log.v("Child Removed", ownerName);
                                sendNotification(key);
                            }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

 // User who added the address in the database will only get notification
    private void sendNotification(String key) {
        PowerManager pm = (PowerManager) this
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();
        //Intent notificationIntent = new Intent(this, LoginActivity.class);
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        //.setContentIntent(contentIntent)
        //PendingIntent pIntent = PendingIntent.getActivity(this, 0, notificationIntent,
          //      PendingIntent.FLAG_CANCEL_CURRENT);
        PackageManager manager = getPackageManager();
        Intent launchIntent = manager.getLaunchIntentForPackage("examples.csci567.project");
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this.getApplicationContext())
                        .setSmallIcon(R.drawable.ic_delete_forever_black_24dp)
                        .setContentTitle(getString(R.string.notificationTitle))
                        .setContentText("Garbage at location " + key + "has been successfully" +
                                "cleaned. Thank you for your contribution!");
                        //.setContentIntent(contentIntent);

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);

        notificationManager.notify(23, notificationBuilder.build());
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(FirebaseError error) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public void addToList(GeoLocation location, String key) {
        Log.v("Geocode+ ", "addList called");
        geofenceList.add(new Geofence.Builder()
                .setRequestId(key)
                .setCircularRegion(location.latitude, location.longitude, 100)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                //.setLoiteringDelay(30000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                //| Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
        implement();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        //builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getPendingIntent() {
        if (pendingIntent != null)
            return pendingIntent;

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void implement() {

        if (mGoogleApiClient.isConnected()) {
            try {
                System.out.println("came inside this to add geofences");
                LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                        getGeofencingRequest(), getPendingIntent()).setResultCallback(this);
            } catch (SecurityException ex) {
                Log.e("Security Exception", ex.getStackTrace().toString());
            }
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.v("Geocode+ ", "Success!");
        } else if (status.hasResolution()) {
            // TODO Handle resolution
        } else if (status.isCanceled()) {
            Log.v("Geocode+ ", "Canceled");
        } else if (status.isInterrupted()) {
            Log.v("Geocode+ ", "Interrupted");
        } else {
            Log.v("Geocode+ ", String.valueOf(status));
        }
    }

}
            