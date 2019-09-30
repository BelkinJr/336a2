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

        //get path of the image as a string
        String photopath = getIntent().getExtras().getString("path");
        File imgFile = new File(photopath);
        //decode image
        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

        photo.setImageBitmap(myBitmap);

    }

}
