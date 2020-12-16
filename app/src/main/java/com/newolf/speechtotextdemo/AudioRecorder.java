package com.newolf.speechtotextdemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.IntDef;

import com.blankj.utilcode.util.LogUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 功能描述
 *
 * @author NeWolf
 * @since 2020-11-20
 */
public class AudioRecorder {
    //音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;
    //声道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    //编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;

    //录音对象
    private AudioRecord audioRecord;

    //录音状态
    private int status = NO_READY;

    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();


    private static class AudioRecorderHolder {
        private static AudioRecorder instance = new AudioRecorder();
    }

    private AudioRecorder() {
    }

    public static AudioRecorder getInstance() {
        return AudioRecorderHolder.instance;
    }


    /**
     * 创建录音对象
     */
    public void createAudio(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, channelConfig);
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
    }

    /**
     * 创建默认的录音对象
     */
    public void createDefaultAudio() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,
                AUDIO_CHANNEL, AUDIO_ENCODING);
        audioRecord = new AudioRecord(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes);
        status = READY;
    }


    public void startRecord(final RecordStreamListener listener) throws IllegalStateException {

        if (status == NO_READY) {
            throw new IllegalStateException("录音尚未初始化,请检查是否禁止了录音权限~");
        }
        if (status == START) {
            return;
        }
        LogUtils.d("AudioRecorder", "===startRecord===" + audioRecord.getState());




        singleThreadExecutor.execute(() -> {
            audioRecord.startRecording();
            updatePcmData(listener);
        });
    }

    public void stopRecord() {
        if (isStart()) {
            audioRecord.stop();
            status = READY;
        }

    }

    public void release() {
        audioRecord.release();
        status = NO_READY;
    }


    private void updatePcmData(RecordStreamListener listener) {
        byte[] audioData = new byte[bufferSizeInBytes];
        int readSize = 0;

        status = START;
        while (status == START) {
            readSize = audioRecord.read(audioData, 0, bufferSizeInBytes);
            if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                if (listener != null) {
                    listener.recordOfByte(audioData, 0, audioData.length);
                }
            }
        }
    }


    public boolean isReady() {
        return status == READY;
    }

    public boolean isStart() {
        return status == START;
    }

    public boolean isStop() {
        return status == STOP;
    }


    public static final int NO_READY = 0x0001;
    public static final int READY = 0x0002;
    public static final int START = 0x0003;
    public static final int STOP = 0x0004;

    @IntDef({NO_READY, READY, START, STOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
    }

}
