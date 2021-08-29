package com.audioplayer

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.audioplayer.modal.AudioData
import kotlinx.android.synthetic.main.activity_play_audio.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import java.io.IOException


class PlayAudioActivity : AppCompatActivity() {

    var mediaPlayer: MediaPlayer? = null
    var path = ""
    var mAudioData: AudioData? = null
    var title = ""

    var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_audio)
        initView()
        allViewOnClick()
    }

    override fun onResume() {
        super.onResume()
        tvActionBarTitle.text = title//resources.getString(R.string.recording)
    }

    private fun initView() {
        if (intent != null) {
            if (intent.hasExtra("audioData")) {
                mAudioData = intent.getParcelableExtra("audioData")
            }
        }
        if (mAudioData != null) {
            path = mAudioData?.path ?: ""
            title = mAudioData?.title ?: ""
        }
        if (path.isNotEmpty()) {
            playContentUri(Uri.parse(path))
        }

    }

    private fun allViewOnClick() {
        imgClose.setOnClickListener {
            onBackPressed()
        }
        btnStop.setOnClickListener {
            //stopPlaying()
            onBackPressed()
        }
    }

    fun playContentUri(uri: Uri) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                prepare()
            }
            mediaPlayer?.start()

            lineBarVisualization()

            mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
                override fun onCompletion(mp: MediaPlayer?) {
                    onBackPressed()
                }
            })


        } catch (e: IOException) {
            print(e.message)
            mediaPlayer = null
            mediaPlayer?.release()
        }

        val MILLISECONDS = mediaPlayer?.duration?.toLong() ?: 0

        var time = 1000
        timer = object : CountDownTimer(MILLISECONDS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val min = time / 60000 % 60
                val sec = time / 1000 % 60
                time = time + 1000
                tvTime.text = "" + min + " : " + sec
            }

            override fun onFinish() {
                tvTime.text = "00:00"
            }

        }

        timer?.start()


    }


    private fun stopPlaying() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        if (timer != null) {
            timer?.cancel()
        }
    }

    fun lineBarVisualization() {
        val lineBarVisualizer = visualizerLineBar
        lineBarVisualizer.visibility = View.VISIBLE

        // setting the custom color to the line.
        lineBarVisualizer.setColor(ContextCompat.getColor(this, R.color.blue))

        // define the custom number of bars we want in the visualizer between (10 - 256).
        lineBarVisualizer.setDensity(60f)
        lineBarVisualizer.setPlayer(mediaPlayer?.audioSessionId ?: 0)

        // Setting the media player to the visualizer.

    }

    override fun onBackPressed() {
        super.onBackPressed()
        stopPlaying()
    }


}