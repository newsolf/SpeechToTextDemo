package com.newolf.speechtotextdemo;

import android.content.Context;

import com.huawei.hiai.asr.AsrCloudEngine;

/**
 * 功能描述
 *
 * @author NeWolf
 * @since 2020-11-24
 */
public class AsrRecognizer {
    private static volatile boolean isInit = false;

    private AsrRecognizer() {
    }

    private static AsrCloudEngine sAsrCloudEngine;


    public static AsrCloudEngine getInstance(Context context) {
        if (sAsrCloudEngine == null) {
            synchronized (AsrRecognizer.class) {
                if (sAsrCloudEngine == null) {
                    sAsrCloudEngine = new AsrCloudEngine(context.getApplicationContext());
                }
            }
        }
        return sAsrCloudEngine;
    }


}
