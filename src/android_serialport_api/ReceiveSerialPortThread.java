package android_serialport_api;

import java.io.IOException;
import java.io.InputStream;

import android.os.Handler;


/**
 * 用于接收来自串口的线程
 * 
 * @author wanghexin
 * @creation 2015年6月9日
 */
public class ReceiveSerialPortThread extends Thread {
	InputStream mStream;
	/**
	 * 接收到消息之后发送给handler需要的messageid
	 */
	int messageID;
	Handler handler;
	private String TAG = ReceiveSerialPortThread.class.getSimpleName();

	/**
	 * @param mInputStream
	 *            用于接收的数据流
	 * @param messageID
	 *            接收到消息之后发送给handler需要的messageid
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
