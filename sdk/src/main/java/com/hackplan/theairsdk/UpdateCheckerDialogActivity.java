package com.hackplan.theairsdk;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ImageViewCompat;

import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
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

        // ## create dialogView
        View dialogView = getLayoutInflater().inflate(R.layout.theairskd_dialog_base, null);
        dialog = new AlertDialog.Builder(this).setView(dialogView).create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        TextView titleTv = dialogView.findViewById(R.id.dialog_title_tv);
        TextView contentTv = dialogView.findViewById(R.id.dialog_content_tv);
        Button negativeBtn = dialogView.findViewById(R.id.negative_btn);
        Button positiveBtn = dialogView.findViewById(R.id.positive_btn);

        // ### set primary color
        int primaryColor = getIntent().getIntExtra(Constants.THEME_PRIMARY_COLOR, Constants.THEME_PRIMARY_COLOR_DEFAULT);
        ImageViewCompat.setImageTintList(dialogView.<ImageView>findViewById(R.id.updater_image), ColorStateList.valueOf(primaryColor));
        dialogView.<TextView>findViewById(R.id.dialog_primary_title_tv).setTextColor(primaryColor);
        positiveBtn.setBackground(createButtonBackground(primaryColor));
        int negativeBackgroundColor = getResources().getColor(R.color.negativeBg);
        negativeBtn.setBackground(createButtonBackground(negativeBackgroundColor));

        // ## set content and listener

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

    private Drawable createButtonBackground(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        float radius = dp2px(8);
        shape.setCornerRadii(new float[] { radius, radius, radius, radius, radius, radius, radius, radius });
        shape.setColor(color);
//        float padding = dp2px(1);
//        shape.setPadding(padding, padding, padding, padding);
        return shape;
    }

    private float dp2px(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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
