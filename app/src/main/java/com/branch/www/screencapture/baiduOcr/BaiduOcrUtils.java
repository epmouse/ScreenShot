package com.branch.www.screencapture.baiduOcr;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;

import java.io.File;

/**
 * Created by stars on 18/1/12.
 */

public class BaiduOcrUtils {

    public static void shibie(File filePath,OnResultListener<GeneralResult> listener) {
        boolean exists = filePath.exists();
        boolean b = exists;
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(filePath);
        OCR instance = OCR.getInstance();
        instance.recognizeAccurateBasic(param, listener);
    }
}
