package com.example.bleaudiorecordingtest1java;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.text.MeasuredText;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "BLEAudioRecordingTest";
    private boolean mIsRecording = false;
    private MediaRecorder mRecorder;
    private String mFilepath;
    private boolean mSCOAudioStateConnected = false;
    private Button mBtnStartRecord;
    private Button mBtnStopRecord;

    private BroadcastReceiver mBluetoothScoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

            if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                // Start recording audio
                Log.d(TAG, " AudioManager.SCO_AUDIO_STATE_CONNECTED");
                mSCOAudioStateConnected = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFilepath = getExternalCacheDir().getAbsolutePath();
        mFilepath += "/audorrecordtest.3gp";
        mBtnStartRecord = (Button) findViewById(R.id.btnStartRecord);
        mBtnStopRecord = (Button) findViewById(R.id.btnStopRecord);
        mBtnStartRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });
        mBtnStopRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
            }
        });
        mBtnStopRecord.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        Intent intent = registerReceiver(mBluetoothScoReceiver, intentFilter);
        if (intent == null) {
            Log.e(TAG, "Failed to register bluetooth sco receiver...");
            return;
        }

        int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
        if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
            // Start recording
            Log.d(TAG, "onResume : AudioManager.SCO_AUDIO_STATE_CONNECTED");
            mSCOAudioStateConnected = true;
        }

        // Ensure the SCO audio connection stays active in case the
        // current initiator stops it.
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.startBluetoothSco();
        Log.d(TAG, "onResume : startBluetoothSco");
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(mBluetoothScoReceiver);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.stopBluetoothSco();
        Log.d(TAG, "onPause : stopBluetoothSco");
    }

    private void startRecord() {
        if (mIsRecording) {
            Log.w(TAG, "already recording");
            return;
        }
        try {

            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mRecorder.setOutputFile(mFilepath);
            mRecorder.prepare();
            mRecorder.start();

            Log.d(TAG, "started recording : " + mFilepath);
            Toast.makeText(this, "started recording : " + mFilepath, Toast.LENGTH_LONG);

            mIsRecording = true;

        } catch (Exception e) {
            Log.i("Error Message", "Error Message :" + e.getMessage());
        }
    }

    private void stopRecord() {
        if (mIsRecording) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mIsRecording = false;
            Log.d(TAG, "stopped recording : " + mFilepath);
        }
    }
}