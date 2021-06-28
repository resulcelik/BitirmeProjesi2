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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {

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
            System.out.println("maps2 activitesindeyim");



            getData(other_mail);
            System.out.println("Map 2 ye geçildi");;

            // diğer kişilerin konum işlemleri burda yapılacak
            // Add a marker in Sydney and move the camera
            int i= konumList.size()-1;
            while (i>=0){
                LatLng konum = new LatLng(konumList.get(i).latitude, konumList.get(i).longitude);
                mMap.addMarker(new MarkerOptions().position(konum).title(konumList.get(i).mail));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(konum,15));
                i = i-1;
            }

        }

    }

    public void getData(String other_mail){

        try {
            database = this.openOrCreateDatabase("Locations",MODE_PRIVATE,null);

            Cursor cursor = database.rawQuery("SELECT * FROM tLocation Where mail = '"+other_mail+"'",null);
            int idIx = cursor.getColumnIndex("id");
            int mailIx = cursor.getColumnIndex("mail");
            int latiIX = cursor.getColumnIndex("latitude");
            int longIx = cursor.getColumnIndex("longitude");

            while (cursor.moveToNext()){
                int idFromDatabase = cursor.getInt(idIx);
                String mailFromDatabase = cursor.getString(mailIx);
                Double latitudeFromDatabase = cursor.getDouble(latiIX);
                Double longitudeFromDatabase = cursor.getDouble(longIx);
                System.out.println("konum databaseden : "+idFromDatabase+" "+mailFromDatabase+" "+latitudeFromDatabase +" "+ longitudeFromDatabase);
                Konum konum = new Konum(mailFromDatabase,latitudeFromDatabase,longitudeFromDatabase);
                konumList.add(konum);

            }
            cursor.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        /*
        CollectionReference collectionReference = firebaseFirestore.collection("Locations");
        collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    System.out.println("Firebaseden okumada sorun var");
                }
                System.out.println("Firebaseden okumada sorun yok");
                if (value != null){
                    System.out.println("value null değil");
                    for (DocumentSnapshot snapshot : value.getDocuments()){
                        System.out.println("for döngüsü");
                        Map<String, Object> data =  snapshot.getData();
                        String mail = (String) data.get("mail");
                        String latitude = (String) data.get("latitude");
                        String longitude = (String) data.get("longitude");

                        System.out.println(mail);

                    }
                }
            }
        });
        */
        firebaseFirestore.collection("Locations")
                .whereEqualTo("mail",other_mail)
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                System.out.println(" => " + document.getData());
                            }
                        } else {
                            System.out.println("Error getting documents."+ task.getException());
                        }
                    }
                });




    }
}