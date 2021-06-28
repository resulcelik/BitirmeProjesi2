package com.example.neredesinsen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class KisiDuzenleActivity extends AppCompatActivity {
    SQLiteDatabase database; //Sqllitedatabase sınıfında database isminde bir veritabanı tanılmıyoruz
    List<String> kisiList;
    ListView listView;
    ArrayAdapter arrayAdapter2;
    ArrayList<String> nameArray2;
    TextView textview;
    String gelen_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kisi_duzenle); //activity_kisi_duzenle layotu ekrana göster.
        listView = findViewById(R.id.listview_2);
        nameArray2 = new ArrayList<String>(); //nameArray2 isminde bir dizi değişkeni tanımlandı.
        arrayAdapter2 = new ArrayAdapter(KisiDuzenleActivity.this, android.R.layout.simple_list_item_1, nameArray2);
        getDataKisi2(); //kişi listesini aldı.
        textview = findViewById(R.id.duzenlenecek_mail_id);//tanımlanan textview konumuna düzenlenecek_mail_id texti gelecek.(düzenlemek istediğimiz mail)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  //listViewde seçilen elemanı textviewe aktarmak için
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { // click yapıldığı anda oluşacak işlemler....
                gelen_mail =nameArray2.get(position); //liste içerisinde bir pozisyona tıklanırsa, o pozisyondaki listelenen maili çek.
                textview.setText(gelen_mail); //textviewdeki texti gelen_mail değişkeni olarak değiştir
            }
        });
    }

    public void  getDataKisi2 (){ //Kişi listesini almak için getDataKisi2 fonksiyonu oluşturuldu.
        try { //Try - Catch yapısı kullanılarak programın çalışması esnasında bir hata oluşması durumunda bizim belirttiğimiz işlemlerin yapılmasını sağlayabiliriz.
            database = this.openOrCreateDatabase("Locations",MODE_PRIVATE,null); // eğer database varsa gerekli işlemleri yap yoksa database oluştur. Location adında database oluşturmuştuk.
            Cursor cursor = database.rawQuery("SELECT * FROM tKisiler",null); //(imleç) ---database içinde dolaşır. kişilerin içinde dolanan bir curser oluşturuldu.
            int mailIx = cursor.getColumnIndex("mail"); //mail sütununun indexleri alındı
            while (cursor.moveToNext()){ //bir sonraki index (i++ gibi çalışır.)
                String mailFromDatabase = cursor.getString(mailIx); //hangi satırdaysa onu okur ve bunu bir stringe atar
                nameArray2.add(mailFromDatabase); //okuduğu maili nameArray2 dizisine atar.
            }
            cursor.close();
            arrayAdapter2.notifyDataSetChanged(); //değişiklik olursa arrayadapterı uyar.
            listView.setAdapter(arrayAdapter2); //veriler listelendi.
        } catch (Exception e){ //Eğer veride bir hata çıkarsa bu blok çalışacak.
            e.printStackTrace(); //hata mesajını yazdırır.
        }
    }

    public void duzenle(View view){ //xml dosyasında butona veridiğimiz onClick kısmı için yeni bir fonksiyon oluşturduk.
        String mail_duzenle = textview.getText().toString(); //textviewe aktardığımız maili yeni bir string değişkenine atıyoruz.
        if(mail_duzenle.matches("")){
            Toast.makeText(getApplicationContext(),"Güncellenmek İstediğiniz Kaydı Seçiniz !",Toast.LENGTH_SHORT).show();
        }else{
            try { //Try - Catch yapısı kullanılarak programın çalışması esnasında bir hata oluşması durumunda bizim belirttiğimiz işlemlerin yapılmasını sağlayabiliriz.
                database = KisiDuzenleActivity.this.openOrCreateDatabase("Locations",MODE_PRIVATE,null); //Kişi düzenle aktivity açıldığında eğer database varsa şunu yap yoksa database oluştur. Location adında database oluşturduk.
                database.execSQL("UPDATE tKisiler SET mail = '"+mail_duzenle+"' WHERE mail = '"+gelen_mail+"' "); //uygulamada seçilen mail adresini kullanıcının girdiği yeni mail adresine günceller.
                Toast.makeText(getApplicationContext(),"Kayıt Güncellendi !",Toast.LENGTH_SHORT).show(); // Uygulamanın alt kısmında kullanıcıya Kayıt güncellendi şeklinde bir toast mesajı veriyor.
            }catch (Exception e){//Eğer veride bir hata çıkarsa bu blok çalışacak.
                e.printStackTrace();//hata çıkmışsa eğer hatayı konsola yazdırır.
            }
            Intent intent_main = new Intent(this, MainActivity.class);  //yeni bir intent oluşturduk. işlemler yapıldıktan sonra main_activiy sayfasına yönlendirecek.
            startActivity(intent_main);  //main_activitye dönmeyi başlatır.
            finish();
        }

    }
    public void  vazgec_duzenle(View view) {
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