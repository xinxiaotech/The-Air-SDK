package com.hackplan.theairsdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

class Constants {

    static final int THEME_PRIMARY_COLOR_DEFAULT = 0xFF0086FA;
    static final String THEME_PRIMARY_COLOR = "primaryColor";
    static final String APK_DOWNLOAD_URL = "url";
    static final String APK_UPDATE_CONTENT = "log";
    static final String APK_VERSION_CODE = "build";
    static final String APK_VERSION_NAME = "version";
    static final String APK_FORCE = "force";

    static String getUpdateUrl(Context c, boolean important) {
        int versionCode = 0;
        String packageName = c.getPackageName();
        try {
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(packageName, 0);
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        String url = getBaseServerUrl(packageName)
                + "?current_version="
                + String.valueOf(versionCode)
                + (important ? "&important=1" : "");

        return url;
    }

    private static String getBaseServerUrl(String packageName) {
        return String.format("http://air.hackplan.com/v1/p/%s/latest.json", packageName);
    }
}
