package com.hackplan.theairsdk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;
import java.io.IOException;

public class UpdateService extends Service {
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private final static int NOTI_ID = 23498;
    private final static String NOTI_CHANNEL_ID = "the_air";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String urlStr = intent.getStringExtra(Constants.APK_DOWNLOAD_URL);
        FileDownloader.setup(this);
        File dir = getExternalCacheDir();
        String apkName = urlStr.substring(urlStr.lastIndexOf("/") + 1, urlStr.length());
        final File apkFile = new File(dir, apkName);


        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, NOTI_CHANNEL_ID);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTI_CHANNEL_ID,
                    "OTA", NotificationManager.IMPORTANCE_HIGH);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
        mBuilder.setContentTitle(
                getString(R.string.downloading_with_app_name, MyUtils.getApplicationName(this)))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setTicker(getString(R.string.downloading_with_app_name,
                        MyUtils.getApplicationName(this)))
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(),
                        PendingIntent.FLAG_CANCEL_CURRENT))
                .setOngoing(true);
        mNotifyManager.notify(NOTI_ID, mBuilder.build());

        FileDownloader.getImpl().create(urlStr)
                .setPath(apkFile.getPath())
                .setForceReDownload(true)
                .setListener(new FileDownloadListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        if (totalBytes != 0) updateProgress(soFarBytes * 100 / totalBytes);
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                        if (totalBytes != 0) updateProgress(soFarBytes * 100 / totalBytes);
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        Intent installAPKIntent = new Intent(Intent.ACTION_VIEW);
                        String[] command = { "chmod", "777", apkFile.toString() };
                        ProcessBuilder builder = new ProcessBuilder(command);
                        try {
                            builder.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            installAPKIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Uri contentUri = TheAirFileProvider.getUriForFile(UpdateService.this,
                                    getPackageName() + ".com.hackplan.fileprovider", apkFile);
                            installAPKIntent.setDataAndType(contentUri,
                                    "application/vnd.android.package-archive");
                            installAPKIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        } else {
                            installAPKIntent.setDataAndType(Uri.fromFile(apkFile),
                                    "application/vnd.android.package-archive");
                            installAPKIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }

                        startActivity(installAPKIntent);
                        PendingIntent pendingIntent = PendingIntent.getActivity(UpdateService.this, 0, installAPKIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        mBuilder.setContentText(getString(R.string.download_success))
                                .setProgress(0, 0, false)
                                .setContentIntent(pendingIntent)
                                .setOngoing(false);
                        Notification notification = mBuilder.build();
                        notification.flags = Notification.FLAG_AUTO_CANCEL;
                        mNotifyManager.notify(NOTI_ID, notification);
                        stopSelf();
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        mNotifyManager.cancel(NOTI_ID);
                        Toast.makeText(UpdateService.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        stopSelf();
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                })
                .start();

        return START_REDELIVER_INTENT;
    }

    /**
     *
     * @param progress 0 - 100
     */
    private void updateProgress(int progress) {
        mBuilder.setContentText(this.getString(R.string.download_progress, progress))
                .setProgress(100, progress, false);
        PendingIntent pendingintent =
                PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingintent);
        mNotifyManager.notify(NOTI_ID, mBuilder.build());
    }
}
