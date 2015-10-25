package com.weibao.robot.recognize;

import java.util.ArrayList;

/**
 * �����������е���Ҫ����Ѷ������ʶ�����Խ�����и����Զ���Ĵ���
 */
public interface IVoiceRecognizeListener {
	/**
	 * ׼���ÿ���˵��
	 * 
	 * @param param
	 */
	void onReadyForSpeach(Object param);

	/**
	 * ¼��������� type:1 Ѷ��ʶ������Ľ�� 2 Nuances
	 * 
	 * @param result
	 */
	void onResults(ArrayList<String> result, int type);

	/**
	 * ¼������
	 * 
	 * @param param
	 */
	void onEndOfRecord(Object param);

	/**
	 * ��ʼ¼��
	 */
	void onBeginRecord();

	/**
	 * ʶ�����
	 * 
	 * @param b
	 *            ������Ϣ
	 */
	void onError();

	/**
	 * �����仯�ص�
	 * 
	 * @param volume
	 */
	void onVolumeChange(int volume);

}
