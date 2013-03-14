package edu.ucla.nesl.speech;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import edu.ucla.nesl.speech.RecordService.LocalBinder;


public class MainActivity extends Activity {
	public static final String LOG_TAG = "MainActivity";
	RecordService mService;
	boolean mBound = false;
	final Context context = this;
	
    @Override
    protected void onStart() {
        super.onStart();
        Intent bindIntent = new Intent(this, RecordService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
            //Log.i("MainActivity", mService.toString());
            mService.initFile();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
	
	private RecordButton mRecordButton = null;
//	private PlayButton mPlayButton = null;

	class RecordButton extends Button {
		boolean mStartRecording = true;

		OnClickListener clicker = new OnClickListener() {
			public void onClick(View v) {
				mService.onRecord(mStartRecording);
				if (mStartRecording) {
					setText("Stop recording");
				} else {
					setText("Start recording");
				}
				mStartRecording = !mStartRecording;
			}
		};

		public RecordButton(Context ctx) {
			super(ctx);
			setText("Start recording");
			setOnClickListener(clicker);
		}
	}

//	class PlayButton extends Button {
//		boolean mStartPlaying = true;
//
//		OnClickListener clicker = new OnClickListener() {
//			public void onClick(View v) {
//				mService.onPlay(mStartPlaying);
//				if (mStartPlaying) {
//					setText("Stop playing");
//				} else {
//					setText("Start playing");
//				}
//				mStartPlaying = !mStartPlaying;
//			}
//		};
//
//		public PlayButton(Context ctx) {
//			super(ctx);
//			setText("Start playing");
//			setOnClickListener(clicker);
//		}
//	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		LinearLayout ll = new LinearLayout(this);
		mRecordButton = new RecordButton(this);
		ll.addView(mRecordButton, new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, 0));
//		mPlayButton = new PlayButton(this);
//		ll.addView(mPlayButton, new LinearLayout.LayoutParams(
//				ViewGroup.LayoutParams.WRAP_CONTENT,
//				ViewGroup.LayoutParams.WRAP_CONTENT, 0));
		setContentView(ll);
	}

	@Override
	public void onPause() {
		super.onPause();
		mService.release();
	}
}
