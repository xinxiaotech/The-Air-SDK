package com.hackplan.theairsdk;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateCheckerDialogActivity extends Activity {
    final static String IS_SILENCE_CHECK = "is_silence_check";
    private CheckBox cbSkipThisVersion;
    private int versionCode = 0;
    private AlertDialog dialog;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //applyPhoneStyle();
        setContentView(R.layout.theairskd_empty);

        boolean isForce = getIntent().getBooleanExtra(Constants.APK_FORCE, false);
        String versionName = getIntent().getStringExtra(Constants.APK_VERSION_NAME);

        String msg = getIntent().getStringExtra(Constants.APK_UPDATE_CONTENT);
        versionCode = getIntent().getIntExtra(Constants.APK_VERSION_CODE, 0);

        View dialogView = getLayoutInflater().inflate(R.layout.theairskd_dialog_base, null);
        dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        TextView titleTv = dialogView.findViewById(R.id.dialog_title_tv);
        TextView contentTv = dialogView.findViewById(R.id.dialog_content_tv);
        Button positiveBtn = dialogView.findViewById(R.id.positive_btn);

        titleTv.setText(
                String.format("%s %s", getString(R.string.found_new_update), versionName));
        if (TextUtils.isEmpty(msg)) {
            contentTv.setVisibility(View.GONE);
        } else {
            contentTv.setText(msg);
        }
        positiveBtn.setText(R.string.upgrade);

        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                goToDownload();
                finishSelf();
            }
        });

        Button negativeBtn = dialogView.findViewById(R.id.negative_btn);
        negativeBtn.setText(android.R.string.cancel);
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finishSelf();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override public void onDismiss(DialogInterface dialog) {
                finishSelf();
            }
        });
        dialog.show();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }

    private void goToDownload() {
        Toast.makeText(this, R.string.downloading_apk_file, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), UpdateService.class);
        intent.putExtra(Constants.APK_DOWNLOAD_URL,
                getIntent().getStringExtra(Constants.APK_DOWNLOAD_URL));
        startService(intent);
    }

    private void finishSelf() {
        //if (cbSkipThisVersion.isChecked()) {
        //    SettingUtility.setIgnoreVersionCode(versionCode);
        //}
        finish();
    }
}
