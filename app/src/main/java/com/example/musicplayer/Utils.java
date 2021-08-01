package com.example.musicplayer;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static void getCover(Context context, ImageView imageView,AudioModel model){
        Uri uri= ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),model.getMusicAlbum_ID());
        Glide.with(context).load(uri).centerCrop()
                .placeholder(R.drawable.ic_baseline_music_note_24)
                .into(imageView);
    }

    public static String getFullTime(int duration){
        return String.format(Locale.ENGLISH,"%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration)%60);
    }
    public static Bitmap getCoverBitmap(Context context,long ID){
        MediaMetadataRetriever metadataRetriever=new MediaMetadataRetriever();
        metadataRetriever.setDataSource(context,ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,ID));

        InputStream inputStream=null;
        if (metadataRetriever.getEmbeddedPicture()!=null){
            inputStream=new ByteArrayInputStream(metadataRetriever.getEmbeddedPicture());
        }
        metadataRetriever.close();

        return BitmapFactory.decodeStream(inputStream);
    }
}
