/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONException;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.weibao.robot.ITtsCallBack;

public class SerialPort implements ITtsCallBack {

	private static final String TAG = "SerialPort";
	private Handler handler;
	private Context mContext;
	boolean needRecognize = false;

	/*
	 * Do not remove or rename the field mFd: it is used by native method
	 * close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	/** ��������ʾ�� */
	private String[] gyroscope_hangup_tips;
	private String[] gyroscope_shake_tips;
	private String[] gyroscope_forward_tips;
	private String[] gyroscope_backward_tips;
	private String[] gyroscope_reversal_tips;
	private String[] gyroscope_reversaltonoraml_tips;
	private String[] gyroscope_backwardtonoraml_tips;
	private String[] gyroscope_forwardtonoraml_tips;
	private int curMode = 0;
	private String tip_after_Serialport_mp3;
	private int tip_speed;

	public SerialPort(File device, int baudrate, int flags, Context mContext,
			Handler handler) throws SecurityException, IOException {

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
		this.mContext = mContext;
		this.handler = handler;
	}

	/**
	 * �����ڷ��͹�������Ϣ
	 * 
	 * @param controllInfo
	 *            ���ڷ��������ı���Ϣ
	 * @param ttsCallBack
	 *            tts������
	 * @param isPlaying
	 *            �Ƿ����ڲ��Ź��¡����֡��������
	 * @param curMode
	 *            0����ģʽ 1����ģʽ 2 �ػ�ģʽ 3 ����ģʽ
	 * @throws JSONException
	 */
	public void handleSerialPortSendInfo(byte[] buffer,
			Handler handler) throws JSONException {
		Log.d("serialport", "controlInfo�Ƿ�Ϊ��" + buffer);
		if (buffer != null && buffer.length > 0) {
			int a = buffer[1];
			if (a == 0) {
				handler.obtainMessage(MessageId.MESSAGE_WELCOME_TIP).sendToTarget();
			} else {
				handler.obtainMessage(MessageId.MESSAGE_STOP_RECOGNIZE).sendToTarget();
			}
		}
	}

	/**
	 * ��ȡtext�Ͷ�Ӧ������
	 * 
	 * @author changjingpei
	 * @date 2015��9��28�� ����2:56:24
	 */
	private void getTextAndSpeed() {
		tip_speed = Integer.parseInt(tip_after_Serialport_mp3
				.charAt(tip_after_Serialport_mp3.length() - 1) + "");
		tip_after_Serialport_mp3 = tip_after_Serialport_mp3.substring(0,
				tip_after_Serialport_mp3.length() - 1);
		Log.d("speed", "tip_speed1:" + tip_speed + "tip_after_Serialport_mp31:"
				+ tip_after_Serialport_mp3);
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate,
			int flags);

	public native void close();

	static {
		System.loadLibrary("serial_port");
	}

	@Override
	public void onSpeakBegin(String content) {

	}

	@Override
	public void onSpeakPaused() {

	}

	@Override
	public void onSpeakResumed() {

	}

	@Override
	public void onTtsPlayCompleted() {
		if (needRecognize || (curMode == 0))
			handler.obtainMessage(MessageId.MESSAGE_START_RECOGNIZE)
					.sendToTarget();
	}

	@Override
	public void onSpeakFailed() {

	}
}
