package com.example.neredesinsen;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class KisiEkleActivity extends AppCompatActivity {

    SQLiteDatabase database;
    EditText eklenecekmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kisi_ekle);//activity_kisi_ekle layotu ekrana göster.
        eklenecekmail = findViewById(R.id.editTextMailEkle);
    }

    public void  ekle(View view) {//xml dosyasında butona veridiğimiz onClick kısmı için yeni bir fonksiyon oluşturduk.
        Intent intent = new Intent(this, MainActivity.class);
        String mail = eklenecekmail.getText().toString();
        if (mail.matches("")){
            Toast.makeText(getApplicationContext(),"Bir Kullanıcı Giriniz!",Toast.LENGTH_LONG).show();
        }else{
            intent.putExtra("mail",mail);
            try {
                database = KisiEkleActivity.this.openOrCreateDatabase("Locations",MODE_PRIVATE,null);
                database.execSQL("CREATE TABLE IF NOT EXISTS tKisiler (id INTEGER PRIMARY KEY, mail VARCHAR)");
                String toCompile ="INSERT INTO tKisiler (mail) VALUES (?)";
                SQLiteStatement sqLiteStatement = database.compileStatement(toCompile);
                sqLiteStatement.bindString(1,mail);
                sqLiteStatement.execute();
                Toast.makeText(getApplicationContext(),"Kayıt Eklendi !",Toast.LENGTH_LONG).show();
            }catch (Exception e){
                e.printStackTrace();
            }
            startActivity(intent);
            finish();
        }

    }

    public void  vazgec(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}