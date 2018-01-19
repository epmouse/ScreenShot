package com.branch.www.screencapture;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.os.AsyncTaskCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.branch.www.screencapture.server.RetrofitHelper;
import com.google.gson.JsonElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by branch on 2016-5-25.
 * <p>
 * 启动悬浮窗界面
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FloatWindowsService extends Service {


    public static Intent newIntent(Context context, Intent mResultData) {

        Intent intent = new Intent(context, FloatWindowsService.class);

        if (mResultData != null) {
            intent.putExtras(mResultData);
        }
        return intent;
    }

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private static Intent mResultData = null;


    private ImageReader mImageReader;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private GestureDetector mGestureDetector;

    private ImageView mFloatView;

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;


    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView(Gravity.LEFT | Gravity.TOP, 0, 0);

        createImageReader();
    }

    public static Intent getResultData() {
        return mResultData;
    }

    public static void setResultData(Intent mResultData) {
        FloatWindowsService.mResultData = mResultData;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createFloatView(int position, int width, int height) {
        mGestureDetector = new GestureDetector(getApplicationContext(), new FloatGestrueTouchListener());
        mLayoutParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        // 设置Window flag
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = position;
        mLayoutParams.x = mScreenWidth;
        mLayoutParams.y = 100;
        mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;


        mFloatView = new ImageView(getApplicationContext());
        mFloatView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mWindowManager.addView(mFloatView, mLayoutParams);


        mFloatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
    }

    private View createAnswerView(int position, int width, int height) {
        mGestureDetector = new GestureDetector(getApplicationContext(), new FloatGestrueTouchListener());
        mLayoutParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        // 设置Window flag
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.gravity = position;
        mLayoutParams.x = mScreenWidth;
        mLayoutParams.y = 0;
        mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = 500;
        View view = View.inflate(this, R.layout.answer_area_layout, null);
        mWindowManager.addView(view, mLayoutParams);
        return view;
    }


    private class FloatGestrueTouchListener implements GestureDetector.OnGestureListener {
        int lastX, lastY;
        int paramX, paramY;

        @Override
        public boolean onDown(MotionEvent event) {
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
            paramX = mLayoutParams.x;
            paramY = mLayoutParams.y;
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            startScreenShot();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            int dx = (int) e2.getRawX() - lastX;
            int dy = (int) e2.getRawY() - lastY;
            mLayoutParams.x = paramX + dx;
            mLayoutParams.y = paramY + dy;
            // 更新悬浮窗位置
            mWindowManager.updateViewLayout(mFloatView, mLayoutParams);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }


    private void startScreenShot() {

        mFloatView.setVisibility(View.GONE);

        Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            public void run() {
                //start virtual
                startVirtual();
            }
        }, 5);

        handler1.postDelayed(new Runnable() {
            public void run() {
                //capture the screen
                startCapture();

            }
        }, 30);
    }


    private void createImageReader() {

        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);

    }

    public void startVirtual() {
        if (mMediaProjection != null) {
            virtualDisplay();
        } else {
            setUpMediaProjection();
            virtualDisplay();
        }
    }

    public void setUpMediaProjection() {
        if (mResultData == null) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            startActivity(intent);
        } else {
            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
        }
    }

    private MediaProjectionManager getMediaProjectionManager() {

        return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void startCapture() {

        Image image = mImageReader.acquireLatestImage();

        if (image == null) {
            startScreenShot();
        } else {
            SaveTask mSaveTask = new SaveTask();
            AsyncTaskCompat.executeParallel(mSaveTask, image);
        }
    }


    public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {

                return null;
            }

            Image image = params[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();
            File fileImage = null;
            if (bitmap != null) {
                try {
                    fileImage = new File(FileUtil.getScreenShotsName(getApplicationContext()));
                    if (!fileImage.exists()) {
                        fileImage.createNewFile();
                    }

                    FileOutputStream out = new FileOutputStream(fileImage);
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                        Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(fileImage);
                        media.setData(contentUri);
                        sendBroadcast(media);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    fileImage = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    fileImage = null;
                }
            }

            if (fileImage != null) {
                return bitmap;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            //预览图片
            if (bitmap != null) {
                Bitmap bitmap1 = cropBitmap(bitmap);
                ((ScreenCaptureApplication) getApplication()).setmScreenCaptureBitmap(bitmap1);
                Log.e("ryze", "获取图片成功");
                startActivity(PreviewPictureActivity.newIntent(getApplicationContext()));
            }
            getAnswerFromServer("");

//
// 图片文字识别
//            BaiduOcrUtils.shibie(bitmap,new OnResultListener<GeneralResult>() {
//                @Override
//                public void onResult(GeneralResult result) {
//                    final StringBuilder sb = new StringBuilder();
//                    for (WordSimple wordSimple : result.getWordList()) {
//                        WordSimple word = wordSimple;
//                        String words = word.getWords();
//                        sb.append(words);
//                    }
//
//                    getAnswerFromServer(sb.toString());
//                }
//
//                @Override
//                public void onError(OCRError error) {
//                    System.out.println(error);
//                }
//            });
            mFloatView.setVisibility(View.VISIBLE);

        }
    }

    //请求网络获取搜索结果
    private void getAnswerFromServer(String s) {
        RetrofitHelper.getSingleton(this.getApplicationContext())
                .getResultByServer(s)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<JsonElement>() {
                    @Override
                    public void accept(JsonElement jsonElement) throws Exception {
                        final View answerView = createAnswerView(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);//显示答案区域
                        new Handler(getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mWindowManager.removeView(answerView);
                            }
                        },8000);//8秒后答案窗口自动消失
                    }
                });


    }

    private Bitmap cropBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
//        int cropWidth = w >= h ? h : w;// 裁切后所取的正方形区域边长
//        cropWidth /= 2;
//        int cropHeight = (int) (cropWidth / 1.2);
        return Bitmap.createBitmap(bitmap, 0, 300, w, h - 400, null, false);
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void stopVirtual() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    @Override
    public void onDestroy() {
        // to remove mFloatLayout from windowManager
        super.onDestroy();
        if (mFloatView != null) {
            mWindowManager.removeView(mFloatView);
        }
        stopVirtual();

        tearDownMediaProjection();
    }


}
