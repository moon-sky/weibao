package com.weibao.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android_serialport_api.MessageId;

public class MyLocalMediaPlayer implements MediaPlayer.OnPreparedListener,
		MediaPlayer.OnCompletionListener {
	private Context mContext;
	private MediaPlayer mPlayer;
	private int resID = 0;// 正在播放的音乐资源id
	private Handler handler;
	private int msgID = -1;
	private boolean needRecognize = false;

	public MyLocalMediaPlayer(Context context) {
		this.mContext = context;
	}

	public void playBatteryAudioResource(int resId, Handler handler) {
		MediaPlayer mPlayer = MediaPlayer.create(mContext, resId);
		setResID(resId);
		this.handler=handler;
		this.msgID=-1;
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnPreparedListener(this);
		mPlayer.start();
	}

	public void playLocalResource(int resId, Handler handler, int msgID) {
		playLocalResource(resId, handler, msgID, false);
	}

	public void playLocalResource(int resId, Handler handler, int msgID,
			boolean needRecognize) {
		recordResourceLog("MediaPlayerResourceStart");
		stopPlay();
		mPlayer = MediaPlayer.create(mContext, resId);
		setResID(resId);
		this.handler = handler;
		mPlayer.setOnCompletionListener(this);
		mPlayer.setOnPreparedListener(this);
		this.msgID = msgID;
		this.needRecognize = needRecognize;
		mPlayer.start();
	}

	public void stopPlay() {
		stopPlay(true);
	}

	public void stopPlay(boolean needCareLoadingAudio) {
		recordResourceLog("MediaPlayerResourceStop");
		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.setOnCompletionListener(null);
			mPlayer.stop();
		}
	}

	public int getResID() {
		return resID;
	}

	public void setResID(int resID) {
		this.resID = resID;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		recordResourceLog("MediaPlayerResourceCompletion");
		handler.obtainMessage(MessageId.MESSAGE_START_RECOGNIZE).sendToTarget();
	}

	/**
	 * 用于播放 需要监听MediaPlayer 播放完成的音频时间
	 * @param audioID
	 * @param listener
	 */
	public void playLocalResource(int audioID, MediaPlayer.OnCompletionListener listener) {
		recordResourceLog("MediaPlayerResourceStart");
		stopPlay();
		mPlayer = MediaPlayer.create(mContext, audioID);
		setResID(audioID);
		handler=null;
		mPlayer.setOnCompletionListener(listener);
		mPlayer.setOnPreparedListener(this);
		mPlayer.start();
	}
	/**
	 * 记录播放操作步骤及资源名字
	 */
	private void recordResourceLog(String operation) {
		String resourceName=resID>0?mContext.getResources().getResourceName(resID):"null";
	}
}
