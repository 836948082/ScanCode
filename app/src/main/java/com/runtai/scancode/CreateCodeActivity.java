package com.runtai.scancode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.runtai.scancode.zxing.encode.EncodingHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 生成二维码
 */
public class CreateCodeActivity extends Activity implements ActionSheet.OnSheetItemClickListener{
    @Bind(R.id.et_code_key)
    EditText etCodeKey;
    @Bind(R.id.btn_create_code)
    Button btnCreateCode;
    @Bind(R.id.iv_2_code)
    ImageView iv2Code;
    @Bind(R.id.iv_bar_code)
    ImageView ivBarCode;

    int width;
    int height;

    /** 头像背景边距 */
    public int biankuan = 20;

    /** 圆角半径 */
    int roundPx = 20;

    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_code);
        getScreen();
        ButterKnife.bind(this);
        iv2Code.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //弹出保存图片菜单
                doSomeThing();
                return true;
            }
        });
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    private ActionSheet actionSheet;
    private void doSomeThing() {
        actionSheet = new ActionSheet(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(false)
                .addSheetItem("发送给好友", ActionSheet.SheetItemColor.Blue,
                        this)
                .addSheetItem("保存到手机", ActionSheet.SheetItemColor.Blue,
                        this)
                .addSheetItem("收藏", ActionSheet.SheetItemColor.Blue,
                        this);
        actionSheet.show();
    }

    /**
     * 获取屏幕尺寸
     */
    public void getScreen() {
        WindowManager wm = this.getWindowManager();
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
    }
    @OnClick({R.id.btn_create_code, R.id.btn_create_code_and_img})
    public void clickListener(View view) {
        String key = etCodeKey.getText().toString();
        switch (view.getId()) {
            case R.id.btn_create_code: //生成码
                if (TextUtils.isEmpty(key)) {
                    Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                } else {
                    create2Code(key);
                    createBarCode(key);
                }
                break;
            case R.id.btn_create_code_and_img: //生成码(带头像)
                if (TextUtils.isEmpty(key)) {
                    Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                } else {
                    /** 生成二维码 */
                    Bitmap bitmap = create2Code(key);//1080
                    /** 生成头像 */
                    Bitmap headBitmap = getHeadBitmap(bitmap.getWidth() / 4 - biankuan);//250(整个头像尺寸是二维码尺寸的1/4)
                    /** 根据头像尺寸生成头像背景(边框效果) */
                    Bitmap baise = miaobian(headBitmap);//270
                    /** 加工头像成圆角矩形 */
                    headBitmap = getRoundedCornerBitmaps(headBitmap);
                    /** 加工头像背景成圆角矩形 */
                    baise = getRoundedCornerBitmaps(baise);
                    /** 头像和头像背景合成 */
                    Bitmap hecheng = combineBitmap(baise, headBitmap);
                    if (bitmap != null && headBitmap != null) {
                        this.bitmap = combineBitmap(bitmap, hecheng);//这里合成的图片用于保存到手机
                        createQRCodeBitmapWithPortrait(bitmap, hecheng);
                    }
                }
                break;
        }
    }

    /**
     * 生成圆角矩形
     * @param bitmap
     * @return
     */
    public Bitmap getRoundedCornerBitmaps(Bitmap bitmap){
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        /**
         * rect：RectF对象。
         * rx：x方向上的圆角半径。
         * ry：y方向上的圆角半径。
         * paint：绘制时所使用的画笔。
         */
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 生成条形码图片
     */
    private Bitmap createBarCode(String key) {
        Bitmap qrCode = null;
        try {
            qrCode = EncodingHandler.createBarCode(key, width, 300);
            ivBarCode.setImageBitmap(qrCode);
        } catch (Exception e) {
            Toast.makeText(this, "输入的内容条形码不支持！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        return qrCode;
    }

    /**
     * 生成二维码图片
     */
    private Bitmap create2Code(String key) {
        Bitmap qrCode = null;
        try {
            qrCode = EncodingHandler.create2Code(key, width);
            this.bitmap = qrCode;//这里的图片用于保存到手机
            iv2Code.setImageBitmap(qrCode);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return qrCode;
    }

    /**
     * 初始化头像图片
     */
    private Bitmap getHeadBitmap(int size) {
        try {
            // 这里采用从asset中加载图片abc.jpg
            Bitmap portrait = BitmapFactory.decodeResource(getResources(), R.drawable.dream);
            // 对原有图片压缩显示大小
            Matrix mMatrix = new Matrix();
            float width = portrait.getWidth();
            float height = portrait.getHeight();
            mMatrix.setScale(size / width, size / height);
            return Bitmap.createBitmap(portrait, 0, 0, (int) width, (int) height, mMatrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 在二维码上绘制头像
     */
    private void createQRCodeBitmapWithPortrait(Bitmap qr, Bitmap portrait) {
        // 头像图片的大小
        int portrait_W = portrait.getWidth();
        int portrait_H = portrait.getHeight();
        // 设置头像要显示的位置，即居中显示
        int left = (qr.getWidth() - portrait_W) / 2;
        int top = (qr.getHeight() - portrait_H) / 2;
        int right = left + portrait_W;
        int bottom = top + portrait_H;
        Rect rect1 = new Rect(left, top, right, bottom);
        // 取得qr二维码图片上的画笔，即要在二维码图片上绘制我们的头像
        Canvas canvas = new Canvas(qr);
        // 设置我们要绘制的范围大小，也就是头像的大小范围
        Rect rect2 = new Rect(0, 0, portrait_W, portrait_H);
        // 开始绘制
        canvas.drawBitmap(portrait, rect2, rect1, null);
    }

    /**
     * 图片合成
     * @param background
     * @param foreground
     * @return
     */
    public Bitmap combineBitmap(Bitmap background, Bitmap foreground) {
        if (background == null) {
            return null;
        }
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        int fgWidth = foreground.getWidth();
        int fgHeight = foreground.getHeight();
        Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newmap);
        canvas.drawBitmap(background, 0, 0, null);
        canvas.drawBitmap(foreground, (bgWidth - fgWidth) / 2, (bgHeight - fgHeight) / 2, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return newmap;
    }

    /**
     * (废弃)
     * 把二维码中间的正方形头像处理成四角圆形 (在返回的Bitmap再处理描边(白色))
     * @param bitmap
     * @return
     */
    public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 20; //圆角半径
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);//透明度、红色、绿色、蓝色
        paint.setColor(color);
        /**
         * rect：RectF对象。
         * rx：x方向上的圆角半径。
         * ry：y方向上的圆角半径。
         * paint：绘制时所使用的画笔。
         */
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 增加白色边框
     * @param bitmap
     * @return
     */
    public Bitmap miaobian(Bitmap bitmap){
        Bitmap cbitmap = Bitmap.createBitmap(bitmap.getWidth() + biankuan, bitmap.getHeight() + biankuan, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);// 设置白色
        paint.setAntiAlias(true);// 抗锯齿效果
        Canvas canvas = new Canvas(cbitmap);
        canvas.drawRect(0, 0, cbitmap.getWidth(), cbitmap.getHeight(), paint);// 正方形
        return cbitmap;
    }

    /**
     * 根据指定内容生成自定义宽高的二维码图片
     * <p/>
     * param logoBm
     * logo图标
     * param content
     * 需要生成二维码的内容
     * param width
     * 二维码宽度
     * param height
     * 二维码高度
     * throws WriterException
     * 生成二维码异常
     */
    public static Bitmap makeQRImage(Bitmap logoBmp, String content, int QR_WIDTH, int QR_HEIGHT) throws WriterException {
        try {
            // 图像数据转换，使用了矩阵转换
            Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);// 容错率
            hints.put(EncodeHintType.MARGIN, 2); // default is 4
            hints.put(EncodeHintType.MAX_SIZE, 350);
            hints.put(EncodeHintType.MIN_SIZE, 100);
            BitMatrix bitMatrix = new QRCodeWriter().encode(content,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {
                // 下面这里按照二维码的算法，逐个生成二维码的图片，//两个for循环是图片横列扫描的结果
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y))
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    else
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                }
            }
            // ------------------添加图片部分------------------//
            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                    Bitmap.Config.ARGB_8888);
            // 设置像素点
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            // 获取图片宽高
            int logoWidth = logoBmp.getWidth();
            int logoHeight = logoBmp.getHeight();
            if (QR_WIDTH == 0 || QR_HEIGHT == 0) {
                return null;
            }
            if (logoWidth == 0 || logoHeight == 0) {
                return bitmap;
            }
            // 图片绘制在二维码中央，合成二维码图片
            // logo大小为二维码整体大小的1/2
            float scaleFactor = QR_WIDTH * 1.0f / 2 / logoWidth;
            try {
                Canvas canvas = new Canvas(bitmap);
                canvas.drawBitmap(bitmap, 0, 0, null);
                canvas.scale(scaleFactor, scaleFactor, QR_WIDTH / 2,
                        QR_HEIGHT / 2);
                canvas.drawBitmap(logoBmp, (QR_WIDTH - logoWidth) / 2,
                        (QR_HEIGHT - logoHeight) / 2, null);
                canvas.save(Canvas.ALL_SAVE_FLAG);
                canvas.restore();
                return bitmap;
            } catch (Exception e) {
                bitmap = null;
                e.getStackTrace();
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CreateCode Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.runtai.scancode/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "CreateCode Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.runtai.scancode/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /**
     * 保存方法
     */
    public void saveBitmap(Bitmap bm) {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + "update/", "123.png");
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Bitmap bitmap;
    /***
     * 判断点击哪个条目做相应的事情
     *
     * @param which
     */
    @Override
    public void onClick(int which) {
        switch (which) {
            case 1:
                //先缓存到本地后再分享
                ToolImage.saveImageToGallery(CreateCodeActivity.this, bitmap);
                //这里添加缓存到本地方法
                sendToFriends();
                break;
            case 2:
                ToolImage.saveImageToGallery(CreateCodeActivity.this, bitmap);
                Toast.makeText(CreateCodeActivity.this, "已保存到手机", Toast.LENGTH_LONG).show();
                break;
            case 3:
                Toast.makeText(CreateCodeActivity.this, "已收藏", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void sendToFriends() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        String dirAndName = "/" + ToolImage.dir + "/" + ToolImage.fileName + ToolImage.suffix;
        Uri imageUri = Uri.parse(this.getExternalCacheDir() + dirAndName);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, getTitle()));
    }
}
