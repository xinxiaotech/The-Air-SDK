package com.hackplan.theairsdk;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateService extends IntentService {
    private static final int BUFFER_SIZE = 10 * 1024; // 8k ~ 32K
    private static final String TAG = "UpdateService";
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public UpdateService() {
        super("UpdateService");
    }

    @Override protected void onHandleIntent(Intent intent) {

        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, "1");

        mBuilder.setContentTitle(
                getString(R.string.downloading_with_app_name, MyUtils.getApplicationName(this)))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setTicker(getString(R.string.downloading_with_app_name,
                        MyUtils.getApplicationName(this)))
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(),
                        PendingIntent.FLAG_CANCEL_CURRENT))
                .setOngoing(true);
        mNotifyManager.notify(0, mBuilder.build());
        String urlStr = intent.getStringExtra(Constants.APK_DOWNLOAD_URL);
        InputStream in = null;
        FileOutputStream out = null;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();
            long byteTotal = urlConnection.getContentLength();
            long byteSum = 0;
            int byteRead;
            in = urlConnection.getInputStream();
            File dir = getExternalCacheDir();
            String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
            File apkFile = new File(dir, apkName);
            out = new FileOutputStream(apkFile);
            byte[] buffer = new byte[BUFFER_SIZE];

            int oldProgress = 0;

            while ((byteRead = in.read(buffer)) != -1) {
                byteSum += byteRead;
                out.write(buffer, 0, byteRead);

                int progress = (int) (byteSum * 100L / byteTotal);
                if (progress != oldProgress) {
                    updateProgress(progress);
                }
                oldProgress = progress;
            }

            Intent installAPKIntent = new Intent(Intent.ACTION_VIEW);
            String[] command = { "chmod", "777", apkFile.toString() };
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.start();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                installAPKIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = TheAirFileProvider.getUriForFile(this,
                        getPackageName() + ".com.hackplan.fileprovider", apkFile);
                installAPKIntent.setDataAndType(contentUri,
                        "application/vnd.android.package-archive");
            } else {
                installAPKIntent.setDataAndType(Uri.fromFile(apkFile),
                        "application/vnd.android.package-archive");
                installAPKIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            startActivity(installAPKIntent);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, installAPKIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentText(getString(R.string.download_success))
                    .setProgress(0, 0, false)
                    .setContentIntent(pendingIntent)
                    .setOngoing(false);
            Notification notification = mBuilder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            mNotifyManager.notify(0, notification);
        } catch (Exception e) {
            Log.e(TAG, "download apk file error", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void updateProgress(int progress) {
        mBuilder.setContentText(this.getString(R.string.download_progress, progress))
                .setProgress(100, progress, false);
        PendingIntent pendingintent =
                PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingintent);
        mNotifyManager.notify(0, mBuilder.build());
    }
}
