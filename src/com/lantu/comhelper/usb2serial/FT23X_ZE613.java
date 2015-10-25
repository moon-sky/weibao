package com.lantu.comhelper.usb2serial;

import android.content.Context;
import android.widget.Toast;

import com.example.robot.R;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.lantu.comhelper.utils.Variable;

public class FT23X_ZE613 {
	
	public static final int ReadMAX = 1024;					// ������1024�ֽ�
	public static final int WriteMAX = 1024;				// ������1024�ֽ�
	
	private Context gContext = null;
	
	public static D2xxManager d2xxManager = null; 		// FTDI�豸�Ĺ�����
	private FT_Device ftDevice = null; 					// ��ǰ����FTDI�豸�ľ��				
	private boolean isOk = false;			 			// ��־Uת���豸�Ƿ�����
	
	byte[] writeBuffer = null;  						// ���պͷ��ͻ���
	public boolean READThreadIsRun = false;  
	
	public FT23X_ZE613(Context context) {
		this.gContext = context;
		this.writeBuffer = new byte[WriteMAX];
	}
	
	// Hot plug for plug in solution This is workaround before android 4.2 . Because BroadcastReceiver can not receive ACTION_USB_DEVICE_ATTACHED broadcast
	public void onResume() {
		boolean isAlreadyOk = this.isOk; 
		
		if (isAlreadyOk || (loadingD2xxLibrary() && mountUartDevice() && openUartDevice())) {
			if(setOptions(Variable.getInstance(gContext).getBaudRate(), 
					     Variable.getInstance(gContext).getDataBits(), 
					     Variable.getInstance(gContext).getStopBits(), 
					     Variable.getInstance(gContext).getParity(), 
					     Variable.getInstance(gContext).getFlowControl())) {
				
				if(!isAlreadyOk) {
					Toast.makeText(gContext, gContext.getString(R.string.info_ft23xOk), Toast.LENGTH_SHORT).show();
				}
				
				return;
			}
		} 
		Toast.makeText(gContext, gContext.getString(R.string.info_ft23xNo), Toast.LENGTH_LONG).show();
	}    
	public void onStop() {
		closeUartDevice();
	}	
	public void onDestroy() {
    	this.recycle();
	}	
	
	// ���ڷ����ֽ�����
	public int sendDatas(byte[] datas, int len) {
		if(ftDevice != null) {
			ftDevice.setLatencyTimer((byte)16);		// Ǳ��ʱ��Ĭ��16ms��ԽСԽ��
			return ftDevice.write(datas, ((len > WriteMAX) ? WriteMAX : len));
		} 
		return -1;
	}
	/**
	 * ��������
	 * @date 2015��10��15�� ����2:29:40
	 */
	public int sendDatas(String str, boolean isHex) {
		byte[] sendBytes = null;
		if(isHex) {	// ����Ϊ hex ģʽ
			String[] arrStr = str.trim().split(" ");
			String regex = "[0-9A-Fa-f]{1,2}"; // ��1-2λ���ֻ�����ĸ���
			
			for(String item : arrStr) {// �ж������Ƿ�Ϊ�Ϸ���hex��ʽ
				if(!item.replace(" ", "").matches(regex)) { 
					Toast.makeText(gContext, gContext.getString(R.string.err_hexFormat), Toast.LENGTH_LONG).show(); 
					return -1;
				}
			}
			
			sendBytes = new byte[arrStr.length];
			for(int i=0; i<arrStr.length; i++) {
				// ��16Ϊ��������
				sendBytes[i] = (byte)Integer.parseInt(arrStr[i].replace(" ", ""), 16);
			}
		} else {
			sendBytes = str.getBytes();
		}
		
		return sendDatas(sendBytes, sendBytes.length);
	}		
	
