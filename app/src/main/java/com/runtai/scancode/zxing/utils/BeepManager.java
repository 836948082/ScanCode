package com.runtai.scancode.zxing.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.runtai.scancode.R;

import java.io.Closeable;
import java.io.IOException;



/**
 */
public class BeepManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, Closeable {

	static final String TAG = BeepManager.class.getSimpleName();

	static final float BEEP_VOLUME = 0.10f;
	static final long VIBRATE_DURATION = 200L;

	final Activity activity;
	MediaPlayer mediaPlayer;
	boolean playBeep;
	boolean vibrate;

	public BeepManager(Activity activity) {
		this.activity = activity;
		this.mediaPlayer = null;
		updatePrefs();
	}

	synchronized void updatePrefs() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		playBeep = shouldBeep(prefs, activity);
		vibrate = true;
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
			// so we now play on the music stream.
			// 设置activity音量控制键控制的音频
			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = buildMediaPlayer(activity);
		}
	}

	/**
	 * 重启响铃和振动
	 */
	public synchronized void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * 判断是否在响铃
	 * @param prefs
	 * @param activity
     * @return
     */
	static boolean shouldBeep(SharedPreferences prefs, Context activity) {
		boolean shouldPlayBeep = true;
		if (shouldPlayBeep) {
			// See if sound settings overrides this
			AudioManager audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
			if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
				shouldPlayBeep = false;
			}
		}
		return shouldPlayBeep;
	}

	/**
	 * 创建MediaPlayer
	 * @param activity
	 * @return
     */
	MediaPlayer buildMediaPlayer(Context activity) {
		MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// 监听是否播放完成
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		// 配置播放资源
		try {
			AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.beiyong);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
			} finally {
				file.close();
			}
			// 设置音量
			mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
			mediaPlayer.prepare();
			return mediaPlayer;
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			mediaPlayer.release();
			return null;
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// When the beep has finished playing, rewind to queue up another one.
		mp.seekTo(0);
	}

	@Override
	public synchronized boolean onError(MediaPlayer mp, int what, int extra) {
		if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
			// we are finished, so put up an appropriate error toast if required
			// and finish
			activity.finish();
		} else {
			// possibly media player error, so release and recreate
			mp.release();
			mediaPlayer = null;
			updatePrefs();
		}
		return true;
	}

	@Override
	public synchronized void close() {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

}
