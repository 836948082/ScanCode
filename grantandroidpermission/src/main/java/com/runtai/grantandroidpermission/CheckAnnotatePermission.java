package com.runtai.grantandroidpermission;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.util.Log;

import com.runtai.grantandroidpermission.annotation.PermissionCheck;
import com.runtai.grantandroidpermission.annotation.PermissionDenied;
import com.runtai.grantandroidpermission.annotation.PermissionGranted;
import com.runtai.grantandroidpermission.listeners.PermissionListener;
import com.runtai.grantandroidpermission.utils.ObjectUtils;
import com.runtai.grantandroidpermission.utils.PermissionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CheckAnnotatePermission implements PermissionListener {
    private static final String TAG = "CheckAnnotatePermission";
    public static final int DEFAULT_REQUEST_CODE = 666;
    private Object   object;
    private final Context mContext;

    private int      mRequestCode = DEFAULT_REQUEST_CODE;
    private String[] mPermissions;
    private String   mRationaleConfirmText;
    private String   mRationaleMessage;

    private String   mDenyMessage;
    private String   mDeniedCloseButtonText;

    private boolean  mHasSettingBtn = false;
    private String   mPackageName;

    public CheckAnnotatePermission(Object object, Context context) {
        this.object = object;
        this.mContext = context;
    }

    public static CheckAnnotatePermission from(Object object, Context context) {
        return new CheckAnnotatePermission(object, context);
    }

    /**
     * request permission of requestCode
     * @param requestCode
     * @return
     */
    public CheckAnnotatePermission addRequestCode(int requestCode){
      this.mRequestCode = requestCode;
      return this;
    }


    /**
     * ask for permissions
     * @param permissions
     * @return
     */
    public CheckAnnotatePermission setPermissions(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    /**
     * explain to the user why your app wants the permissions
     * @param rationaleMessage
     * @return
     */

    public CheckAnnotatePermission setRationaleMsg(String rationaleMessage) {
        this.mRationaleMessage = rationaleMessage;
        return this;
    }

    /**
     * explain to the user why your app wants the permissions
     * @param stringRes
     * @return
     */
    public CheckAnnotatePermission setRationaleMsg(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for RationaleMessage");
        }
        this.mRationaleMessage = mContext.getString(stringRes);
        return this;
    }

    /**
     * The text to display in the positive button of rationale message dialog
     * @param rationaleConfirmText
     * @return
     */
    public CheckAnnotatePermission setRationaleConfirmText(String rationaleConfirmText) {

        this.mRationaleConfirmText = rationaleConfirmText;
        return this;
    }
    /**
     * The text to display in the positive button of rationale message dialog
     * @param stringRes
     * @return
     */
    public CheckAnnotatePermission setRationaleConfirmText(@StringRes int stringRes) {

        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for RationaleConfirmText");
        }
        this.mRationaleConfirmText = mContext.getString(stringRes);

        return this;
    }


    /**
     * when user deny permission, show deny message
     * @param denyMessage
     * @return
     */
    public CheckAnnotatePermission setDeniedMsg(String denyMessage) {
        this.mDenyMessage = denyMessage;
        return this;
    }
    /**
     * when user deny permission, show deny message
     * @param stringRes
     * @return
     */
    public CheckAnnotatePermission setDeniedMsg(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for DeniedMessage");
        }
        this.mDenyMessage = mContext.getString(stringRes);
        return this;
    }


    /**
     * The text to display in the close button of deny message dialog
     * @param stringRes
     * @return
     */
    public CheckAnnotatePermission setDeniedCloseButtonText(@StringRes int stringRes) {

        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for DeniedCloseButtonText");
        }
        this.mDeniedCloseButtonText = mContext.getString(stringRes);

        return this;
    }
    /**
     * The text to display in the close button of deny message dialog
     * @param deniedCloseButtonText
     * @return
     */
    public CheckAnnotatePermission setDeniedCloseButtonText(String deniedCloseButtonText) {

        this.mDeniedCloseButtonText = deniedCloseButtonText;
        return this;
    }


    public CheckAnnotatePermission setGotoSettingButton(boolean hasSettingBtn) {

        this.mHasSettingBtn = hasSettingBtn;
        return this;
    }

    /**
     * 检查、请求权限
     */
    public void check() {
        if (ObjectUtils.isEmpty(mPermissions)) {
            mPermissions = PermissionUtils.findPermissionsWithRequestCode(object, object.getClass(), PermissionCheck.class, mRequestCode);
        }

        if (ObjectUtils.isEmpty(mPermissions)) {
            throw new NullPointerException("You must setPermissions()");
        } else {
            if (PermissionUtils.isOverMarshmallow()) {
                Log.d(TAG, "Marshmallow");
                requestPermissions();
            } else {
                Log.d(TAG, "pre Marshmallow");
                permissionGranted();
            }
        }
    }


    private void requestPermissions( ){
        ShadowPermissionActivity.setPermissionListener(this);
        Intent intent = new Intent(mContext, ShadowPermissionActivity.class);
        intent.putExtra(ShadowPermissionActivity.EXTRA_PERMISSIONS, mPermissions);
        intent.putExtra(ShadowPermissionActivity.EXTRA_RATIONALE_MESSAGE, mRationaleMessage);
        intent.putExtra(ShadowPermissionActivity.EXTRA_RATIONALE_CONFIRM_TEXT, mRationaleConfirmText);
        intent.putExtra(ShadowPermissionActivity.EXTRA_SETTING_BUTTON, mHasSettingBtn);
        intent.putExtra(ShadowPermissionActivity.EXTRA_DENY_MESSAGE, mDenyMessage);
        intent.putExtra(ShadowPermissionActivity.EXTRA_DENIED_DIALOG_CLOSE_TEXT, mDeniedCloseButtonText);
        mContext.startActivity(intent);
    }


    @Override
    public void permissionGranted() {
        doExecuteGranted(object, mRequestCode);
    }

    @Override
    public void permissionDenied() {
        doExecuteDenied(object, mRequestCode);
    }


    private static void doExecuteGranted(Object activity, int requestCode) {
        Method executeMethod = PermissionUtils.findMethodWithRequestCode(activity.getClass(),
                PermissionGranted.class, requestCode);

        executeMethod(activity, executeMethod);
    }

    private static void doExecuteDenied(Object activity, int requestCode) {
        Method executeMethod = PermissionUtils.findMethodWithRequestCode(activity.getClass(),
                PermissionDenied.class, requestCode);

        executeMethod(activity, executeMethod);
    }

    private static void executeMethod(Object activity, Method executeMethod) {
        if (executeMethod != null) {
            try {
                if (!executeMethod.isAccessible()) executeMethod.setAccessible(true);
                executeMethod.invoke(activity);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
