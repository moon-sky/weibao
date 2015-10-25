package com.weibao.robot;

/**
 * TTS监听接口
 */
public interface ITtsCallBack {
	/**
	 * 开始朗读
	 */
	public void onSpeakBegin(String content);
	/**
	 * 暂停朗读
	 */
	public void onSpeakPaused();
	/**
	 * 重新朗读
	 */
	public void onSpeakResumed();
	/**
	 * 朗读结束
	 */
	public void onTtsPlayCompleted();
	/**
	 * 合成失败
	 */
	public void onSpeakFailed();
	
	}
	


