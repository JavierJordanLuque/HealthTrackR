package com.javierjordanluque.healthtrackr.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;

import com.javierjordanluque.healthtrackr.models.Location;

public class NavigationUtils {
    private static final String USER_MANUAL_URL = "https://github.com/JavierJordanLuque/HealthTrackR/tree/main";

    public static void openUserManual(Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(USER_MANUAL_URL));
        intent.putExtra(Browser.EXTRA_HEADERS, "Accept-Language: " + context.getResources().getConfiguration().getLocales().get(0).getLanguage());
        context.startActivity(intent);
    }

    public static void openGoogleMaps(Context context, Location location) {
        String encodedLocation;
        if (location.getPlace() != null) {
            encodedLocation =  Uri.encode(location.getPlace());
        } else if (location.getLatitude() != null && location.getLongitude() != null) {
            encodedLocation = location.getLatitude() + "," + location.getLongitude();
        } else {
            return;
        }

        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + encodedLocation);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            Uri webIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + encodedLocation);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webIntentUri);
            context.startActivity(webIntent);
        }
    }
}
