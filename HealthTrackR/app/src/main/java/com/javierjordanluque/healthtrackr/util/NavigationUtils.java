package com.javierjordanluque.healthtrackr.util;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.widget.Toast;

import com.javierjordanluque.healthtrackr.R;

public class NavigationUtils {
    private static final String USER_MANUAL_URL = "https://github.com/JavierJordanLuque/HealthTrackR/tree/main";
    public static void openUserManual(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(USER_MANUAL_URL));
        context.startActivity(intent);
    }

    public static void openGoogleMaps(Context context, double latitude, double longitude) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            Uri webIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webIntentUri);
            context.startActivity(webIntent);
        }
    }
}
