package com.weibao.robot;

/**
 * TTS�����ӿ�
 */
public interface ITtsCallBack {
	/**
	 * ��ʼ�ʶ�
	 */
	public void onSpeakBegin(String content);
	/**
	 * ��ͣ�ʶ�
	 */
	public void onSpeakPaused();
	/**
	 * �����ʶ�
	 */
	public void onSpeakResumed();
	/**
	 * �ʶ�����
	 */
	public void onTtsPlayCompleted();
	/**
	 * �ϳ�ʧ��
	 */
	public void onSpeakFailed();
	
	}
	


