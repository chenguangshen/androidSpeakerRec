package edu.ucla.nesl.speech;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class MediaRecordService extends Service {
	private final String LOG_TAG = "RecordService";
    private final IBinder mBinder = new LocalBinder();
    
    public class LocalBinder extends Binder {
        MediaRecordService getService() {
            return MediaRecordService.this;
        }
    }

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private AudioRecord aRecorder = null;
	private static String mFileName = null;
	private MediaRecorder mRecorder = null;
	private MediaPlayer mPlayer = null;
	
	public void initFile() {
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/audio_test.3gp";
	}
	
	public void startAudioRecording() {
		aRecorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, 
		        8000, 
		        AudioFormat.CHANNEL_IN_MONO, 
		        AudioFormat.ENCODING_PCM_16BIT, 
		        AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,  AudioFormat.ENCODING_PCM_16BIT)
		);
		aRecorder.startRecording();
	}

	public void onRecord(boolean start) {
		if (start) {
			startRecording();
		} else {
			stopRecording();
		}
	}

	public void onPlay(boolean start) {
		if (start) {
			startPlaying();
		} else {
			stopPlaying();
		}
	}
	
	public void release() {
		if (mRecorder != null) {
			mRecorder.release();
			mRecorder = null;
		}

		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	private void startPlaying() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mFileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}

	private void stopPlaying() {
		mPlayer.release();
		mPlayer = null;
	}

	private void startRecording() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setOutputFile(mFileName);
		mRecorder.setAudioSamplingRate(8000);
		mRecorder.setMaxDuration(5000);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

		try {
			mRecorder.prepare();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}

		mRecorder.start();
	}

	private void stopRecording() {
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}
}
