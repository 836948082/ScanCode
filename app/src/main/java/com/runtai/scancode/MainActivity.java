package com.runtai.scancode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.runtai.scancode.utils.Constant;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @Bind(R.id.text_size)
    TextView text_size;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    /**
     * 按钮监听事件，这里我使用Butterknife，不喜欢的也可以直接写监听
     *
     * @param view
     */
    @OnClick({R.id.create_code, R.id.scan_2code, R.id.scan_bar_code, R.id.scan_code, R.id.clean})
    public void clickListener(View view) {
        Intent intent;
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

    private void getSize(){
        String sizes = DataCleanManager.getTotalCacheSize(MainActivity.this);
        text_size.setText(sizes);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSize();
    }
}
