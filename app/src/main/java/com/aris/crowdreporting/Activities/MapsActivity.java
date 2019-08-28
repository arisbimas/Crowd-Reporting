package com.aris.crowdreporting.Activities;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.aris.crowdreporting.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, DirectionCallback {

    private Button btnRequestDirection;
    private GoogleMap googleMap;
    private String serverKey = "AIzaSyCI-4RaDocwneRsw2ryTRPMf7NzGV-F1CE"; //"AIzaSyBfrkZXKYufFH2V-f-5HbO748pa-IQoW-U";
//    private LatLng origin = new LatLng(37.7849569, -122.4068855);
//    private LatLng destination = new LatLng(37.7814432, -122.4460177);

    Double lat_a, lat_b, long_a, long_b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

//        btnRequestDirection = findViewById(R.id.btnGetDirection);
//        btnRequestDirection.setOnClickListener(this);

        lat_a = getIntent().getDoubleExtra("lat_a", -6.244208);
        long_a = getIntent().getDoubleExtra("lon_a", 107.014806);
        lat_b = getIntent().getDoubleExtra("lat_b", -6.244208);
        long_b = getIntent().getDoubleExtra("lon_b", 107.014806);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapNearBy);
        mapFragment.getMapAsync(this);
        requestDirection();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);


        LatLng origin = new LatLng(lat_a, long_a);
        LatLng destination = new LatLng(lat_b, long_b);
        String descMap = getIntent().getStringExtra("descMap");

        // GANTI UKURAN Pin Maps
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.pinmu1);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        BitmapDrawable bitmapdraw2=(BitmapDrawable)getResources().getDrawable(R.drawable.pinkejadian);
        Bitmap b2=bitmapdraw2.getBitmap();
        Bitmap smallMarker2 = Bitmap.createScaledBitmap(b2, width, height, false);

        googleMap.addMarker(new MarkerOptions().position(origin).title("Lokasi Anda")
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));

        googleMap.addMarker(new MarkerOptions().position(destination).title("Lokasi Kejadian").snippet(descMap)
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker2)));
        //Zoom and Animateku
        LatLng uman =  new LatLng(lat_b,long_b);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(uman, 14));

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
//        if (id == R.id.btnGetDirection) {
//            requestDirection();
//        }
    }

    public void requestDirection() {
        LatLng origin = new LatLng(lat_a, long_a);
        LatLng destination = new LatLng(lat_b, long_b);
//        Snackbar.make(btnRequestDirection, "Direction Requesting...", Snackbar.LENGTH_SHORT).show();
        Toast.makeText(this, "Direction Requesting...", Toast.LENGTH_SHORT).show();
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
//        Snackbar.make(btnRequestDirection, "Success with status : " + direction.getStatus(), Snackbar.LENGTH_SHORT).show();
        Toast.makeText(this, "Success with status : " + direction.getStatus(), Toast.LENGTH_SHORT).show();

        if (direction.isOK()) {
            LatLng origin = new LatLng(lat_a, long_a);
            LatLng destination = new LatLng(lat_b, long_b);
            Route route = direction.getRouteList().get(0);
//            googleMap.addMarker(new MarkerOptions().position(origin).title("Lokasi Anda"));
//            googleMap.addMarker(new MarkerOptions().position(destination).title("Lokasi Kejadian"));

            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
            googleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.RED));
            setCameraWithCoordinationBounds(route);

//            btnRequestDirection.setVisibility(View.GONE);
        } else {
//            Snackbar.make(btnRequestDirection, direction.getStatus(), Snackbar.LENGTH_SHORT).show();
            Toast.makeText(this, direction.getStatus(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
//        Snackbar.make(findViewById(R.id.mapNearBy), t.getMessage(), Snackbar.LENGTH_SHORT).show();
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }
}
//    private Double lat_a = getIntent().getDoubleExtra("lat_a", -6.244208);
//    private Double long_a = getIntent().getDoubleExtra("lon_a", 107.014806);
//    private Double lat_b = getIntent().getDoubleExtra("lat_b", -6.244208);
//    private Double long_b = getIntent().getDoubleExtra("lon_b", 107.014806);
//    private Button btnRequestDirection;
//    private GoogleMap googleMap;
//    private String serverKey = "AIzaSyCQOj4Qor-Oemh7-xfsfSQbAtHhhGpBSKA";
//    private LatLng origin = new LatLng(lat_a, long_a);
//    private LatLng destination = new LatLng(lat_b, long_b);


// API
// AIzaSyCiIcIJWGuBL7gXBsJLphkZQGp-zVdohsg
// AIzaSyBfrkZXKYufFH2V-f-5HbO748pa-IQoW-U
// AIzaSyCQOj4Qor-Oemh7-xfsfSQbAtHhhGpBSKA
// AIzaSyBZPo1EqljM6CoMhEF9KlEdSRXs4ZUej2g