package com.audioplayer

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.audioplayer.utils.VisualizerView
import com.audioplayer.utils.hide
import com.audioplayer.utils.show
import kotlinx.android.synthetic.main.activity_recording.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import java.io.IOException
import java.util.*


class RecordingActivity : AppCompatActivity() {

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var recordingStopped: Boolean = false

    var visualizerView: VisualizerView? = null

    private var handlerVisualizer: Handler? = null
    private var handlerTimer: Handler? = null

    var time = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)
        initView()
        allViewOnClick()

    }

    override fun onResume() {
        super.onResume()
        tvActionBarTitle.text = resources.getString(R.string.recording)
    }

    private fun initView() {
        visualizerView = findViewById(R.id.visualizerLineBar)
        handlerVisualizer = Handler(Looper.getMainLooper())
        mediaRecorder = MediaRecorder()

        val uniqueIdData = UUID.randomUUID().toString().split("-")
        val uniqueId = uniqueIdData.get(0)

        output = "${externalCacheDir?.absolutePath}/recording_$uniqueId.mp3"

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, 0)
        } else {
            startRecording()
        }
    }

    private fun allViewOnClick() {
        btnStop.setOnClickListener {
            pauseRecording()
        }

        imgClose.setOnClickListener {
            onBackPressed()
        }

        btnContinue.setOnClickListener {
            resumeRecording()
        }

        btnSave.setOnClickListener {
            stopRecording()
        }

    }

    private fun startRecording() {
        btnContinue.hide()
        btnSave.hide()
        btnStop.show()
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }


        handlerVisualizer?.post(updateVisualizer)

        handlerTimer = Handler(Looper.getMainLooper())
        handlerTimer?.post(updateTime)

    }

    var updateTime: Runnable = object : Runnable {
        override fun run() {
            val min = time / 60000 % 60
            val sec = time / 1000 % 60
            time = time + 1000
            tvTime.text = "" + min + " : " + sec
            handlerTimer?.postDelayed(this, 1000)
        }
    }

    var updateVisualizer: Runnable = object : Runnable {
        override fun run() {
            if (state) // if we are already recording
            {
                // get the current amplitude
                val x: Int = mediaRecorder?.getMaxAmplitude() ?: 0
                if (x != 0) {
                    visualizerView!!.addAmplitude(x.toFloat()) // update the VisualizeView
                    visualizerView!!.invalidate()
                }
                handlerVisualizer?.postDelayed(this, 50)
            }
        }
    }


    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun pauseRecording() {
        btnContinue.show()
        btnSave.show()
        btnStop.hide()
        if (state) {
            if (!recordingStopped) {
                mediaRecorder?.pause()
                recordingStopped = true
                handlerTimer?.removeCallbacks(updateTime)
                handlerVisualizer?.removeCallbacks(updateVisualizer)
                // button_pause_recording.text = "Resume"
            } else {
                resumeRecording()
            }

        }
    }

    @SuppressLint("RestrictedApi", "SetTextI18n")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resumeRecording() {
        btnContinue.hide()
        btnSave.hide()
        btnStop.show()

        mediaRecorder?.resume()
        recordingStopped = false
        handlerTimer?.post(updateTime)
        handlerVisualizer?.post(updateVisualizer)
    }

    private fun stopRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            handlerVisualizer?.removeCallbacks(updateVisualizer)
            visualizerView?.clear();
            val intent = Intent()
            intent.putExtra("path", output)
            setResult(RESULT_OK, intent)
            finish()
        } else {
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaRecorder?.stop()
        mediaRecorder?.release()
        handlerVisualizer?.removeCallbacks(updateVisualizer)
        handlerTimer?.removeCallbacks(updateTime)
        visualizerView?.clear()
        state = false
    }

}