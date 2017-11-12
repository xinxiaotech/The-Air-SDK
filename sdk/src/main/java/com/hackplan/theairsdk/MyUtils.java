package com.hackplan.theairsdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * Created by Dacer on 11/11/2017.
 */

class MyUtils {
    static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString()
                : context.getString(stringId);
    }
}
