package com.example.neredesinsen;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MapsActivity3 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationmanager;
    LocationListener locationListener;

    SQLiteDatabase database;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    ArrayList<Konum> konumList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        konumList = new ArrayList<Konum>();
        locationmanager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();

        if (intent.getStringExtra("info").matches("other")){
            String other_mail = intent.getStringExtra("other_mail");
            System.out.println("maps3 activitesindeyim");

            getData(other_mail);

            System.out.println("Map 3 ye geçildi");;

        }

    }

    public void getData(String other_mail){
        firebaseFirestore.collection("Locations")
                .whereEqualTo("mail",other_mail)
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Map<String,Object> veri = document.getData();
                                String mailFromFirebase  = (String)veri.get("mail");
                                Double latitudeFromFirebase  = (Double)veri.get("latitude");
                                Double longitudeFromFirebase  = (Double)veri.get("longitude");

                                Timestamp timeFromFirebase = (Timestamp) veri.get("time");

                                Date ldt = timeFromFirebase.toDate();

                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                String time = sdf.format(ldt);
                                System.out.println("time : "+ time);

                                // String timeFromFirebase = (String) veri.get("time");
                                //System.out.println(veri.get("time"));
                                System.out.println(">>" + mailFromFirebase+" "+latitudeFromFirebase+" "+longitudeFromFirebase+" "+time);

                                LatLng konum = new LatLng(latitudeFromFirebase, longitudeFromFirebase);
                                mMap.addMarker(new MarkerOptions().position(konum).title(time+" "+ mailFromFirebase));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(konum,14));

                            }
                        } else {
                            System.out.println("Error getting documents."+ task.getException());
                        }
                    }
                });




    }
}