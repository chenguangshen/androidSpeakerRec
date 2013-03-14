package edu.ucla.nesl.speech;

import java.util.Arrays;
import java.util.LinkedList;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class RecordService extends Service {
	private final String LOG_TAG = "RecordService";
	private final IBinder mBinder = new LocalBinder();

	public class LocalBinder extends Binder {
		RecordService getService() {
			return RecordService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private AudioRecord aRecorder = null;
	private static String mFileName = null;
	private boolean isRecording;
	private Thread recordingThread = null;
	// private MediaRecorder mRecorder = null;
	// private MediaPlayer mPlayer = null;

	private static final int RECORDER_SAMPLERATE = 8000;
	private static final int SAMPLE_SIZE = 3;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we
									// use only 1024
	int BytesPerElement = 2; // 2 bytes in 16bit format

	public void initFile() {
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/voice8K16bitmono.pcm";
		Log.i(LOG_TAG,
				"buffer size="
						+ AudioRecord.getMinBufferSize(8000,
								AudioFormat.CHANNEL_IN_MONO,
								AudioFormat.ENCODING_PCM_16BIT));
	}

	private void startAudioRecording() {
		aRecorder = new AudioRecord(android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);
		aRecorder.startRecording();
		isRecording = true;
		recordingThread = new Thread(new Runnable() {
			public void run() {
				writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");
		recordingThread.start();
	}

	// private byte[] short2byte(short[] sData) {
	// int shortArrsize = sData.length;
	// byte[] bytes = new byte[shortArrsize * 2];
	// for (int i = 0; i < shortArrsize; i++) {
	// bytes[i * 2] = (byte) (sData[i] & 0x00FF);
	// bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
	// sData[i] = 0;
	// }
	// return bytes;
	//
	// }

	private void writeAudioDataToFile() {
		// Write the output audio in byte
		short[] sData = new short[BufferElements2Rec];
		short[] signal = new short[RECORDER_SAMPLERATE * SAMPLE_SIZE + 2048];
		//LinkedList<Short> sigvec = new LinkedList<Short>(); 
		int count = 0;
		int signal_pt = 0;
		// FileOutputStream os = null;
		// try {
		// os = new FileOutputStream(mFileName);
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }

		while (isRecording) {
			// gets the voice output from microphone to byte format
			aRecorder.read(sData, 0, BufferElements2Rec);
			count = count + BufferElements2Rec;
			//Log.i(LOG_TAG, "count" + count);
			int i = 0;
			while (signal_pt < count) {
				signal[signal_pt++] = sData[i];
				i++;
			}
//			for (i = 0; i < 200; i++) {
//				Log.i(LOG_TAG, "sData[" + i + "]=" + (float)sData[i]);
//			}
//			for (i = 0; i < signal_pt; i++) {
//				Log.i(LOG_TAG, "sigvec[" + i + "]=" + signal[i]);
//			}
//			for (int i = signal_pt; i < count; i++) {
//				Log.i(LOG_TAG, "sData[i - signal_pt]=" + sData[i - signal_pt]);
//				signal[i] = sData[i - signal_pt];
//			}
//			signal_pt = count;
//			count = newcount;
			if (count >= RECORDER_SAMPLERATE * SAMPLE_SIZE) {
//				for (i = 0; i < count; i++) {
//					Log.i(LOG_TAG, "sigvec[" + i + "]=" + signal[i]);
//				}
				int label = getSpeaker(signal, count, 128);
				Log.i("3s!! count = ", "" + count);
				Log.i("speaker is = ", "" + label);
				count = 0;
				signal_pt = 0;
				Arrays.fill(signal, (short) 0);
			}

			// os.write(bData, 0, BufferElements2Rec * BytesPerElement);
		}
	}

	private void stopRecording() {
		// stops the recording activity
		if (null != aRecorder) {
			isRecording = false;
			aRecorder.stop();
			aRecorder.release();
			aRecorder = null;
			recordingThread = null;
		}
	}

	public void onRecord(boolean start) {
		if (start) {
			startAudioRecording();
		} else {
			stopRecording();
		}
	}

	public void release() {
		if (aRecorder != null) {
			aRecorder.release();
			aRecorder = null;
		}
	}

	public native int getSpeaker(short[] signal, int size, int inc);

	static {
		System.loadLibrary("ndkspeaker");
	}

	// public void onPlay(boolean start) {
	// if (start) {
	// startPlaying();
	// } else {
	// stopPlaying();
	// }
	// }
	//
	// public void release() {
	// if (mRecorder != null) {
	// mRecorder.release();
	// mRecorder = null;
	// }
	//
	// if (mPlayer != null) {
	// mPlayer.release();
	// mPlayer = null;
	// }
	// }
	//
	// private void startPlaying() {
	// mPlayer = new MediaPlayer();
	// try {
	// mPlayer.setDataSource(mFileName);
	// mPlayer.prepare();
	// mPlayer.start();
	// } catch (IOException e) {
	// Log.e(LOG_TAG, "prepare() failed");
	// }
	// }
	//
	// private void stopPlaying() {
	// mPlayer.release();
	// mPlayer = null;
	// }
}
