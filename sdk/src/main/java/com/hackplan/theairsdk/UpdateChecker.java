package com.hackplan.theairsdk;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Dacer on 5/11/14.
 */
public class UpdateChecker implements ASyncCheckResult {

    private ASyncCheckResult mCheckResultCallback;
    private Context mContext;
    private int primaryColor = Constants.THEME_PRIMARY_COLOR_DEFAULT;
    private boolean isSilenceCheck = false;
    private OnCheckFinishListener checkFinishListener;

    public UpdateChecker(Context context, OnCheckFinishListener listener) {
        this(context);
        this.checkFinishListener = listener;
    }

    public UpdateChecker(Context context) {
        mContext = context;
        mCheckResultCallback = this;
    }

    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
    }

    /**
     * 失败或没有更新或用户跳过此更新时不会有任何提示
     */
    public void startSilence(boolean important) {
        this.isSilenceCheck = true;
        checkUpdate(important);
    }

    public void start(boolean important) {
        this.isSilenceCheck = false;
        checkUpdate(important);
    }

    private void checkUpdate(boolean important) {
        ASyncCheck syncTask = new ASyncCheck(mCheckResultCallback, mContext, important);
        syncTask.execute();
    }

    @Override public void upToDate() {
        if (!isSilenceCheck) {
            Toast.makeText(mContext, R.string.ota_up_to_date, Toast.LENGTH_LONG).show();
        }
        if (checkFinishListener != null) checkFinishListener.checkFinished();
    }

    @Override public void versionDownloadableFound(String link, String msg, int versionCode,
            String versionName, boolean isForce) {

        Intent intent = new Intent(mContext, UpdateCheckerDialogActivity.class);
        intent.putExtra(Constants.THEME_PRIMARY_COLOR, primaryColor);
        intent.putExtra(Constants.APK_DOWNLOAD_URL, link);
        intent.putExtra(Constants.APK_UPDATE_CONTENT, msg);
        intent.putExtra(Constants.APK_VERSION_CODE, versionCode);
        intent.putExtra(Constants.APK_VERSION_NAME, versionName);
        intent.putExtra(Constants.APK_FORCE, isForce);
        intent.putExtra(UpdateCheckerDialogActivity.IS_SILENCE_CHECK, isSilenceCheck);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mContext.startActivity(intent);
        if (checkFinishListener != null) checkFinishListener.checkFinished();
    }

    @Override public void aSyncCheckError(String error) {
        if (!isSilenceCheck) {
            Toast.makeText(mContext, error, Toast.LENGTH_LONG).show();
        }
        if (checkFinishListener != null) checkFinishListener.checkFinished();
    }

    public interface OnCheckFinishListener {
        void checkFinished();
    }
}
