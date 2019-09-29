package com.mobileapps.assignment2ver2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class openimage extends AppCompatActivity {

    ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.openimage);
        photo = findViewById(R.id.photoviewer);
       // Uri uri = getIntent().getData();
       // photo.setImageResource(getIntent().getIntExtra("img", 0));

        String photopath = getIntent().getExtras().getString("path");
        File imgFile = new File(photopath);
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());


        photo.setImageBitmap(myBitmap);


        //photo.setImageResource(imageId);
        //photo.setImageResource(getIntent().getIntExtra("imageId", 0));

    }

}
