package com.rock.zxingphoto;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.rock.zxingphoto.utils.QRCodeDecoder;
import com.rock.zxingphoto.utils.UriUtil;
import com.rock.zxingphoto.view.QRCodeView;
import com.rock.zxingphoto.view.ZXingView;


public class SearchActivity extends AppCompatActivity implements View.OnClickListener, QRCodeView.Delegate {

    private ZXingView zXingView;
    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;
    private ProgressDialog mProgress;
    private boolean lightState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        zXingView = (ZXingView) findViewById(R.id.zxingview);
        findViewById(R.id.canama).setOnClickListener(this);
        findViewById(R.id.shanguang).setOnClickListener(this);
        zXingView.setDelegate(this);
        zXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.5秒后开始识别
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.canama:
                //打开手机中的相册
                Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
                innerIntent.setType("image/*");
                startActivityForResult(innerIntent, REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY);
                break;

            case R.id.shanguang:
                if (lightState == false){
                    zXingView.openFlashlight();
                    lightState = true;
                }else if (lightState == true){
                    zXingView.closeFlashlight();
                    lightState = false;
                }
                break;
        }
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i("aa", "result:" + result);
        setTitle("扫描结果为：" + result);
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
        Intent intent = new Intent();
        intent.putExtra("result",result);
        setResult(2,intent);
        finish();
        zXingView.startSpot(); // 延迟0.5秒后开始识别
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e("aa", "打开相机出错");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        zXingView.startSpotAndShowRect(); // 显示扫描框，并且延迟0.5秒后开始识别

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY) {

            handleAlbumPic(data);
        }
    }

    private void handleAlbumPic(Intent data) {
        //获取选中图片的路径

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("正在扫描...");
        mProgress.setCancelable(false);
        mProgress.show();
        final String photo_path = UriUtil.getRealPathFromUri(this, data.getData());
        if (!TextUtils.isEmpty(photo_path)){

            async(photo_path);
            mProgress.dismiss();
        }

    }

    private void async(final String photo_path) {
         /*
            没有用到 QRCodeView 时可以调用 QRCodeDecoder 的 syncDecodeQRCode 方法

            这里为了偷懒，就没有处理匿名 AsyncTask 内部类导致 Activity 泄漏的问题
            请开发在使用时自行处理匿名内部类导致Activity内存泄漏的问题，处理方式可参考 https://github
            .com/GeniusVJR/LearningNotes/blob/master/Part1/Android/Android%E5%86%85%E5%AD%98%E6%B3%84%E6%BC%8F%E6%80%BB%E7%BB%93.md
             */
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                return QRCodeDecoder.syncDecodeQRCode(photo_path);
            }

            @Override
            protected void onPostExecute(String result) {
                if (TextUtils.isEmpty(result)) {
                    Toast.makeText(SearchActivity.this, "未发现二维码", Toast.LENGTH_SHORT).show();
                } else {
                    zXingView.decodeQRCode(result);
                    Intent intent = new Intent();
                    intent.putExtra("result",result);
                    setResult(2,intent);
                    finish();
                }
            }
        }.execute();
    }

}
