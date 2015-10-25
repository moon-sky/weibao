package com.lantu.comhelper.usb2serial;

import android.content.Context;
import android.widget.Toast;

import com.example.robot.R;
import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.lantu.comhelper.utils.Variable;

public class FT23X_ZE613 {
	
	public static final int ReadMAX = 1024;					// 读缓存1024字节
	public static final int WriteMAX = 1024;				// 读缓存1024字节
	
	private Context gContext = null;
	
	public static D2xxManager d2xxManager = null; 		// FTDI设备的管理类
	private FT_Device ftDevice = null; 					// 当前连接FTDI设备的句柄				
	private boolean isOk = false;			 			// 标志U转串设备是否正常
	
	byte[] writeBuffer = null;  						// 接收和发送缓存
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
	
	// 串口发送字节数据
	public int sendDatas(byte[] datas, int len) {
		if(ftDevice != null) {
			ftDevice.setLatencyTimer((byte)16);		// 潜伏时间默认16ms，越小越好
			return ftDevice.write(datas, ((len > WriteMAX) ? WriteMAX : len));
		} 
		return -1;
	}
	/**
	 * 发送数据
	 * @date 2015年10月15日 下午2:29:40
	 */
	public int sendDatas(String str, boolean isHex) {
		byte[] sendBytes = null;
		if(isHex) {	// 输入为 hex 模式
			String[] arrStr = str.trim().split(" ");
			String regex = "[0-9A-Fa-f]{1,2}"; // 由1-2位数字或这字母组成
			
			for(String item : arrStr) {// 判断输入是否为合法的hex格式
				if(!item.replace(" ", "").matches(regex)) { 
					Toast.makeText(gContext, gContext.getString(R.string.err_hexFormat), Toast.LENGTH_LONG).show(); 
					return -1;
				}
			}
			
			sendBytes = new byte[arrStr.length];
			for(int i=0; i<arrStr.length; i++) {
				// 以16为基数解析
				sendBytes[i] = (byte)Integer.parseInt(arrStr[i].replace(" ", ""), 16);
			}
		} else {
			sendBytes = str.getBytes();
		}
		
		return sendDatas(sendBytes, sendBytes.length);
	}		
	
	/** 加载jar包库*/
	private boolean loadingD2xxLibrary () { 
    	try {
    		d2xxManager = D2xxManager.getInstance(gContext);
        	if(d2xxManager != null && !d2xxManager.setVIDPID(0x0403, 0xada1)) {	// 0x0403代表FTDI公司，0xada1代表产品型号
        		return false;
        	} else {
        		return true;
        	}
    	} catch (D2xxManager.D2xxException ex) {
    		ex.printStackTrace();
    		return false;
    	}    	
    }		
	/**加载Uart外设*/
	public boolean mountUartDevice() { 
		if (d2xxManager.createDeviceInfoList(gContext) > 0) {
			return true;
		} else {
			return false;
		}
	}	
	/** 打开串口*/
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
	/** 配置串口参数*/
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
	/** 释放资源，关闭串*/
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
	
	/**还原参数归null*/
	private void recycle() {
		this.gContext = null;
		
		d2xxManager = null; 				// FTDI设备的管理类
		this.ftDevice = null; 				// 当前连接FTDI设备的句柄				
		this.isOk = false;			 		// 标志U转串设备是否正常
		
		this.writeBuffer = null;  			// 接收和发送缓存
		this.READThreadIsRun = false;  
		this.rxtxReceiveThread = null;	
	}
	
	
	
	/**
	 * 
	 * 用于底层的串口数据接收
	 * 
	 */
	private final static int MaxRxBufBytes = 1024 * 10;     // 10MB在115200条件下能保持 80*50 ms = 4s 不溢出
	private RxtxReceiveThread rxtxReceiveThread;			// 接收原始串口数据的线程  
	/** 串口原始接收管道中未处理的数据个数*/
	private int unDealCount = 0;    
	/**串口接收Buffer*/
	private byte[] rxtxReceiveBuf = new byte[MaxRxBufBytes];// 串口接收Buffer
	
	// 串口接收字节数据线程
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
								dealRxtxReceiveThread(tmBuf);   // 处理接到原始数据
							}
							
							tmBuf = null;
							Thread.sleep(100);  // 必须有延时，否则串口容易丢数据
						}
//					}					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**处理接收线程里的数据，复制到指定数组，并限定大小在10M以内，4S不溢出*/
    private void dealRxtxReceiveThread(byte[] buf) {
        int size = buf.length;                    // 接收字节个数

        if ((size + unDealCount) >= MaxRxBufBytes) {
            unDealCount = 0;              		// 管道中数据太多未处理，直接忽略这些没处理数据，暂时没有更好的办法，只能尽可能扩大接收Buffer
            if (size > MaxRxBufBytes) {
                size = MaxRxBufBytes;
            }
        }
        //第一个是要复制的数组，第二个是从要复制的数组的第几个开始，第三个是复制到那，四个是复制到的数组第几个开始，最后一个是复制长度
        System.arraycopy(buf, 0, rxtxReceiveBuf, unDealCount, size);
        unDealCount += size;
        
//        android.util.Log.i(LogHelper.TAG, "raw rec bytes: " + com.lantu.comhelper.utils.Comm.byteArray2String(buf, 0, buf.length, true));
    }
    
    /**获取管道中的数据，并把管道中的数据向前推*/
    public byte[] getRecBytes(int size) {
    	int len = (size > unDealCount)? unDealCount : size;
    	byte[] res = new byte[len];
    	
    	System.arraycopy(rxtxReceiveBuf, 0, res, 0, len);
    	
    	unDealCount -= size;
    	
    	// 向前推
        for (int i = 0, j = len; i < unDealCount; i++) {
            rxtxReceiveBuf[i] = rxtxReceiveBuf[j++];
        }    	
    	
    	return res;
    }
    /** 获取串口原始接收管道中未处理的数据个数*/
	public int getUnDealCount() {
		return unDealCount;
	}
	/**是否初始化完成*/
	public boolean isOk() {
		return isOk;
	}

        
}
