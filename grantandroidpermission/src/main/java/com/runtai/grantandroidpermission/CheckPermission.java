package com.runtai.grantandroidpermission;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.util.Log;

import com.runtai.grantandroidpermission.listeners.PermissionListener;
import com.runtai.grantandroidpermission.utils.ObjectUtils;
import com.runtai.grantandroidpermission.utils.PermissionUtils;

public class CheckPermission {
    private static final String TAG = "CheckPermission";
    private final Context mContext;
    private PermissionListener mPermissionListener;

    private String[] mPermissions;
    private String   mRationaleConfirmText;
    private String   mRationaleMessage;

    private String   mDenyMessage;
    private String   mDeniedCloseButtonText;

    private boolean  mHasSettingBtn = false;

    public CheckPermission(Context context) {
        this.mContext = context;
    }

    public static CheckPermission from(Context context) {
        return new CheckPermission(context);
    }

    /**
     * 授权或拒绝后的回调
     * @param listener
     */
    public CheckPermission setPermissionListener(PermissionListener listener) {
        this.mPermissionListener = listener;
        return this;
    }

    /**
     * 设置权限列表
     * @param permissions
     * @return
     */
    public CheckPermission setPermissions(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    /**
     * 向用户解释你的APP为什么需要这些权限(第二次用户触发时)
     * @param rationaleMessage
     */
    public CheckPermission setRationaleMsg(String rationaleMessage) {
        this.mRationaleMessage = rationaleMessage;
        return this;
    }

    /**
     * 向用户解释你的APP为什么需要这些权限(第二次用户触发时)
     * @param stringRes
     * @return
     */
    public CheckPermission setRationaleMsg(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for RationaleMessage");
        }
        this.mRationaleMessage = mContext.getString(stringRes);
        return this;
    }

    /**
     * 设置(向用户解释你的APP为什么需要这些权限)时的重新请求权限的--按钮文本
     * @param rationaleConfirmText
     * 注意：这里如果想要显示，必须和setRationaleMsg(解释信息)方法一起实现
     */
    public CheckPermission setRationaleConfirmText(String rationaleConfirmText) {
        this.mRationaleConfirmText = rationaleConfirmText;
        return this;
    }

    /**
     * 设置(向用户解释你的APP为什么需要这些权限)时的重新请求权限的--按钮文本
     * @param stringRes
     * 注意：这里如果想要显示，必须和setRationaleMsg(解释信息)方法一起实现
     */
    public CheckPermission setRationaleConfirmText(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for RationaleConfirmText");
        }
        this.mRationaleConfirmText = mContext.getString(stringRes);
        return this;
    }

    /**
     * 当用户拒绝授权时显示拒绝授权信息
     * @param denyMessage
     */
    public CheckPermission setDeniedMsg(String denyMessage) {
        this.mDenyMessage = denyMessage;
        return this;
    }

    /**
     * 当用户拒绝授权时显示拒绝授权信息
     * @param stringRes
     */
    public CheckPermission setDeniedMsg(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for DeniedMessage");
        }
        this.mDenyMessage = mContext.getString(stringRes);
        return this;
    }

    /**
     * 设置(用户点击拒绝授权消息对话框后)显示(Dialog)关闭按钮文本
     * @param stringRes
     */
    public CheckPermission setDeniedCloseButtonText(@StringRes int stringRes) {

        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for DeniedCloseButtonText");
        }
        this.mDeniedCloseButtonText = mContext.getString(stringRes);
        return this;
    }
    /**
     * 设置(用户点击拒绝授权消息对话框后)显示(Dialog)关闭按钮文本
     * @param deniedCloseButtonText
     */
    public CheckPermission setDeniedCloseButtonText(String deniedCloseButtonText) {
        this.mDeniedCloseButtonText = deniedCloseButtonText;
        return this;
    }

    /**
     * 当用户拒绝授权时，显示设置按钮
     * @param hasSettingBtn
     */
    public CheckPermission setGotoSettingButton(boolean hasSettingBtn) {

        this.mHasSettingBtn = hasSettingBtn;
        return this;
    }

    /**
     * 请求检查权限
     */
    public void check() {
        if (mPermissionListener == null) {
            throw new NullPointerException("You must setPermissionListener() on CheckPermission");
        }
        if (ObjectUtils.isEmpty(mPermissions)) {
            throw new NullPointerException("You must setPermissions() on CheckPermission");
        }

        if (PermissionUtils.isOverMarshmallow()) {
            Log.d(TAG, "Marshmallow");
            requestPermissions();
        } else {
            Log.d(TAG, "pre Marshmallow");
            mPermissionListener.permissionGranted();
        }
    }

    /**
     * 提示用户授权
     */
    private void requestPermissions( ){
        ShadowPermissionActivity.setPermissionListener(mPermissionListener);
        Intent intent = new Intent(mContext, ShadowPermissionActivity.class);
        intent.putExtra(ShadowPermissionActivity.EXTRA_PERMISSIONS, mPermissions);
        intent.putExtra(ShadowPermissionActivity.EXTRA_RATIONALE_MESSAGE, mRationaleMessage);
        intent.putExtra(ShadowPermissionActivity.EXTRA_RATIONALE_CONFIRM_TEXT, mRationaleConfirmText);
        intent.putExtra(ShadowPermissionActivity.EXTRA_SETTING_BUTTON, mHasSettingBtn);
        intent.putExtra(ShadowPermissionActivity.EXTRA_DENY_MESSAGE, mDenyMessage);
        intent.putExtra(ShadowPermissionActivity.EXTRA_DENIED_DIALOG_CLOSE_TEXT, mDeniedCloseButtonText);
        mContext.startActivity(intent);
    }

}
