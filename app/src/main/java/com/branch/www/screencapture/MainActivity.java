package com.branch.www.screencapture;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;

public class MainActivity extends FragmentActivity {


    public static final int REQUEST_MEDIA_PROJECTION = 18;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onclick();
        requestCapturePermission();
        initAccessTokenWithAkSk();
    }

    private void onclick() {
        findViewById(R.id.chong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                toBackAndGetSelectPlatform(0);
            }
        });
        findViewById(R.id.zhi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toBackAndGetSelectPlatform(1);
            }
        });
        findViewById(R.id.other).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toBackAndGetSelectPlatform(2);
            }
        });

    }

    //退出到后台并获取用户选择的平台，以确定截图裁剪区域
    private void toBackAndGetSelectPlatform(int i) {
        switch (i) {
            case 0://冲顶大会
                break;
            case 1://芝士超人
                break;
            case 2://其他
                break;
        }
        moveTaskToBack(false);//应用退出到后台

    }


    public void requestCapturePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //5.0 之后才允许使用屏幕截图

            return;
        }

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:

                if (resultCode == RESULT_OK && data != null) {
                    FloatWindowsService.setResultData(data);
                    startService(new Intent(getApplicationContext(), FloatWindowsService.class));
                }
                break;
        }

    }

    private void initAccessTokenWithAkSk() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
            }
        }, getApplicationContext(), "L9MdD56vUddXPWX5LTxfklZ9", "8rvDPBHFSjZzjTQzlpzQxWIH2qCFaCWg");
    }

}
