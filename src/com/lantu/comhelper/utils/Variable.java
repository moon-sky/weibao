package com.lantu.comhelper.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Variable {
    private static Variable instance; 
    
    public static Context gContext = null;

    public static String[] RS232_Params_names = new String[] {"baudRate", "stopBits", "dataBits", "parity", "flowControl", "sendAutoInterval"};
	private int baudRate = 115200;				// baud rate
	private int stopBits = 1;					// 1:1stop bits, 2:2 stop bits
	private int dataBits = 8;					// 8:8bit, 7: 7bit
	private int parity = 1;						// 0: none, 1: odd, 2: even, 3: mark, 4: space
	private int flowControl = 0; 				// 0:none, 1: flow control(CTS,RTS)	
	
	private boolean recHex = false;
	private boolean recPause = false;
	private boolean sendAreaShow = false;	  
	private boolean sendHex = false;
	private boolean sendMemory = false;	
	private boolean sendAuto = false;	
	private int sendAutoInterval = 1000;		// ms
	
	private boolean screenFreedom = false;
	
	private String sendMemories[] = new String[10];
	private String sendMemoryMemos[] = new String[10];
	
	private static SharedPreferences sharedPref = null;
	private static SharedPreferences.Editor sharedPrefEditor = null;	
    
  
    public static Variable getInstance(Context context) { 			//单例模式中获取唯一的Variable实例   
	    if(null == instance) {  
	    	instance = new Variable(context);  
	    	sharedPref = gContext.getSharedPreferences("ComHelper", Activity.MODE_PRIVATE);
	    } 
	    return instance;    	
    } 
    
    public Variable(Context context) {
    	gContext = context;
    }    
    
    public void getCookieVariables() {
    	baudRate = sharedPref.getInt("baudRate", 115200);
    	stopBits = sharedPref.getInt("stopBits", 1);
    	dataBits = sharedPref.getInt("dataBits", 8);
    	parity = sharedPref.getInt("parity", 0);
    	flowControl = sharedPref.getInt("flowControl", 0);
    	
    	recHex = sharedPref.getBoolean("recHex", false);
    	sendHex = sharedPref.getBoolean("sendHex", false);
    	sendAreaShow = sharedPref.getBoolean("sendAreaShow", true);
    	sendMemory = sharedPref.getBoolean("sendMemory", false);
    	sendAutoInterval = sharedPref.getInt("sendAutoInterval", 1000);
    	
    	screenFreedom = sharedPref.getBoolean("screenFreedom", false);
    	
    	for(int i=0; i<10; i++) {
    		sendMemories[i] = sharedPref.getString("sendMemory_" + i, "");
    		sendMemoryMemos[i] = sharedPref.getString("sendMemoryMemo_" + i, "");
    	}
    }
    
	public void setCookieVariables(String name[], Object obj[]) {
		sharedPrefEditor = sharedPref.edit();
		
		if(obj[0] instanceof Integer) {
			for(int i=0; i<name.length; i++) {
				sharedPrefEditor.putInt(name[i], (Integer)obj[i]);
			}
		} else if(obj[0] instanceof String) {
			for(int i=0; i<name.length; i++) {
				sharedPrefEditor.putString(name[i], (String)obj[i]);
			}			
		} else if(obj[0] instanceof Boolean) {
			for(int i=0; i<name.length; i++) {
				sharedPrefEditor.putBoolean(name[i], (Boolean)obj[i]);
			}				
		}
		sharedPrefEditor.commit();
	}  
	
	public void setCookieVariables(String name, Object obj) {
		sharedPrefEditor = sharedPref.edit();
		
		if(obj instanceof Integer) {
			sharedPrefEditor.putInt(name, (Integer)obj);
		} else if(obj instanceof String) {
			sharedPrefEditor.putString(name, (String)obj);
		} else if(obj instanceof Boolean) {
			sharedPrefEditor.putBoolean(name, (Boolean)obj);
		}
		sharedPrefEditor.commit();
	} 	

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public int getStopBits() {
		return stopBits;
	}

	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	public int getDataBits() {
		return dataBits;
	}

	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	public int getParity() {
		return parity;
	}

	public void setParity(int parity) {
		this.parity = parity;
	}

	public int getFlowControl() {
		return flowControl;
	}

	public void setFlowControl(int flowControl) {
		this.flowControl = flowControl;
	}

	public boolean isRecHex() {
		return recHex;
	}

	public void setRecHex(boolean recHex) {
		this.recHex = recHex;
	}

	public boolean isRecPause() {
		return recPause;
	}

	public void setRecPause(boolean recPause) {
		this.recPause = recPause;
	}

	public boolean isSendAreaShow() {
		return sendAreaShow;
	}

	public void setSendAreaShow(boolean sendShow) {
		this.sendAreaShow = sendShow;
	}

	public boolean isSendHex() {
		return sendHex;
	}

	public void setSendHex(boolean sendHex) {
		this.sendHex = sendHex;
	}

	public boolean isSendMemory() {
		return sendMemory;
	}

	public void setSendMemory(boolean sendMemory) {
		this.sendMemory = sendMemory;
	}
	/**自动发送机制*/
	public boolean isSendAuto() {
		return sendAuto;
	}

	public void setSendAuto(boolean sendAuto) {
		this.sendAuto = sendAuto;
	}
	
	public boolean isScreenFreedom() {
		return screenFreedom;
	}

	public void setScreenFreedom(boolean screenFreedom) {
		this.screenFreedom = screenFreedom;
	}
	/**获取自动发送的区间*/
	public int getSendAutoInterval() {
		return sendAutoInterval;
	}

	public void setSendAutoInterval(int sendAutoInterval) {
		this.sendAutoInterval = sendAutoInterval;
	}

	public String[] getSendMemories() {
		return sendMemories;
	}

	public void setSendMemories(String[] sendMemories) {
		this.sendMemories = sendMemories;
	}

	public String[] getSendMemoryMemos() {
		return sendMemoryMemos;
	}

	public void setSendMemoryMemos(String[] sendMemoryMemos) {
		this.sendMemoryMemos = sendMemoryMemos;
	}
   

}