	/** ����jar����*/
	private boolean loadingD2xxLibrary () { 
    	try {
    		d2xxManager = D2xxManager.getInstance(gContext);
        	if(d2xxManager != null && !d2xxManager.setVIDPID(0x0403, 0xada1)) {	// 0x0403����FTDI��˾��0xada1�����Ʒ�ͺ�
        		return false;
        	} else {
        		return true;
        	}
    	} catch (D2xxManager.D2xxException ex) {
    		ex.printStackTrace();
    		return false;
    	}    	
    }		
	/**����Uart����*/
	public boolean mountUartDevice() { 
		if (d2xxManager.createDeviceInfoList(gContext) > 0) {
			return true;
		} else {
			return false;
		}
	}	
	/** �򿪴���*/
	public boolean openUartDevice() { 
		if (null == ftDevice) {
			ftDevice = d2xxManager.openByIndex(gContext, 0);
		} else {
			synchronized (ftDevice) {
				ftDevice = d2xxManager.openByIndex(gContext, 0);
			}
		}
		if (ftDevice != null && true == ftDevice.isOpen()) {
			if (false == READThreadIsRun) {
				READThreadIsRun = true;
				rxtxReceiveThread = new RxtxReceiveThread();
				rxtxReceiveThread.start();
				
//				android.util.Log.i(LogHelper.TAG, "receive thread ready start()");
				
				return true;
			}
		} 
		return false;
	}	
	/** ���ô��ڲ���*/
	public boolean setOptions(int baud, int dataBits, int stopBits, int parity, int flowControl) { 
		ftDevice.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
		ftDevice.setBaudRate(baud);

		switch (dataBits) {
			case 7: 	dataBits = D2xxManager.FT_DATA_BITS_7; 	break;
			case 8: 	dataBits = D2xxManager.FT_DATA_BITS_8; 	break;
			default:	dataBits = D2xxManager.FT_DATA_BITS_8; 	break;
		}
		switch (stopBits) {
			case 1:		stopBits = D2xxManager.FT_STOP_BITS_1;	break;
			case 2:		stopBits = D2xxManager.FT_STOP_BITS_2;	break;
			default:	stopBits = D2xxManager.FT_STOP_BITS_1;	break;
		}
		switch (parity) {
			case 0:		parity = D2xxManager.FT_PARITY_NONE;	break;
			case 1:		parity = D2xxManager.FT_PARITY_ODD;		break;	
			case 2:		parity = D2xxManager.FT_PARITY_EVEN;	break;
			case 3:		parity = D2xxManager.FT_PARITY_MARK; 	break;
			case 4:		parity = D2xxManager.FT_PARITY_SPACE; 	break;
			default:	parity = D2xxManager.FT_PARITY_NONE; 	break;
		}

		ftDevice.setDataCharacteristics((byte)dataBits, (byte)stopBits, (byte)parity);

		short flowCtrlSetting;
		switch (flowControl) {
			case 0:		flowCtrlSetting = D2xxManager.FT_FLOW_NONE;		break;
			case 1:		flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;	break;
			case 2:		flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;	break;
			case 3:		flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;	break;
			default:	flowCtrlSetting = D2xxManager.FT_FLOW_NONE;		break;
		}

		ftDevice.setFlowControl(flowCtrlSetting, (byte) 0x0B, (byte) 0x0D);

		isOk = true;
		
		return true;
	}	
	/** �ͷ���Դ���رմ�*/
	public void closeUartDevice() {									 
		this.isOk = false;
		this.READThreadIsRun = false;
		try {
			Thread.sleep(50);
			if (ftDevice != null) {
				synchronized (ftDevice) {
					if (true == ftDevice.isOpen()) {
						ftDevice.close();
					}
				}
			}			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}	
	
	/**��ԭ������null*/
	private void recycle() {
		this.gContext = null;
		
		d2xxManager = null; 				// FTDI�豸�Ĺ�����
		this.ftDevice = null; 				// ��ǰ����FTDI�豸�ľ��				
		this.isOk = false;			 		// ��־Uת���豸�Ƿ�����
		
		this.writeBuffer = null;  			// ���պͷ��ͻ���
		this.READThreadIsRun = false;  
		this.rxtxReceiveThread = null;	
	}
	
	
	
	/**
	 * 
	 * ���ڵײ�Ĵ������ݽ���
	 * 
	 */
	private final static int MaxRxBufBytes = 1024 * 10;     // 10MB��115200�������ܱ��� 80*50 ms = 4s �����
	private RxtxReceiveThread rxtxReceiveThread;			// ����ԭʼ�������ݵ��߳�  
	/** ����ԭʼ���չܵ���δ��������ݸ���*/
	private int unDealCount = 0;    
	/**���ڽ���Buffer*/
	private byte[] rxtxReceiveBuf = new byte[MaxRxBufBytes];// ���ڽ���Buffer
	
	// ���ڽ����ֽ������߳�
	private class RxtxReceiveThread extends Thread { 
		
		RxtxReceiveThread() {
			this.setPriority(Thread.MIN_PRIORITY);
		}	
		
		@Override
		public void run() {
			int readCount = 0;
			byte[] tmBuf = null;
			
//			android.util.Log.i(LogHelper.TAG, "receive thread ready run()");
			
			while (READThreadIsRun) {
				try {
					Thread.sleep(50);
//					synchronized (ftDevice) {
						while ((readCount = ftDevice.getQueueStatus()) > 0) {
							tmBuf = new byte[readCount]; 
							ftDevice.read(tmBuf, readCount);
							
//							android.util.Log.i(LogHelper.TAG, "receive thread get count " + readCount);
						
							if(!Variable.getInstance(gContext).isRecPause()) {
								dealRxtxReceiveThread(tmBuf);   // ����ӵ�ԭʼ����
							}
							
							tmBuf = null;
							Thread.sleep(100);  // ��������ʱ�����򴮿����׶�����
						}
//					}					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**��������߳�������ݣ����Ƶ�ָ�����飬���޶���С��10M���ڣ�4S�����*/
    private void dealRxtxReceiveThread(byte[] buf) {
        int size = buf.length;                    // �����ֽڸ���

        if ((size + unDealCount) >= MaxRxBufBytes) {
            unDealCount = 0;              		// �ܵ�������̫��δ����ֱ�Ӻ�����Щû�������ݣ���ʱû�и��õİ취��ֻ�ܾ������������Buffer
            if (size > MaxRxBufBytes) {
                size = MaxRxBufBytes;
            }
        }
        //��һ����Ҫ���Ƶ����飬�ڶ����Ǵ�Ҫ���Ƶ�����ĵڼ�����ʼ���������Ǹ��Ƶ��ǣ��ĸ��Ǹ��Ƶ�������ڼ�����ʼ�����һ���Ǹ��Ƴ���
        System.arraycopy(buf, 0, rxtxReceiveBuf, unDealCount, size);
        unDealCount += size;
        
//        android.util.Log.i(LogHelper.TAG, "raw rec bytes: " + com.lantu.comhelper.utils.Comm.byteArray2String(buf, 0, buf.length, true));
    }
    
    /**��ȡ�ܵ��е����ݣ����ѹܵ��е�������ǰ��*/
    public byte[] getRecBytes(int size) {
    	int len = (size > unDealCount)? unDealCount : size;
    	byte[] res = new byte[len];
    	
    	System.arraycopy(rxtxReceiveBuf, 0, res, 0, len);
    	
    	unDealCount -= size;
    	
    	// ��ǰ��
        for (int i = 0, j = len; i < unDealCount; i++) {
            rxtxReceiveBuf[i] = rxtxReceiveBuf[j++];
        }    	
    	
    	return res;
    }
    /** ��ȡ����ԭʼ���չܵ���δ��������ݸ���*/
	public int getUnDealCount() {
		return unDealCount;
	}
	/**�Ƿ��ʼ�����*/
	public boolean isOk() {
		return isOk;
	}

        
}
