package com.technophile.shapeovermap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, View.OnTouchListener {

    private static final long LOCATION_REFRESH_TIME = 0;
    private static final float LOCATION_REFRESH_DISTANCE = 0;
    private static final String MAP_DATA = "mapData";
    private static final String TAG = "Map Activity";
    ArrayList<LatLng> latLngs = new ArrayList<>();
    PolylineOptions polylineOptions;
    ArrayList<Polyline> polylines = new ArrayList<>();
    private MapView mapView;
    private GoogleMap googleMap;
    private boolean alignToLocation = true;
    private FloatingActionButton fab_btn;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            /*Animate camera to current location*/
            if (alignToLocation) {
                LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
                alignToLocation = false;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {


        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    private boolean canDrawPolygon = false;
    private Polygon polygon;
    private TextView tv_mesg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapView = (MapView) findViewById(R.id.map_view);
        tv_mesg = (TextView) findViewById(R.id.tv_mesg);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_DATA);
        }
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
        InitLocationManager();
        fab_btn = (FloatingActionButton) findViewById(R.id.fab_btn);
        fab_btn.setOnClickListener(this);
        CustomFrame fl_clicker = (CustomFrame) findViewById(R.id.fl_clicker);
        fl_clicker.setOnTouchListener(this);

    }

    private void InitLocationManager() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
            Log.d(TAG, "InitLocationManager: Request permissions");
            return;

        }
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, locationListener);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        InitLocationManager();
                    }
                } else {
                    Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /*Save state for configuration changes*/
        Bundle mapViewBundle = outState.getBundle(MAP_DATA);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_DATA, mapViewBundle);
        }
        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null)
            mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_btn:
                /*Remove shape on cancel*/
                if (polygon != null) {
                    polygon.remove();
                    polygon = null;
                    canDrawPolygon = true;
                }
                /*Change icon on click*/
                fab_btn.setImageResource(canDrawPolygon ? R.drawable.ic_touch_app_white_24dp : R.drawable.ic_close_white_24dp);
                tv_mesg.setVisibility(canDrawPolygon ? View.GONE : View.VISIBLE);
                canDrawPolygon = !canDrawPolygon;
                break;
        }
    }

    private void drawOnMapT(LatLng latLng) {
        if (polylineOptions == null) {
            polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.BLACK).width(5);
        }
        polylineOptions.add(latLng);
        Polyline polyline = googleMap.addPolyline(polylineOptions);
        polylines.add(polyline);
    }

    private void drawOnMap() {

        /*Remove polyline before drawing the polygon*/
        for (Polyline polyline : polylines) {
            polyline.remove();
        }
        polylines.clear();
        polylineOptions = null;

        /*Stop drawing*/
        canDrawPolygon = !canDrawPolygon;
        tv_mesg.setVisibility(View.GONE);
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(latLngs);
        polygonOptions.strokeColor(getResources().getColor(R.color.colorTintAccent)).strokeWidth(5).fillColor(getResources().getColor(R.color.colorTransparentAccent));
        polygon = googleMap.addPolygon(polygonOptions);
    }


    @Override
    public boolean onTouch(View view, MotionEvent dragEvent) {
        Log.d(TAG, "onDrag: X" + String.valueOf(dragEvent.getX()));
        Log.d(TAG, "onDrag: Y" + String.valueOf(dragEvent.getY()));

        int xCords = Integer.parseInt(String.valueOf(Math.round(dragEvent.getX())));
        int yCords = Integer.parseInt(String.valueOf(Math.round(dragEvent.getY())));

        Point xyPoints = new Point(xCords, yCords);
        LatLng latLng = googleMap.getProjection().fromScreenLocation(xyPoints);

        switch (dragEvent.getAction()) {
            case ACTION_DOWN:
                latLngs.clear();
                latLngs.add(latLng);
                break;
            case ACTION_MOVE:
                /*Draw line on move*/
                drawOnMapT(latLng);
                latLngs.add(latLng);
                break;
            case ACTION_UP:
                view.performClick();
                latLngs.add(latLng);
                drawOnMap();
                break;
        }
        return canDrawPolygon;
    }
}
