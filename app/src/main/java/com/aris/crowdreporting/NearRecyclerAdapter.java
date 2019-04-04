package com.aris.crowdreporting;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.LatLng;

import java.text.DecimalFormat;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.constraint.Constraints.TAG;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class NearRecyclerAdapter extends RecyclerView.Adapter<NearRecyclerAdapter.ViewHolder> {

    public List<Near> near_list;
    public List<User> userList;

    private FusedLocationProviderClient client;

    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    LocationManager locationManager;
    private static final String TAG = "MyActivity";


    public NearRecyclerAdapter(List<Near> near_list) {

        this.near_list = near_list;
//        this.userList = userList;


    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.near_row, viewGroup, false);
        context = viewGroup.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new NearRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        String desc_data = near_list.get(i).getDesc();
        viewHolder.setNearDescText(desc_data);

        String image_url = near_list.get(i).getImage_url();
        String thumbUri = near_list.get(i).getImage_thumb();
        viewHolder.setBlogImage(image_url, thumbUri);

        String user_id = near_list.get(i).getUser_id();
        //User Data will be retrieved here...
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    String userName = task.getResult().getString("name");
//                    String userImage = task.getResult().getString("image");

                    viewHolder.setUserData(userName);


                } else {

                    //Firebase Exception

                }

            }
        });


        client = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        client.getLastLocation().addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null){

                    Double lat_a = location.getLatitude();
                    Double lng_a = location.getLongitude();

                    String ll = near_list.get(i).getLatitude();
                    Double lat_b = Double.parseDouble(ll);
                    String lg = near_list.get(i).getLongitude();
                    Double lng_b = Double.parseDouble(lg);
                    String descMap = near_list.get(i).getDesc();

                    viewHolder.getDistance(lat_a, lng_a, lat_b, lng_b);

                    viewHolder.seeMap.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intentMap = new Intent(context, MapsActivity.class);
                            intentMap.putExtra("lat_a", lat_a);
                            intentMap.putExtra("lon_a", lng_a);
                            intentMap.putExtra("lat_b", lat_b);
                            intentMap.putExtra("lon_b", lng_b);
                            intentMap.putExtra("descMap", descMap);
                            context.startActivity(intentMap);
                        }
                    });

                }

            }
        });




    }


    @Override
    public int getItemCount() {
        return near_list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView nearDesc;
        private TextView blogUserName;
        private CircleImageView blogUserImage;
        private TextView nearU;
        private ImageView blogImageView, seeMap;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            nearU = mView.findViewById(R.id.jarak);
            seeMap = mView.findViewById(R.id.see_map);
        }

        public void setNearDescText(String descText) {

            nearDesc = mView.findViewById(R.id.near_desc);
            nearDesc.setText(descText);

        }

        public void setUserData(String name) {

//            blogUserImage = mView.findViewById(R.id.near_user_image);
            blogUserName = mView.findViewById(R.id.near_username);

            blogUserName.setText(name);

//            RequestOptions placeholderOption = new RequestOptions();
//            placeholderOption.placeholder(R.drawable.profile_placeholder);
//
//            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);

        }

        public void setBlogImage(String downloadUri, String thumbUri){

            blogImageView = mView.findViewById(R.id.near_user_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.profile_placeholder);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(blogImageView);

        }

//        public void setNearU(Double latitude) {
//            nearU = mView.findViewById(R.id.jarak);
//            nearU.setText("" + latitude);
//
//        }
//
//        public void getDistanceBetween(Double latitude1, Double longitude1, Double latitude2, Double longitude2 )
//        {
//            Double theta = longitude1 - longitude2;
//            Double distance = (sin(Math.toRadians(latitude1)) * sin(Math.toRadians(latitude2)))  + (cos(Math.toRadians(latitude1)) * cos(Math.toRadians(latitude2)) * cos(Math.toRadians(theta)));
//            distance = acos(distance);
//            distance = Math.toDegrees(distance);
//            Double m = distance * 60 * 1.1515;
//            Double km = distance * 1.609344;
//
//            DecimalFormat decimalFormat = new DecimalFormat("#.##");
//            String format = decimalFormat.format(m);
//
//
//            nearU = mView.findViewById(R.id.jarak);
//
//            nearU.setText(""+format);
//
//        }


        public void getDistance(Double lat_a, Double lng_a, Double lat_b, Double lng_b) {
            // earth radius is in mile
            double earthRadius = 3958.75;
            double latDiff = Math.toRadians(lat_b - lat_a);
            double lngDiff = Math.toRadians(lng_b - lng_a);
            double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                    + Math.cos(Math.toRadians(lat_a))
                    * Math.cos(Math.toRadians(lat_b)) * Math.sin(lngDiff / 2)
                    * Math.sin(lngDiff / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = earthRadius * c;

            int meterConversion = 1609;
            double kmConvertion = 1.6093;
            // return new Float(distance * meterConversion).floatValue();
//            return String.format("%.2f", new Float(distance * kmConvertion).floatValue()) + " km";
            String fin = String.format("%.2f", new Float(distance * kmConvertion).floatValue()) + " km";
            nearU = mView.findViewById(R.id.jarak);

            nearU.setText(""+ fin);
            // return String.format("%.2f", distance)+" m";
        }
    }
}