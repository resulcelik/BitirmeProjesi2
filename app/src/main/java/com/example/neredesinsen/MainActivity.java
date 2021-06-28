package com.example.neredesinsen;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private FirebaseFirestore firebaseFirestore;


    private GoogleMap mMap;
    LocationManager locationmanager;
    LocationListener locationListener;

    SQLiteDatabase database;
    ArrayList<String> nameArray;
    ArrayList<Integer> idArray;
    List<Konum> konumList;
    List<String> kisiList;
    ListView listView;
    ArrayAdapter arrayAdapter;

    SharedPreferences sp;
    Switch sw_konum;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // ayarlar menusne geçiş
        if (item.getItemId() == R.id.setting_id) {
            Intent intentToUpload = new Intent(MainActivity.this, InfoActivity.class);
            intentToUpload.putExtra("info","setting");
            startActivity(intentToUpload);

            // çıkış işlemleri
        } else if (item.getItemId() == R.id.signout_id) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Hesaptan Çıkış Yapmak istiyor musunuz?");
            builder.setPositiveButton("EVET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sp.edit().putBoolean("sw_check",false).apply();
                    firebaseAuth.signOut();
                    Intent intentToSignIN = new Intent(MainActivity.this, SignUpActivity.class);
                    intentToSignIN.putExtra("info","signin");
                    startActivity(intentToSignIN);
                    finish();
                }
            });
            builder.setNegativeButton("HAYIR", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        // diğer menuler için intent ekleyebilir


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        sw_konum = findViewById(R.id.switch2);

        listView = findViewById(R.id.listTextKisiListesi);
        nameArray = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArray);
        listView.setAdapter(arrayAdapter);
        getDataKisi();

        sp = MainActivity.this.getSharedPreferences("com.projem.neredesinsen",MODE_PRIVATE);
        boolean sw_check = sp.getBoolean("sw_check",false);
        sw_konum.setChecked(sw_check);
        konumkontrolleri();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent_other = new Intent(getApplicationContext(),MapsActivity4.class);
                intent_other.putExtra("info","other");
                intent_other.putExtra("other_mail",nameArray.get(position));
                startActivity(intent_other);
                finish();
            }
        });

    }

    public void konumkontrolleri(){
        // BU İF blogunu iki kere kullandık daha güzel bi algoritma yaz
        if(sw_konum.isChecked()){
            System.out.println("çalıştı konum açık");
            sp.edit().putBoolean("sw_check",true).apply();
            konumal();
        }else{
            System.out.println("konum kapalı");
            sp.edit().putBoolean("sw_check",false).apply();
        }
        sw_konum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_konum.isChecked()){
                    System.out.println("çalıştı konum açık");
                    sp.edit().putBoolean("sw_check",true).apply();
                    konumal();

                }else{
                    System.out.println("konum kapalı");
                    sp.edit().putBoolean("sw_check",false).apply();
                }
            }
        });
    }
    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...

            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    sw_konum.setChecked(false);
                    sp.edit().putBoolean("sw_check",false).apply();
                    try {
                        // Konum ayarları iyi değil, düzeltilebilir

                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    public void konumal() {
        locationmanager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText( getApplicationContext(),"GPS Veri bilgileri Alınıyor...",Toast.LENGTH_SHORT).show();
                sw_konum.setChecked(true);
                sp.edit().putBoolean("sw_check",true).apply();
            }
            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText( getApplicationContext(),"GPS Bağlantı Bekleniyor... "+"\n"+"Konumunuz Kapalı Olabilir...",Toast.LENGTH_SHORT).show();
                createLocationRequest();
            }

            @Override
            public void onLocationChanged(@NonNull Location location) {
                //mMap.clear();
                if(sw_konum.isChecked()){
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    String mail = firebaseUser.getEmail();
                    Konum konumum = new Konum(mail,location.getLatitude(),location.getLongitude());
                    System.out.println("konum: "+konumum.mail+ " " + konumum.latitude +" "+ konumum.longitude);
                    // burda konumu SQLite veri tabanına göndericez
                    // mode_private kısmını değiştirilmesi gerekebilir.

                    HashMap<String, Object> konumHash = new HashMap<String, Object>();
                    konumHash.put("mail",mail);
                    konumHash.put("latitude",konumum.latitude);
                    konumHash.put("longitude", konumum.longitude);
                    konumHash.put("time", FieldValue.serverTimestamp());
                    System.out.println("haş çalıştı");
                    firebaseFirestore.collection("Locations")
                            .add(konumHash)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    System.out.println("firebaseFirestore çalıştı");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("firebaseFirestore çalışmadı");
                        }
                    });
                }
            }
        };
        // kullanıcıdan location izni alma
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // permissin izinleri
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else{
            // location işlemleri
            locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,10,locationListener);
        }
    }

    public void konumagit(View View) {
        Intent intent_me = new Intent(MainActivity.this, com.example.neredesinsen.MapsActivity.class);
        intent_me.putExtra("info", "me");
        startActivity(intent_me);
        finish();
    }

    public void  getDataKisi (){
        try {
            database = this.openOrCreateDatabase("Locations",MODE_PRIVATE,null);
            Cursor cursor = database.rawQuery("SELECT * FROM tKisiler",null);
            int mailIx = cursor.getColumnIndex("mail");
            while (cursor.moveToNext()){
                String mailFromDatabase = cursor.getString(mailIx);
                nameArray.add(mailFromDatabase);
            }
            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void kisiekleyegit(View view){//kişi eklemek için kisikleyegit fonksiyonu tanımlandı.
        Intent intent_ekle = new Intent(this, KisiEkleActivity.class);//yeni bir intent oluşturuldu. kisiekleactivitye yönlendirilecek
        startActivity(intent_ekle);//intent_ekle başlatılacak.
        finish();
    }

    public void kisiduzenleyegit(View view){//kişiyi düzenlemek için kisiduzenleyegit fonksiiyonu tanımlandı.
        Intent intent_duzenle = new Intent(this,KisiDuzenleActivity.class);//yeni bir intent oluşturuldu. kisidüzenleactivitye yönlendirilecek.
        startActivity(intent_duzenle);//intent_duzenle başlatılacak.
        finish();
    }

    public void kisisilegit(View view){//kişiyi silmek için kisisilegit fonksiyonu tanımlandı.
        Intent intent_sil = new Intent(this,KisiSilActivity.class);//yeni bir intent oluşturuldu. kisisilactivitye yönlendirilecek.
        startActivity(intent_sil);//intent_sil activiy başlatılacak
        finish();
    }

    @Override public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Uygulamadan Çıkış Yapmak istiyor musunuz?");
        builder.setPositiveButton("EVET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("HAYIR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}