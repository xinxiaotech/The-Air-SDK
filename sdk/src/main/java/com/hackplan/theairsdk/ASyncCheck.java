package com.hackplan.theairsdk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import org.json.JSONException;
import org.json.JSONObject;

class ASyncCheck extends AsyncTask<String, Integer, ASyncCheck.CheckResult> {

    private static final int VERSION_DOWNLOADABLE_FOUND = 0;
    private static final int NETWORK_ERROR = 1;
    private static final int OTHER_ERROR = 2;
    private static final int UP_TO_DATE = 3;

    private final WeakReference<Context> mContextRef;
    private ASyncCheckResult mResultInterface;
    private boolean mImportant;
    private String downloadLink;
    private String updateMsg;
    private int versionCode;
    private String versionName;
    private boolean isForce = false;

    ASyncCheck(ASyncCheckResult resultInterface, Context context, boolean important) {
        this.mImportant = important;
        this.mResultInterface = resultInterface;
        this.mContextRef = new WeakReference<>(context);
    }

    private static boolean isNetworkAvailable(Context context) {
        boolean connected = false;
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                connected = ni.isConnected();
            }
        }
        return connected;
    }

    @Override protected CheckResult doInBackground(String... ignore) {
        Context c = mContextRef.get();
        if (c == null) return new CheckResult(OTHER_ERROR, "mContextRef.get() is null");
        if (!isNetworkAvailable(c)) {
            return new CheckResult(NETWORK_ERROR, c.getString(R.string.error_msg_no_network));
        }
        String json;

        try {
            json = sendRequest();
        } catch (Exception204 e) {
            return new CheckResult(UP_TO_DATE);
        } catch (IOException e) {
            Log.e("ASyncCheck", "error", e);
            return new CheckResult(OTHER_ERROR, e.getMessage());
        } catch (HttpException e) {
            Log.e("ASyncCheck", "error", e);
            return new CheckResult(OTHER_ERROR, "HTTP ERROR: " + e.getMessage());
        }

        try {
            JSONObject obj = new JSONObject(json);

            this.downloadLink = obj.getJSONObject("file").getString("url");
            this.versionCode = obj.getInt("build");
            this.versionName = obj.getString("version");
            if (obj.has("changelog")) {
                this.updateMsg = obj.getString("changelog");
            }
            if (obj.has("force")) {
                this.isForce = obj.getInt("force") == 1;
            }
            return new CheckResult(VERSION_DOWNLOADABLE_FOUND);
        } catch (JSONException e) {
            return new CheckResult(OTHER_ERROR, "Error: " + json);
        }
    }

    @Override protected void onPostExecute(CheckResult result) {
        switch (result.status) {
            case VERSION_DOWNLOADABLE_FOUND:
                mResultInterface.versionDownloadableFound(downloadLink, updateMsg, versionCode,
                        versionName, isForce);
                break;
            case NETWORK_ERROR:
                mResultInterface.aSyncCheckError(result.errorMsg);
                break;
            case OTHER_ERROR:
                mResultInterface.aSyncCheckError(result.errorMsg);
                break;
            case UP_TO_DATE:
                mResultInterface.upToDate();
                break;
        }
    }

    private String sendRequest() throws IOException, Exception204, HttpException {
        Context c = mContextRef.get();
        if (c == null) throw new RuntimeException("mContextRef.get() is null");
        URL url = new URL(Constants.getUpdateUrl(c, mImportant));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        if (connection.getResponseCode() == 204) {
            throw new Exception204("");
        } else if (connection.getResponseCode() >= 400 && connection.getResponseCode() < 500) {
            throw new HttpException(connection.getResponseCode() + "");
        }

        InputStream is = connection.getInputStream();
        String content_encode = connection.getContentEncoding();
        if (!TextUtils.isEmpty(content_encode) && content_encode.equals("gzip")) {
            is = new GZIPInputStream(is);
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        StringBuilder strBuilder = new StringBuilder();
        String line;
        while ((line = buffer.readLine()) != null) {
            strBuilder.append(line);
        }
        String result = strBuilder.toString();
        try {
            buffer.close();
        } catch (IOException ignored) {
        }
        try {
            is.close();
        } catch (IOException ignored) {
        }
        connection.disconnect();
        return result;
    }

    class Exception204 extends Exception {
        Exception204(String message) {
            super(message);
        }
    }

    class HttpException extends Exception {
        HttpException(String message) {
            super(message);
        }
    }

    class CheckResult {
        int status;
        String errorMsg;

        CheckResult(int status) {
            this.status = status;
        }

        CheckResult(int status, String errorMsg) {
            this.status = status;
            this.errorMsg = errorMsg;
        }
    }
}