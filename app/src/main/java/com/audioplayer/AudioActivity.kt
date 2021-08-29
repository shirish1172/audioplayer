package com.audioplayer

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audioplayer.`interface`.OnClickAudio
import com.audioplayer.adapter.AudioListAdapter
import com.audioplayer.helper.URIPathHelper
import com.audioplayer.modal.AudioData
import kotlinx.android.synthetic.main.activity_audio.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import java.io.File


class AudioActivity : AppCompatActivity() {

    var TAG = AudioActivity::class.java.simpleName

    val PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED
    val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    val EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    val PERMISSION_REQUEST_AUDIO_CODE = 100
    val PERMISSION_REQUEST_EXTERNAL_STORAGE_CODE = 101

    val audioDataList: ArrayList<AudioData> = ArrayList()

    var recyclerView: RecyclerView? = null

    var mAudioListAdapter: AudioListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        initView()
        allViewOnClick()
    }

    override fun onResume() {
        super.onResume()
        tvActionBarTitle.text = resources.getString(R.string.audio)
    }

    private fun initView() {

        recyclerView = findViewById(R.id.rvPodcast)
        recyclerView?.layoutManager =
            object : LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {}
        mAudioListAdapter = AudioListAdapter(this, audioDataList, object : OnClickAudio {
            override fun onClickAudio(position: Int) {
                if (!isPermissionAudioGranted()) {
                    requestAudioPermission()
                    return
                }
                startActivity(
                    Intent(this@AudioActivity, PlayAudioActivity::class.java)
                        .putExtra("audioData", audioDataList[position])
                )
            }
        })
        recyclerView?.adapter = mAudioListAdapter

    }

    private fun allViewOnClick() {

        btnRecord.setOnClickListener {
            if (!isPermissionAudioGranted()) {
                requestAudioPermission()
                return@setOnClickListener
            }
            recordingScreen()
        }

        btnImportFile.setOnClickListener {
            if (!isPermissionExternalStorageGranted()) {
                requestExternalStoragePermission()
                return@setOnClickListener
            }
            openAudioFile()
        }

        imgClose.setOnClickListener {
            finish()
        }

    }


    fun openAudioFile() {
        val intent_upload = Intent()
        intent_upload.type = "audio/*"
        intent_upload.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent_upload)
    }

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data != null) {
                    val uri = data.data
                    if (uri != null) {
                        val path = URIPathHelper().getPath(this, uri)
                        if (path != null) {
                            val file = File(path)
                            audioDataList.add(AudioData(file.nameWithoutExtension, path))
                            mAudioListAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }

    var resultRecordLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data != null) {
                    val path = data.getStringExtra("path")
                    if (!path.isNullOrEmpty()) {
                        //val path = URIPathHelper().getPath(this, uri)
                       // if (path != null) {
                            val file = File(path)
                            audioDataList.add(AudioData(file.nameWithoutExtension, path))
                            mAudioListAdapter?.notifyDataSetChanged()
                        //}
                    }
                }
            }
        }

    private fun recordingScreen() {
        resultRecordLauncher.launch(Intent(this, RecordingActivity::class.java))
       // startActivity(Intent(this, RecordingActivity::class.java))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_AUDIO_CODE) {
            if (grantResults[0] == PERMISSION_GRANTED) {
                //recordingScreen()
            }
        } else if (requestCode == PERMISSION_REQUEST_EXTERNAL_STORAGE_CODE) {
            if (grantResults[0] == PERMISSION_GRANTED) {
                openAudioFile()
            }
        }
    }

    private fun isPermissionAudioGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) checkSelfPermission(
            AUDIO_PERMISSION
        ) == PERMISSION_GRANTED
        else return true

    }

    private fun isPermissionExternalStorageGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) checkSelfPermission(
            EXTERNAL_STORAGE_PERMISSION
        ) == PERMISSION_GRANTED
        else return true

    }

    private fun requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(AUDIO_PERMISSION), PERMISSION_REQUEST_AUDIO_CODE)
        }
    }

    private fun requestExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(EXTERNAL_STORAGE_PERMISSION),
                PERMISSION_REQUEST_EXTERNAL_STORAGE_CODE
            )
        }
    }

}