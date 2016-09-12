package com.runtai.scancode;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.runtai.grantandroidpermission.CheckAnnotatePermission;
import com.runtai.grantandroidpermission.utils.PermissionUtils;
import com.runtai.scancode.utils.Constant;

public class MainActivity extends Activity implements View.OnClickListener {

    TextView text_size;
    Button create_code, scan_2code, scan_bar_code, scan_code;
    RelativeLayout clean;
    Intent intent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_size = (TextView) findViewById(R.id.text_size);

        create_code = (Button) findViewById(R.id.create_code);
        scan_2code = (Button) findViewById(R.id.scan_2code);
        scan_bar_code = (Button) findViewById(R.id.scan_bar_code);
        scan_code = (Button) findViewById(R.id.scan_code);
        clean = (RelativeLayout) findViewById(R.id.clean);

        create_code.setOnClickListener(this);
        scan_2code.setOnClickListener(this);
        scan_bar_code.setOnClickListener(this);
        scan_code.setOnClickListener(this);
        clean.setOnClickListener(this);
    }

    private void getSize() {
        String sizes = DataCleanManager.getTotalCacheSize(MainActivity.this);
        text_size.setText(sizes);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSize();
    }

    String[] permissionGroup = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    public void onClick(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionUtils.hasSelfPermissions(this, permissionGroup) != true) {
                CheckAnnotatePermission
                        .from(this, this)
                        .setPermissions(permissionGroup)
                        .setRationaleMsg("该功能需要赋予相关访问权限，不开启将无法正常工作！")
                        .check();
                Log.e("没有权限", "不跳转页面");
                return;
            }
        }
        switch (view.getId()) {
            case R.id.create_code: //生成码
                intent = new Intent(this, CreateCodeActivity.class);
                startActivity(intent);
                break;
            case R.id.scan_2code: //扫描二维码
                intent = new Intent(this, CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_QRCODE_MODE);
                startActivity(intent);
                break;
            case R.id.scan_bar_code://扫描条形码
                intent = new Intent(this, CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_BARCODE_MODE);
                startActivity(intent);
                break;
            case R.id.scan_code://扫描条形码或者二维码
                intent = new Intent(this, CommonScanActivity.class);
                intent.putExtra(Constant.REQUEST_SCAN_MODE, Constant.REQUEST_SCAN_MODE_ALL_MODE);
                startActivity(intent);
                break;
            case R.id.clean://清除缓存
                DataCleanManager.clearAllCache(MainActivity.this);
                getSize();
                break;
        }
    }
}
