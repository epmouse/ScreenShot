package com.branch.www.screencapture.server;

/**
 * Created by stars on 17/12/19.
 */

public interface MyHttpCallBack {
    void onStart(long totalLength);
    void onSuccess();
    void onProgress(long progress);
}
