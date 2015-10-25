package com.weibao.robot.recognize;

import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.example.robot.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

public class XunFeiRecognizer extends VoiceRecognizerBase {

	private static final int recognizeMinTimePeriod = 6000;
	private SpeechRecognizer mIat;
	private int recognizeResultCode = 0;// 函数调用返回值
	private final StringBuilder xunfeiSB;
	private boolean initXunfeiCompelet = false;
	private boolean needRecognizeDirectly = false;
	private static final String recognizeTimeout = "6000";
	private static final String VAD_BOS = "6500";// 判断没有说话的时间点，最大支持10秒
	private static final String VAD_EOS = "500";// 判断没有说话的时间点，最大支持10秒
	private static final String TAG = XunFeiRecognizer.class.getName();
	private long recordStartTimeInMillions = 0;
	private int retryRecordCount = 0;

	/**
	 * 讯飞语音识别管理类 构造方法
	 * 
	 * @param context
	 * @param recognizeWatcher
	 *            监听语音识别结果的listener
	 * @param mHandler
	 */
	public XunFeiRecognizer(Context mContext,
			IVoiceRecognizeListener recognizeWatcher, Handler mHandler) {
		super(mContext, recognizeWatcher, mHandler);
		initXunfeiRecognizer();
		xunfeiSB = new StringBuilder();
	}

	/**
	 * 初始化讯飞组件
	 */
	private void initXunfeiRecognizer() {
		SpeechUtility.createUtility(mContext.getApplicationContext(), "appid="
				+ mContext.getString(R.string.xunfei_appid));

		if (SpeechRecognizer.getRecognizer() != null) {
			mIat = SpeechRecognizer.getRecognizer();
			initXunfeiCompelet = true;
		} else
			mIat = SpeechRecognizer.createRecognizer(mContext, mInitListener);
		setXunfeiParam();
	}

	@Override
	public void startRecord() {

		if (initXunfeiCompelet) {
			needRecognizeDirectly = false;
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					if (!mIat.isListening()) {
						recognizeResultCode = mIat
								.startListening(recognizerListener);
						if (recognizeResultCode != ErrorCode.SUCCESS) {
							Toast.makeText(
									mContext,
									"Dictation failure, error code:："
											+ recognizeResultCode,
									Toast.LENGTH_SHORT).show();
						}
					}
				}
			}, 0);

		} else {
			needRecognizeDirectly = true;
		}

	}

	@Override
	public void stopRecord() {
		if (mIat.isListening()) {
			mIat.stopListening();
		}
		clearWatchers();
	}

	@Override
	public void cancelRecord() {
		if (mIat != null && mIat.isListening()) {
			mIat.cancel();
		}
		setRecording(false);
	}

	private void setXunfeiParam() {
		// 设置语言
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

		mIat.setParameter(SpeechConstant.NET_TIMEOUT, recognizeTimeout);
		// 设置语音前端点
		mIat.setParameter(SpeechConstant.VAD_BOS, VAD_BOS);
		// 设置语音后端点
		mIat.setParameter(SpeechConstant.VAD_EOS, VAD_EOS);
		// 设置标点符号
		mIat.setParameter(SpeechConstant.ASR_PTT, "1");
		// 设置音频保存路径
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
				"/sdcard/iflytek/wavaudio.pcm");
	}

	/**
	 * 删除无用字符
	 * 
	 * @param strRegResult
	 * @return
	 */
	private String delUserlessSuffix(String strRegResult) {
		// 对识别结果做字符串处理
		if (strRegResult.endsWith("。")) // 删除尾部的句号
			strRegResult = strRegResult.substring(0, strRegResult.length() - 1);
		if (strRegResult.startsWith("哦")) // 删除句首的"哦"
			strRegResult = strRegResult.substring(1, strRegResult.length());
		if (strRegResult.startsWith("，") || strRegResult.startsWith("。"))// 删除句首的"，"和"。"
			strRegResult = strRegResult.substring(1, strRegResult.length());

		return strRegResult;
	}

	/**
	 * 初始化监听器。
	 */
	private final InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			if (code == ErrorCode.SUCCESS) {
				initXunfeiCompelet = true;
				if (needRecognizeDirectly) {
					startRecord();
				}
			}
		}
	};
	/**
	 * 讯飞听写监听器。
	 */
	private final RecognizerListener recognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			recordStartTimeInMillions = Calendar.getInstance()
					.getTimeInMillis();
			if (mIVoiceRecognizeWatcher != null) {
				if (!isRecording()) {
					setRecording(true);
					mIVoiceRecognizeWatcher.onBeginRecord();
				}
			}
		}

		@Override
		public void onError(SpeechError error) {
			if (error.getErrorCode() == ErrorCode.MSP_ERROR_NO_DATA) {
				if (recognizeTimeLessThanNomarl())
					return;
				if (mIVoiceRecognizeWatcher != null)
					mIVoiceRecognizeWatcher.onError();
			}
		}

		/**
		 * @param code
		 * @return
		 */

		/**
		 * 识别识别是否小于最小限制时间
		 */
		private boolean recognizeTimeLessThanNomarl() {
			boolean result = false;
			long recordEndTimeInMillions = Calendar.getInstance()
					.getTimeInMillis();

			if (recordEndTimeInMillions - recordStartTimeInMillions < recognizeMinTimePeriod) {
				if (retryRecordCount < 2) {
					retryRecordCount++;
					startRecord();
					result = true;
				} else
					retryRecordCount = 0;
			}
			return result;
		}

		@Override
		public void onEndOfSpeech() {
			if (mIVoiceRecognizeWatcher != null) {
				mIVoiceRecognizeWatcher.onEndOfRecord(null);
			}
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			setRecognizeTime(0);
			retryRecordCount = 0;
			setRecording(false);
			String text = null;
			try {
				text = parseIatResult(results.getResultString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (mContext != null) {

				xunfeiSB.append(text);
				String strRegResult = xunfeiSB.toString().trim();

				strRegResult = delUserlessSuffix(strRegResult);
				if (isLast) {
					if (result_array != null)
						result_array.clear();
					else
						result_array = new ArrayList<String>();
					result_array.add(strRegResult);
					if (mIVoiceRecognizeWatcher != null) {
						mIVoiceRecognizeWatcher.onResults(result_array, 1);
					}
					xunfeiSB.delete(0, xunfeiSB.length());
				}
			}

		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {

		}

		@Override
		public void onVolumeChanged(int volume, byte[] arg1) {
			if (mIVoiceRecognizeWatcher != null) {
				mIVoiceRecognizeWatcher.onVolumeChange(volume);
			}
		}

	};

	/**
	 * 解析识别结果
	 * 
	 * @param json
	 *            识别结果
	 * @return 识别结果
	 * @throws JSONException
	 */
	private static String parseIatResult(String json) throws JSONException {
		StringBuffer ret = new StringBuffer();
		JSONTokener tokener = new JSONTokener(json);
		JSONObject joResult = new JSONObject(tokener);

		JSONArray words = joResult.getJSONArray("ws");
		for (int i = 0; i < words.length(); i++) {
			// 转写结果词，默认使用第一个结果
			JSONArray items = words.getJSONObject(i).getJSONArray("cw");
			JSONObject obj = items.getJSONObject(0);
			ret.append(obj.getString("w"));
		}

		return ret.toString();
	}
}
