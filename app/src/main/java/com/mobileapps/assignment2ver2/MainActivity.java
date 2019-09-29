package com.mobileapps.assignment2ver2;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.NumberPicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.provider.BaseColumns._ID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "Images";
    final String[] columns = { MediaStore.Images.Media.DATA,
            _ID };
    final String[] filePath = { MediaStore.Images.Media.DATA };
    final String orderBy = MediaStore.Images.Media.DATE_ADDED;
    final String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};

    private GridView mTiles;
    Cursor mCursor;
    int mPosition=0;
    GalleryAdapter adapter;

   // int mLastVisiblePos;

    public void init () {
        mCursor=getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,
                null, orderBy);
        mCursor.moveToFirst();


    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        // save the list position
        mPosition=mTiles.getFirstVisiblePosition();
        // close the cursor (will be opened again in init() during onResume())
        mCursor.close();

    }

    @Override
    public void onResume() {
        super.onResume();
        // reinit in case things have changed
        init();
        // set the list position
        mTiles.setSelection(mPosition);
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(String res, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }



    public class GalleryAdapter extends BaseAdapter {

        // Holds the photo imageview and it's position in the list
        class ViewHolder {
            int position;
            ImageView image;
        }


        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        // not used
        @Override
        public Object getItem(int i) {return null;}

        // not used
        @Override
        public long getItemId(int i) {
            return i;
        }
        @SuppressLint("StaticFieldLeak")
        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {


            ViewHolder vh;
            if (convertView == null) {
                // if it's not recycled, inflate it from xml
                convertView = getLayoutInflater().inflate(R.layout.tile,  viewGroup, false);
                // convertview will be a LinearLayout
                vh=new ViewHolder();
                vh.image=convertView.findViewById(R.id.tilebtn);
                // and set the tag to it
                convertView.setTag(vh);
            } else
                vh=(ViewHolder)convertView.getTag();
            vh.position = i;
            vh.image.setImageBitmap(null);
            // make an AsyncTask to load the image
            new AsyncTask<ViewHolder,Void, Bitmap>() {
                private ViewHolder vh;
                @Override
                protected Bitmap doInBackground(ViewHolder... params) {
                    vh=params[0];
                    mCursor.moveToPosition(vh.position);
                    Bitmap bmp=null;

                    try {
                        String imagePath = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));

                        //THIS BIT IS FOR ORIETNATION
                        ExifInterface exif = null;
                        try {
                            exif = new ExifInterface(imagePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);



                        // decode the jpeg into a bitmap
                        bmp = decodeSampledBitmapFromResource(imagePath, 900, 900);
                        bmp = ThumbnailUtils.extractThumbnail(bmp, 200, 200);
                        bmp = rotateBitmap(bmp, orientation);
                    } catch (Exception e) {
                        Log.i(TAG,"Error Loading:"+mCursor.getString(mCursor.getColumnIndex(filePath[0])));
                        e.printStackTrace();
                    }

                    return bmp;
                }
                @Override
                protected void onPostExecute(Bitmap bmp) {
                    // only set the imageview if the position hasn't changed.
                    if (vh.position == i) {
                        vh.image.setImageBitmap(bmp);
                    }
                }
            }.execute(vh);

            return convertView;
        }

    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        mCursor.moveToPosition(i);
        String imagePath = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
       // Bitmap bmp = BitmapFactory.decodeResource(getResources(), mCursor.getColumnIndex(_ID));
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();

       // bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

       // byte[] byteArray = stream.toByteArray();
//        Uri uri = ContentUris
//                .withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                        mCursor.getInt(mCursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)));
        Intent openImageIntent = new Intent(this, openimage.class);
//        openImageIntent.setData(uri);
        //openImageIntent.putExtra("picture", byteArray);
        openImageIntent.putExtra("path", imagePath );
        startActivity(openImageIntent);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {


        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT > 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )
            requestPermissions(
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        else
        init();
        mTiles=findViewById(R.id.galleryGridView);
        //mLastVisiblePos = mTiles.getFirstVisiblePosition();
        adapter=new GalleryAdapter();
        mTiles.setAdapter(adapter);
        mTiles.setOnItemClickListener(this);
        //mTiles.setOnScrollListener(new AbsListView.OnScrollListener());
    }
}