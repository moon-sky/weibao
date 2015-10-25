package android_serialport_api;

import java.io.IOException;
import java.io.InputStream;

import android.os.Handler;


/**
 * ���ڽ������Դ��ڵ��߳�
 * 
 * @author wanghexin
 * @creation 2015��6��9��
 */
public class ReceiveSerialPortThread extends Thread {
	InputStream mStream;
	/**
	 * ���յ���Ϣ֮���͸�handler��Ҫ��messageid
	 */
	int messageID;
	Handler handler;
	private String TAG = ReceiveSerialPortThread.class.getSimpleName();

	/**
	 * @param mInputStream
	 *            ���ڽ��յ�������
	 * @param messageID
	 *            ���յ���Ϣ֮���͸�handler��Ҫ��messageid
	 */
	public ReceiveSerialPortThread(InputStream mInputStream, int messageID,
			Handler handler) {
		this.mStream = mInputStream;
		this.messageID = messageID;
		this.handler = handler;
	}

	@Override
	public void run() {
		super.run();
		while (!isInterrupted()) {
			byte[] buffer = new byte[2048];
			if (mStream == null)
				return;

			try {
				if (mStream.available() > 0 == false) {
					continue;
				} else {
					Thread.sleep(200);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				mStream.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			handler.obtainMessage(messageID, buffer).sendToTarget();
		}
	}
}
