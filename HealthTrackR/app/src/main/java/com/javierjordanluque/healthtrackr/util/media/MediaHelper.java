package com.javierjordanluque.healthtrackr.util.media;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MediaHelper {
    public static List<Media> getImages(ContentResolver contentResolver) {
        List<Media> images = new ArrayList<>();
        Uri collectionUri;
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.MIME_TYPE
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collectionUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        try (android.database.Cursor cursor = contentResolver.query(
                collectionUri,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);

                while (cursor.moveToNext()) {
                    Uri uri = ContentUris.withAppendedId(collectionUri, cursor.getLong(idColumn));
                    String name = cursor.getString(displayNameColumn);
                    long size = cursor.getLong(sizeColumn);
                    String mimeType = cursor.getString(mimeTypeColumn);

                    Media image = new Media(uri, name, size, mimeType, null);
                    images.add(image);
                }
            }
        }

        return images;
    }

    public static List<Media> getVideos(ContentResolver contentResolver) {
        List<Media> videos = new ArrayList<>();
        Uri collectionUri;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DURATION
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collectionUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        try (android.database.Cursor cursor = contentResolver.query(
                collectionUri,
                projection,
                null,
                null,
                MediaStore.Video.Media.DATE_ADDED + " DESC")) {

            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);

                while (cursor.moveToNext()) {
                    Uri uri = ContentUris.withAppendedId(collectionUri, cursor.getLong(idColumn));
                    String name = cursor.getString(displayNameColumn);
                    long size = cursor.getLong(sizeColumn);
                    String mimeType = cursor.getString(mimeTypeColumn);
                    long duration = cursor.getLong(durationColumn);

                    Media video = new Media(uri, name, size, mimeType, duration);
                    videos.add(video);
                }
            }
        }

        return videos;
    }
}
