package com.newolf.speechtotextdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.LogUtils
import com.huawei.hiai.asr.AsrCloudEngine
import com.huawei.hiai.asr.AsrConstants
import com.huawei.hiai.asr.AsrListener
import com.newolf.volumelib.SpeechDrawable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var audioRecorder:AudioRecorder
    lateinit var asrCloudEngine:AsrCloudEngine

    var canStartAgain = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StoragePermission.getAllPermission(this)

         asrCloudEngine = AsrCloudEngine(this)
        val intent = Intent()
        intent.putExtra(AsrConstants.EXT_DEVICE_ID, "asdasdasdasd")
        intent.putExtra(AsrConstants.EXT_APP_ID, "013254796")


        LogUtils.e("init")


         audioRecorder = AudioRecorder.getInstance()
        audioRecorder.createDefaultAudio()
        val speechDrawable = SpeechDrawable()
        speechDrawable.lineWidth = 4
        speechDrawable.minHeight = 4
        speechDrawable.setStepWidth(8)
        iv.setImageDrawable(speechDrawable)
        speechDrawable.start()










        val intent1 = Intent()
        intent1.putExtra(AsrConstants.EXT_SUR_LANGUAGE, AsrConstants.SUR_LANGUAGE_FR)
        intent1.putExtra(AsrConstants.ASR_VAD_END_WAIT_MS, 4000)

        asrCloudEngine.init(intent, object : AsrListener {
            override fun onInit(p0: Bundle?) {
                LogUtils.e("AsrListener", p0)


            }

            override fun onPartialResults(p0: Bundle?) {
                LogUtils.e("AsrListener", p0)
                val str = p0?.getString(AsrConstants.ASR_RECOGNIZE_RESULT)
                runOnUiThread { tvPartialResults.text = "${tvPartialResults.text}  , $str  " }

            }

            override fun onRecordEnd() {
                LogUtils.e("AsrListener", "onRecordEnd")
                stop()

            }

            override fun onRecordStart() {
                canStartAgain = false
                LogUtils.e("AsrListener", "onRecordStart")
                runOnUiThread {
                    tvPartialResults.text = ""
                    tvResult.text = ""
                }

                audioRecorder
                    .startRecord { audioData, _, end ->
//                        LogUtils.e(audioData, begin, end, Thread.currentThread())

                        asrCloudEngine.writePcm(audioData, end)

                        runOnUiThread {

                            var i = 0;
                            while (i < end) {
                                wv.addData(audioData[i])

                                i += 480
                            }

                            speechDrawable.setVolume(audioData[10].toInt() )

                        }

                    }


            }



            override fun onEnd() {
                LogUtils.e("AsrListener", "onEnd",System.currentTimeMillis() - stopTime)
                canStartAgain = true

            }

            override fun onError(p0: Int) {
                LogUtils.e("AsrListener", p0)
//                audioRecorder.release()
            }

            override fun onResults(result: Bundle?) {
                LogUtils.e("AsrListener", result)
                runOnUiThread {
                    tvResult.text = result?.getString(AsrConstants.ASR_RECOGNIZE_RESULT)
                }

            }

        })






        btnStart.setOnClickListener {
            LogUtils.e("setOnClickListener ","canStartAgain = $canStartAgain","isReady =  ${audioRecorder.isReady}" )

            if (audioRecorder.isReady && canStartAgain) {
                btnStart.text = "Stop"

                LogUtils.e("startListening")
                asrCloudEngine.startListening(intent1)

                val uniqueDeviceId = DeviceUtils.getUniqueDeviceId()
                val androidID = DeviceUtils.getAndroidID()
                LogUtils.e("uniqueDeviceId = $uniqueDeviceId","androidID = $androidID")


            }else{
                LogUtils.e("stop")
                stop()
            }

        }


    }

var stopTime = System.currentTimeMillis()
    fun stop(){
        runOnUiThread { btnStart.text = "Start"

            }

        audioRecorder.stopRecord()
        asrCloudEngine.stopListening()
         stopTime = System.currentTimeMillis()

    }
}