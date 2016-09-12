package com.runtai.grantandroidpermission;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.runtai.grantandroidpermission.listeners.PermissionListener;
import com.runtai.grantandroidpermission.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class ShadowPermissionActivity extends AppCompatActivity {

    public static final int REQ_CODE_PERMISSION_REQUEST = 110;
    public static final int REQ_CODE_REQUEST_SETTING = 119;
    public static final int REQ_CODE_REQUEST_SYSTEM_ALERT_WINDOW = 120;
    public static final int REQ_CODE_REQUEST_WRITE_SETTING = 121;

    public static final String EXTRA_PERMISSIONS = "permissions";
    public static final String EXTRA_RATIONALE_MESSAGE = "rationale_message";
    public static final String EXTRA_DENY_MESSAGE = "deny_message";
    public static final String EXTRA_PACKAGE_NAME = "package_name";
    public static final String EXTRA_SETTING_BUTTON = "setting_button";
    public static final String EXTRA_SETTING_BUTTON_TEXT = "setting_button_text";
    public static final String EXTRA_RATIONALE_CONFIRM_TEXT = "rationale_confirm_text";
    public static final String EXTRA_DENIED_DIALOG_CLOSE_TEXT = "denied_dialog_close_text";

    String rationale_message;
    String denyMessage;
    String[] permissions;
    boolean hasRequestedSystemAlertWindow = false;
    String permissionSystemAlertWindow;
    boolean hasRequestedWriteSettings = false;
    String permissionWriteSettings;
    String packageName;

    boolean hasSettingButton;
    String settingButtonText;

    String deniedCloseButtonText;
    String rationaleConfirmText;

    public static PermissionListener mPermissionListener;

    public static PermissionListener getPermissionListener() {
        return mPermissionListener;
    }

    public static void setPermissionListener(PermissionListener permissionListener) {
        ShadowPermissionActivity.mPermissionListener = permissionListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        setupFromSavedInstanceState(savedInstanceState);
        checkPermissions(false);
    }

    private void setupFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            permissions = savedInstanceState.getStringArray(EXTRA_PERMISSIONS);
            rationale_message = savedInstanceState.getString(EXTRA_RATIONALE_MESSAGE);
            denyMessage = savedInstanceState.getString(EXTRA_DENY_MESSAGE);
            packageName = savedInstanceState.getString(EXTRA_PACKAGE_NAME);

            hasSettingButton = savedInstanceState.getBoolean(EXTRA_SETTING_BUTTON, true);
            settingButtonText = savedInstanceState.getString(EXTRA_SETTING_BUTTON_TEXT, getString(R.string.permission_setting));

            rationaleConfirmText = savedInstanceState.getString(EXTRA_RATIONALE_CONFIRM_TEXT);
            deniedCloseButtonText = savedInstanceState.getString(EXTRA_DENIED_DIALOG_CLOSE_TEXT);

        } else {

            Bundle bundle = getIntent().getExtras();
            permissions = bundle.getStringArray(EXTRA_PERMISSIONS);
            rationale_message = bundle.getString(EXTRA_RATIONALE_MESSAGE);
            denyMessage = bundle.getString(EXTRA_DENY_MESSAGE);
            packageName = getPackageName();
            hasSettingButton = bundle.getBoolean(EXTRA_SETTING_BUTTON, false);
            settingButtonText = bundle.getString(EXTRA_SETTING_BUTTON_TEXT, getString(R.string.permission_setting));
            rationaleConfirmText = bundle.getString(EXTRA_RATIONALE_CONFIRM_TEXT, getString(R.string.permission_ok));
            deniedCloseButtonText = bundle.getString(EXTRA_DENIED_DIALOG_CLOSE_TEXT, getString(R.string.permission_close));
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(EXTRA_PERMISSIONS, permissions);
        outState.putString(EXTRA_RATIONALE_MESSAGE, rationale_message);
        outState.putString(EXTRA_DENY_MESSAGE, denyMessage);
        outState.putString(EXTRA_PACKAGE_NAME, packageName);
        outState.putBoolean(EXTRA_SETTING_BUTTON, hasSettingButton);
        outState.putString(EXTRA_SETTING_BUTTON, deniedCloseButtonText);
        outState.putString(EXTRA_RATIONALE_CONFIRM_TEXT, rationaleConfirmText);

        super.onSaveInstanceState(outState);
    }

    /**
     * 授权
     */
    private void permissionGranted() {
        if(mPermissionListener != null){
            mPermissionListener.permissionGranted();
            mPermissionListener = null;
        }
        finish();
        overridePendingTransition(0, 0);
    }

    private void permissionDenied(List<String> deniedpermissions) {
        if(mPermissionListener != null){
            mPermissionListener.permissionDenied();
            mPermissionListener = null;
        }
        finish();
        overridePendingTransition(0, 0);
    }

    private void checkPermissions(boolean isAllRequested) {

        List<String> needPermissions = PermissionUtils.findDeniedPermissions(this, permissions);

        boolean showRationale = false;
        for (String permission : needPermissions) {
            if(!hasRequestedSystemAlertWindow && permission.equals(Manifest.permission.SYSTEM_ALERT_WINDOW)) {
                permissionSystemAlertWindow = Manifest.permission.SYSTEM_ALERT_WINDOW;
            } else if(!hasRequestedWriteSettings && permission.equals(Manifest.permission.WRITE_SETTINGS)) {
                permissionWriteSettings = Manifest.permission.WRITE_SETTINGS;
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                showRationale = true;
            }
        }

        if (needPermissions.isEmpty()) {
            permissionGranted();
        } else if (isAllRequested) {
            //From Setting Activity
            permissionDenied(needPermissions);
        } else if (showRationale && !TextUtils.isEmpty(rationale_message)) {
            //Need Show Rationale
            showRationaleDialog(needPermissions);
        } else {
            //Need Request Permissions
            requestPermissions(needPermissions);
        }
    }

    /**
     * 重新向用户解释、申请权限
     * @param needPermissions
     */
    @TargetApi(value = Build.VERSION_CODES.M)
    public void requestPermissions(List<String> needPermissions) {
        //first SYSTEM_ALERT_WINDOW
        if (!hasRequestedSystemAlertWindow && !TextUtils.isEmpty(permissionSystemAlertWindow)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + packageName));
            startActivityForResult(intent, REQ_CODE_REQUEST_SYSTEM_ALERT_WINDOW);
        } else if (!hasRequestedWriteSettings && !TextUtils.isEmpty(permissionWriteSettings)) {
            //second WRITE_SETTINGS
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    Uri.parse("package:" + packageName));
            startActivityForResult(intent, REQ_CODE_REQUEST_WRITE_SETTING);
        }else{
            //other permission

            ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[needPermissions.size()]), REQ_CODE_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> deniedPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.isEmpty()) {
            permissionGranted();
        } else {
            showPermissionDenyDialog(deniedPermissions);
        }
    }

    private void showRationaleDialog(final List<String> needPermissions) {

        new AlertDialog.Builder(this)
                .setMessage(rationale_message)
                .setCancelable(false)
                .setNegativeButton(rationaleConfirmText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(needPermissions);
                    }
                })
                .show();
    }

    /**
     * 拒绝授权
     */
    public void showPermissionDenyDialog(final ArrayList<String> deniedPermissions) {

        if (TextUtils.isEmpty(denyMessage)) {
            // denyMessage
            permissionDenied(deniedPermissions);
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(denyMessage)
                .setCancelable(false)
                .setNegativeButton(deniedCloseButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionDenied(deniedPermissions);
                    }
                });

        if (hasSettingButton) {

            builder.setPositiveButton(settingButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.parse("package:" + packageName));
                        startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                        startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
                    }
                }
            });

        }
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CODE_REQUEST_SETTING: {
                checkPermissions(true);
                break;
            }
            case REQ_CODE_REQUEST_SYSTEM_ALERT_WINDOW: {
                hasRequestedSystemAlertWindow = true;
                checkPermissions(false);
                break;
            }
            case REQ_CODE_REQUEST_WRITE_SETTING: {
                hasRequestedWriteSettings = true;
                checkPermissions(false);
                break;
            }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
