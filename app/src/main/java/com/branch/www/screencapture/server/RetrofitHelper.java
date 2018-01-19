package com.branch.www.screencapture.server;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by stars on 17/9/26.
 */

public class RetrofitHelper {
    private static volatile RetrofitHelper instance;
    private static final String BASE_URL = "http://192.168.178.127/api/";
    private MyApi api;
    private OkHttpClient okHttpClient;
    private static Context mContext;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static RetrofitHelper getSingleton(Context context) {
        mContext = context;
        if (instance == null) {
            synchronized (RetrofitHelper.class) {
                if (instance == null) {
                    instance = new RetrofitHelper();
                }
            }
        }
        return instance;
    }

    private RetrofitHelper() {
        okHttpClient = new OkHttpClient();
        okHttpClient = getOK();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        api = retrofit.create(MyApi.class);
    }

    private OkHttpClient getOK() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .build();
        return httpClient;
    }


    public Observable<String> getOrderSign(String decOrder) {
        return api.getSign(decOrder);
    }


    public Observable<JsonObject> uploadJson(String json) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        return api.registerAndUploadPubKey(requestBody);
    }

    /**
     * 同步上传文件,携带参数的文件上传, 多个参数一个文件
     *
     * @param mUrl     如果url是baseurl，则传入空字符串  “”
     * @param fileName 文件名
     * @param file     文件实体
     * @param map      参数集合
     * @return
     */
    public String uploadFileMulParamsSingleFile(String mUrl, String fileName, File file, HashMap<String, String> map) {
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        if (map != null) {
            for (String key : map.keySet()) {
                builder.addFormDataPart(key, map.get(key) + "");
            }
        }
        RequestBody requestBody = builder//携带参数
                .addFormDataPart("file", fileName, fileBody)//实体
                .build();
        Request request = new Request.Builder()
                .url(BASE_URL + mUrl)
                .post(requestBody)
                .build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 多参数多文件上传  同步
     *
     * @param mUrl
     * @param map  可参数与文件一起放入，内部会判断
     * @return
     */
    public String uploadFileForMulParamsAndMulFiles(String mUrl, Map<String, Object> map) {
        MediaType FILE_TYPE = MediaType.parse("application/octet-stream");
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for (String key : map.keySet()) {
            if (map.get(key) instanceof File) {
                builder.addPart(MultipartBody.Part.createFormData(key, ((File) map.get(key)).getName(), RequestBody.create(FILE_TYPE, (File) map.get(key))));
            } else {
                builder.addFormDataPart(key, map.get(key).toString());
            }
        }
        RequestBody requestBody = builder//携带参数
                .build();
        Request request = new Request.Builder()
                .url(BASE_URL + mUrl)
                .post(requestBody)
                .build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 请在ui线程调用该方法。不带进度条
     */
    public void downLoadFileProgress(String fileUrl, final String destFileDir, final MyHttpCallBack callBack) {
        String realUrl = fileUrl;
        try {
            realUrl = URLDecoder.decode(fileUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        File dir = new File(destFileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = realUrl.substring(realUrl.lastIndexOf("/"));
        final File file = new File(destFileDir, fileName);
        if (file.exists()) {
            file.delete();
        }
        final Request request = new Request.Builder().url(realUrl).build();
        final Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[1024];
                int len = 0;
                long progress = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    fos = new FileOutputStream(file);
                    callBack.onStart(response.body().contentLength());
                    while ((len = is.read(buf)) != -1) {
                        try {
                            fos.write(buf, 0, len);
                            progress += len;
                            Thread.sleep(10);
                            callBack.onProgress(progress);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    fos.flush();
                    callBack.onSuccess();
                } catch (final IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }


    public Call call;

    public void cacelDownload() {
        if (call != null && !call.isCanceled())
            call.cancel();
    }


}
