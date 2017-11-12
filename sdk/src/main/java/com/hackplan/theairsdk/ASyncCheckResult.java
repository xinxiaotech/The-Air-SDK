package com.hackplan.theairsdk;

/**
 * Created by Dacer on 5/11/14.
 */
interface ASyncCheckResult {

    void upToDate();

    void versionDownloadableFound(String link, String msg, int versionCode, String versionName,
            boolean isForce);

    void aSyncCheckError(String error);
}
