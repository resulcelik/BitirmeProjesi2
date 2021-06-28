package com.example.neredesinsen;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class KisiSilActivity extends AppCompatActivity {
    SQLiteDatabase database;
    List<String> kisiList;
    ListView listView;
    ArrayAdapter arrayAdapter3;
    ArrayList<String> nameArray3;
    TextView textview;
    String gelen_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kisi_sil); //activity_kisi_sil layotu ekrana göster.
        listView = findViewById(R.id.listview_3);
        nameArray3 = new ArrayList<String>(); //nameArray3 isminde bir dizi değişkeni tanımlandı.
        arrayAdapter3 = new ArrayAdapter(KisiSilActivity.this, android.R.layout.simple_list_item_1, nameArray3);
        getDataKisi3();//kisi listesi alında
        textview = findViewById(R.id.silinecek_mail_id); //tanımlanan textview konumuna düzenlenecek_mail_id texti gelecek.(silmek istediğimiz mail)
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {  //listViewde seçilen elemanı textviewe aktarmak için
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { // click yapıldığı anda oluşacak işlemler....
                gelen_mail =nameArray3.get(position); //liste içerisinde bir pozisyona tıklanırsa, o pozisyondaki listelenen maili çek.
                textview.setText(gelen_mail); //textviewdeki texti gelen_mail değişkeni olarak değiştir
            }
        });
    }

    public void  getDataKisi3 (){ //Kişi listesini almak için getDataKisi3 fonksiyonu oluşturuldu.
        try { //Try - Catch yapısı kullanılarak programın çalışması esnasında bir hata oluşması durumunda bizim belirttiğimiz işlemlerin yapılmasını sağlayabiliriz.
            database = this.openOrCreateDatabase("Locations",MODE_PRIVATE,null);// eğer database varsa gerekli işlemleri yap yoksa database oluştur. Location adında database oluşturmuştuk.
            Cursor cursor = database.rawQuery("SELECT * FROM tKisiler",null); //(imleç) ---database içinde dolaşır. kişilerin içinde dolanan bir curser oluşturuldu.
            int mailIx = cursor.getColumnIndex("mail");//mail sütununun indexleri alındı
            while (cursor.moveToNext()){//bir sonraki index (i++ gibi çalışır.)
                String mailFromDatabase = cursor.getString(mailIx);//hangi satırdaysa onu okur ve bunu bir stringe atar
                nameArray3.add(mailFromDatabase);//okuduğu maili nameArray3 dizisine atar.
            }
            arrayAdapter3.notifyDataSetChanged();//değişiklik olursa arrayadapterı uyar.
            listView.setAdapter(arrayAdapter3);//veriler listelendi.
            cursor.close();
        } catch (Exception e){//Eğer veride bir hata çıkarsa bu blok çalışacak.
            e.printStackTrace();//hata mesajını yazdırır.
        }
    }


    public void sil(View view) {//xml dosyasında butona veridiğimiz onClick kısmı için yeni bir fonksiyon oluşturduk.
        String mail_delete = textview.getText().toString();//textviewe aktardığımız maili yeni bir string değişkenine atıyoruz.
        if (mail_delete.matches("")){
            Toast.makeText(getApplicationContext(),"Silmek İstediğiniz Kullanıcıyı Seçiniz!",Toast.LENGTH_LONG).show();
        }else{
            AlertDialog.Builder alrt = new AlertDialog.Builder(this);
            alrt.setTitle("UYARI");//uyarı başlığını belirler.
            alrt.setMessage(mail_delete + " kişisini silmek istediğinize emin misiniz?" );//uyarı mesajını gösterir.
            alrt.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {//olumsuz button çalıştığında şu işlemler yapılır....
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(KisiSilActivity.this, "Silmekten vazgeçitiniz", Toast.LENGTH_LONG).show();
                }
            });
            alrt.setPositiveButton("Evet", new DialogInterface.OnClickListener() {//olumlu button çalıştığında şu işlemler yapılır....
                @Override
                public void onClick(DialogInterface dialog, int which) {//TEKRAR BAK CATCH BLOGUNA
                    try { //Try - Catch yapısı kullanılarak programın çalışması esnasında bir hata oluşması durumunda bizim belirttiğimiz işlemlerin yapılmasını sağlayabiliriz.
                        database = KisiSilActivity.this.openOrCreateDatabase("Locations",MODE_PRIVATE,null);//Kişi sil aktivity açıldığında eğer database varsa şunu yap yoksa database oluştur. Location adında database oluşturduk.
                        database.execSQL("DELETE FROM tKisiler WHERE mail = '" + mail_delete + "'");//uygulamada seçilen mail adresini veritabanından siler.
                        Toast.makeText(getApplicationContext(),"Kayıt Silindi !",Toast.LENGTH_LONG).show();// Uygulamanın alt kısmında kullanıcıya Kayıt silindi şeklinde bir toast mesajı veriyor.
                    }catch (Exception e){//Eğer veride bir hata çıkarsa bu blok çalışacak.
                        e.printStackTrace();
                    }
                    Intent intent_main = new Intent(KisiSilActivity.this, MainActivity.class);//yeni bir intent oluşturduk. işlemler yapıldıktan sonra main_activiy sayfasına yönlendirecek.
                    startActivity(intent_main); //main_activitye dönmeyi başlatır.
                    finish();
                }
            });
            alrt.show();
        }

    }

    public void  vazgec_sil(View view) {
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