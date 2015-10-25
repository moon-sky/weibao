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
	private int recognizeResultCode = 0;// �������÷���ֵ
	private final StringBuilder xunfeiSB;
	private boolean initXunfeiCompelet = false;
	private boolean needRecognizeDirectly = false;
	private static final String recognizeTimeout = "6000";
	private static final String VAD_BOS = "6500";// �ж�û��˵����ʱ��㣬���֧��10��
	private static final String VAD_EOS = "500";// �ж�û��˵����ʱ��㣬���֧��10��
	private static final String TAG = XunFeiRecognizer.class.getName();
	private long recordStartTimeInMillions = 0;
	private int retryRecordCount = 0;

	/**
	 * Ѷ������ʶ������� ���췽��
	 * 
	 * @param context
	 * @param recognizeWatcher
	 *            ��������ʶ������listener
	 * @param mHandler
	 */
	public XunFeiRecognizer(Context mContext,
			IVoiceRecognizeListener recognizeWatcher, Handler mHandler) {
		super(mContext, recognizeWatcher, mHandler);
		initXunfeiRecognizer();
		xunfeiSB = new StringBuilder();
	}

	/**
	 * ��ʼ��Ѷ�����
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
									"Dictation failure, error code:��"
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
		// ��������
		mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");

		mIat.setParameter(SpeechConstant.NET_TIMEOUT, recognizeTimeout);
		// ��������ǰ�˵�
		mIat.setParameter(SpeechConstant.VAD_BOS, VAD_BOS);
		// ����������˵�
		mIat.setParameter(SpeechConstant.VAD_EOS, VAD_EOS);
		// ���ñ�����
		mIat.setParameter(SpeechConstant.ASR_PTT, "1");
		// ������Ƶ����·��
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
				"/sdcard/iflytek/wavaudio.pcm");
	}

	/**
	 * ɾ�������ַ�
	 * 
	 * @param strRegResult
	 * @return
	 */
	private String delUserlessSuffix(String strRegResult) {
		// ��ʶ�������ַ�������
		if (strRegResult.endsWith("��")) // ɾ��β���ľ��
			strRegResult = strRegResult.substring(0, strRegResult.length() - 1);
		if (strRegResult.startsWith("Ŷ")) // ɾ�����׵�"Ŷ"
			strRegResult = strRegResult.substring(1, strRegResult.length());
		if (strRegResult.startsWith("��") || strRegResult.startsWith("��"))// ɾ�����׵�"��"��"��"
			strRegResult = strRegResult.substring(1, strRegResult.length());

		return strRegResult;
	}

	/**
	 * ��ʼ����������
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
	 * Ѷ����д��������
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
		 * ʶ��ʶ���Ƿ�С����С����ʱ��
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
	 * ����ʶ����
	 * 
	 * @param json
	 *            ʶ����
	 * @return ʶ����
	 * @throws JSONException
	 */
	private static String parseIatResult(String json) throws JSONException {
		StringBuffer ret = new StringBuffer();
		JSONTokener tokener = new JSONTokener(json);
		JSONObject joResult = new JSONObject(tokener);

		JSONArray words = joResult.getJSONArray("ws");
		for (int i = 0; i < words.length(); i++) {
			// תд����ʣ�Ĭ��ʹ�õ�һ�����
			JSONArray items = words.getJSONObject(i).getJSONArray("cw");
			JSONObject obj = items.getJSONObject(0);
			ret.append(obj.getString("w"));
		}

		return ret.toString();
	}
}
