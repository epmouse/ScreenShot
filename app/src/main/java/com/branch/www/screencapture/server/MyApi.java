package com.branch.www.screencapture.server;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by stars on 17/9/26.
 */

public interface MyApi {
    public static final String UPLOAD_CRASHFILE_URL = "block/android/crash/upload";


    @POST("sign")
    Observable<String> getSign(@Field("orderParam") String orderParams);
    @GET("block/test/{device}")
    Observable<JsonElement> getServerVersion(@Path("device") String device);
    @GET("")
    Observable<JsonElement> getResultFromServer(@Path("question") String question);
//    @FormUrlEncoded
//    @POST("login/invalid")
//    Observable<LoginStatusBean> login(@Field("username") String username, @Field("password") String password);

    @POST("registerAndUploadPubKey")
    Observable<JsonObject> registerAndUploadPubKey(@Body RequestBody body);

}
