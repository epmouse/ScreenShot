package com.branch.www.screencapture.baiduOcr;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

/**
 * Created by stars on 18/1/12.
 */

public class BaiduOcrUtils {
    public static String baseUrl = "https://zhidao.baidu.com/search?word=";

    public static void shibie(File filePath) {
        boolean exists = filePath.exists();
        boolean b = exists;
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(filePath);

        OCR instance = OCR.getInstance();
        instance.recognizeAccurateBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                final StringBuilder sb = new StringBuilder();
                for (WordSimple wordSimple : result.getWordList()) {
                    WordSimple word = wordSimple;
                    String words = word.getWords();
                    sb.append(words);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        parseUrl(sb.toString());

                    }
                }).start();
            }

            @Override
            public void onError(OCRError error) {
                System.out.println(error);
            }
        });
    }

    private static void parseUrl(String s) {
        Document doc = null;
        try {
            doc = Jsoup.connect(baseUrl + s).get();
            System.out.println(doc.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
