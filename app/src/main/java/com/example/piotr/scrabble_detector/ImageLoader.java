package com.example.piotr.scrabble_detector;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by piotr on 1/16/18.
 */

class ImageLoader {

    ImageLoader(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    private Uri imageUri;
    private Bitmap bitmap;
    private ContentResolver contentResolver;

    Uri getImageUri() {
        return imageUri;
    }

    Bitmap getBitmap() {
        return bitmap;
    }

    void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }



    void loadBitmap(int request, Intent data){
        if(request ==  Request.IMAGE_CAPTURE){
            loadBitmapFromCamera();
        }
        if(request == Request.LOAD_IMAGE){
            loadBitmapFromGallery(data);
        }
    }


    private void loadBitmapFromCamera () {
        Log.i("Camera", "Opening camera");
        contentResolver.notifyChange(imageUri, null);
        try {
            bitmap = android.provider.MediaStore.Images.Media
                    .getBitmap(contentResolver, imageUri);
            bitmap = ExifUtil.rotateBitmap(imageUri.getPath(), bitmap);
        } catch (Exception e) {
            Log.e("Camera", e.toString());
        }
    }

    private void loadBitmapFromGallery(Intent data) {
        imageUri = data.getData();
        Log.i("Gallery", "Opening gallery");
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        if (imageUri != null) {
            try {
                Cursor cursor = contentResolver.query(imageUri,
                        filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String mCurrentPhotoPath = cursor.getString(columnIndex);
                    cursor.close();
                    bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
                    bitmap = ExifUtil.rotateBitmap(mCurrentPhotoPath, bitmap);
                }
            } catch (Exception e) {
                Log.e("Gallery", e.toString());
            }
        }
    }


}
