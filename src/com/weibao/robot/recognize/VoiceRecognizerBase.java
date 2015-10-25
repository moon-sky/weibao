package com.weibao.robot.recognize;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

public abstract class VoiceRecognizerBase {

	protected Handler mHandler;
	protected Context mContext;
	/** 没有说话的计数变量 */
	protected int recognizeNoDataCount = 0;
	/**当前是否正在识别*/
	protected boolean isRecording = false;
	/**
	 * 最大识别错误次数
	 */
	protected int maxRecognizeErrorCount = 2;
	/**识别监听者*/
	protected IVoiceRecognizeListener mIVoiceRecognizeWatcher;
	protected ArrayList<String> result_array;
	/**
	 * 当讯飞识别超过 该指定时长的时候
	 */
	protected long time_to_loading=600;

	public long getTime_to_loading() {
		return time_to_loading;
	}

	public void setTime_to_loading(long time_to_loading) {
		this.time_to_loading = time_to_loading;
	}

	// 获取声音识别监听
	public IVoiceRecognizeListener getmIVoiceRecognizeWatcher() {
		return mIVoiceRecognizeWatcher;
	}

	// 设置声音识别监听
	public void setmIVoiceRecognizeWatcher(
			IVoiceRecognizeListener mIVoiceRecognizeWatcher) {
		this.mIVoiceRecognizeWatcher = mIVoiceRecognizeWatcher;
	}

	/**
	 * 清除监听者
	 */
	public void clearWatchers() {
		if (mIVoiceRecognizeWatcher != null)
			mIVoiceRecognizeWatcher = null;
	}

	/**
	 * 构造方法，设置对象
	 * @param context
	 * @param recognizeWatcher
	 * @param mHandler
	 */
	public VoiceRecognizerBase(Context context,
			IVoiceRecognizeListener recognizeWatcher, Handler mHandler) {
		this.mHandler = mHandler;
		this.mIVoiceRecognizeWatcher = recognizeWatcher;
		this.mContext = context;
		// 结果集合
		result_array = new ArrayList<String>();
	}

	/**
	 * 开启识别
	 */
	public abstract void startRecord();

	/**
	 * 停止识别，开始去获取结果
	 */
	abstract void stopRecord();

	/**
	 * 取消录音
	 */
	public abstract void cancelRecord();

	/**
	 * 获取没有说话的计数变量，即识别次数
	 */
	public int getRecognizeTime() {
		return recognizeNoDataCount;
	}

	/**
	 * 设置识别的次数
	 * */
	public void setRecognizeTime(int recognizeTime) {
		this.recognizeNoDataCount = recognizeTime;
	}

	/**
	 * 是否正在录音
	 */
	public boolean isRecording() {
		return isRecording;
	}

	/**
	 * 设置是否正在录音
	 * */
	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	/**
	 * 创建错误信息
	 * @param errorcount 出错的次数
	 * @param recognizerType
	 *            识别引擎的类型  0：讯飞 (中文)1讯飞：英文2：Nuance
	 * @param errorType
	 *            错误类型 0：默认 1：网络不稳定 2：没有听到说话 （全语音场景下前两者要显示气泡）
	 * @param information
	 *            错误信息
	 * @return
	 */
	protected Bundle createErrorInfo(int errorcount, int recognizerType,
			int errorType, String information, boolean isBye) {
		Bundle b = new Bundle();
		b.putInt("recognizerType", recognizerType);
		b.putInt("errorType", errorType);
		b.putString("information", information);
		b.putBoolean("isBye", isBye);
		b.putInt("errorcount", errorcount);
		return b;
	}
}
