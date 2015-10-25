package com.weibao.robot.recognize;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

public abstract class VoiceRecognizerBase {

	protected Handler mHandler;
	protected Context mContext;
	/** û��˵���ļ������� */
	protected int recognizeNoDataCount = 0;
	/**��ǰ�Ƿ�����ʶ��*/
	protected boolean isRecording = false;
	/**
	 * ���ʶ��������
	 */
	protected int maxRecognizeErrorCount = 2;
	/**ʶ�������*/
	protected IVoiceRecognizeListener mIVoiceRecognizeWatcher;
	protected ArrayList<String> result_array;
	/**
	 * ��Ѷ��ʶ�𳬹� ��ָ��ʱ����ʱ��
	 */
	protected long time_to_loading=600;

	public long getTime_to_loading() {
		return time_to_loading;
	}

	public void setTime_to_loading(long time_to_loading) {
		this.time_to_loading = time_to_loading;
	}

	// ��ȡ����ʶ�����
	public IVoiceRecognizeListener getmIVoiceRecognizeWatcher() {
		return mIVoiceRecognizeWatcher;
	}

	// ��������ʶ�����
	public void setmIVoiceRecognizeWatcher(
			IVoiceRecognizeListener mIVoiceRecognizeWatcher) {
		this.mIVoiceRecognizeWatcher = mIVoiceRecognizeWatcher;
	}

	/**
	 * ���������
	 */
	public void clearWatchers() {
		if (mIVoiceRecognizeWatcher != null)
			mIVoiceRecognizeWatcher = null;
	}

	/**
	 * ���췽�������ö���
	 * @param context
	 * @param recognizeWatcher
	 * @param mHandler
	 */
	public VoiceRecognizerBase(Context context,
			IVoiceRecognizeListener recognizeWatcher, Handler mHandler) {
		this.mHandler = mHandler;
		this.mIVoiceRecognizeWatcher = recognizeWatcher;
		this.mContext = context;
		// �������
		result_array = new ArrayList<String>();
	}

	/**
	 * ����ʶ��
	 */
	public abstract void startRecord();

	/**
	 * ֹͣʶ�𣬿�ʼȥ��ȡ���
	 */
	abstract void stopRecord();

	/**
	 * ȡ��¼��
	 */
	public abstract void cancelRecord();

	/**
	 * ��ȡû��˵���ļ�����������ʶ�����
	 */
	public int getRecognizeTime() {
		return recognizeNoDataCount;
	}

	/**
	 * ����ʶ��Ĵ���
	 * */
	public void setRecognizeTime(int recognizeTime) {
		this.recognizeNoDataCount = recognizeTime;
	}

	/**
	 * �Ƿ�����¼��
	 */
	public boolean isRecording() {
		return isRecording;
	}

	/**
	 * �����Ƿ�����¼��
	 * */
	public void setRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	/**
	 * ����������Ϣ
	 * @param errorcount ����Ĵ���
	 * @param recognizerType
	 *            ʶ�����������  0��Ѷ�� (����)1Ѷ�ɣ�Ӣ��2��Nuance
	 * @param errorType
	 *            �������� 0��Ĭ�� 1�����粻�ȶ� 2��û������˵�� ��ȫ����������ǰ����Ҫ��ʾ���ݣ�
	 * @param information
	 *            ������Ϣ
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
