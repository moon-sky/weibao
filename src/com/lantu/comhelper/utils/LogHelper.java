package com.lantu.comhelper.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Environment;
  
/** 
 * log��־ͳ�Ʊ��� 
 */  
public class LogHelper {  
    
    private static LogHelper INSTANCE = null;  
    private static String LOGCAT_DIR = null;
    public static String LOGCAT_DIR_SAVE = null;  
    public static String DIR = "ComHelperLog";
    public static String DIRSECOND = DIR + File.separator + "Save"; 
    public static String TAG = "ComHelperLog";
    private int appPid; 
    private LogThread logThread = null;   
    
    
    /**
     * ���캯��
     * @param context
     */
    private LogHelper(Context context) {  
        this.init(context);  
        appPid = android.os.Process.myPid();  
    }     
    
    /**
     * �ӿں�����������־
     */
    public void start() {  
        if (logThread == null) {
        	logThread = new LogThread(String.valueOf(appPid), LOGCAT_DIR);  
        }
        logThread.start();  
    }      

    /**
     * ������־���ļ���   
     */
    public void init(Context context) {  
    	// �������ļ���  + ��������������ݵ��ļ���
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {	// ���ȱ���SD
            LOGCAT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DIR;
            LOGCAT_DIR_SAVE = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DIRSECOND;  
        } else {																		// ��Ӧ�õ�Ŀ¼  
            LOGCAT_DIR = context.getFilesDir().getAbsolutePath() + File.separator + DIR;
            LOGCAT_DIR_SAVE = context.getFilesDir().getAbsolutePath() + File.separator + DIRSECOND;  
        }  
        
        File file = new File(LOGCAT_DIR);  
        if (!file.exists()) {  
            file.mkdirs();  
        }  
        
        File fileSave = new File(LOGCAT_DIR_SAVE);   
        if (!fileSave.exists()) {  
            fileSave.mkdirs();  
        }        
    }  

    /**
     * �õ���־����
     * @param context
     * @return
     */
    public static LogHelper getInstance(Context context) {  
        if (INSTANCE == null) {  
            INSTANCE = new LogHelper(context);  
        }  
        return INSTANCE;  
    }  
  
    /**
     * ��־�洢�߳�
     */
    private class LogThread extends Thread {  
  
        private Process logcatProc;  
        private BufferedReader bufferedReader = null;   
        private boolean runningFlag = true;   
        String cmds = null;  
        private String myPID;   
        private FileOutputStream out = null;  
  
        public LogThread(String pid, String dir) {  
            myPID = pid;  
            try {  
            	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);   
                String curDate = format.format(new Date(System.currentTimeMillis())); 
                out = new FileOutputStream(new File(dir, curDate + ".log"), true);  
            } catch (Exception e) {
				e.printStackTrace();
			}  

  
            /** 
             * ��־�ȼ���*:v , *:d , *:w , *:e , *:f , *:s 
             * ��ʾ��ǰmPID����� E��W�ȼ�����־. 
             * */  
            //cmds = "logcat *:e *:w | grep \"(" + myPID + ")\"";  
            //cmds = "logcat  | grep \"(" + myPID + ")\"";//��ӡ������־��Ϣ  
            //cmds = "logcat -s way";//��ӡ��ǩ������Ϣ  
            //cmds = "logcat *:e *:i | grep \"(" + myPID + ")\""; 
            cmds = "logcat *:e *:w | -s " + TAG + " & grep \"(" + myPID + ")\"";  
        }  
  
        public void stopLogs() {  
            runningFlag = false;  
        }  
  
        @Override  
        public void run() {  
            try {  
                logcatProc = Runtime.getRuntime().exec(cmds);  
                bufferedReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);  
                String line = null;  
                while (runningFlag && (line = bufferedReader.readLine()) != null) {  
                    if (!runningFlag) {  
                        break;  
                    }  
                    if (line.length() == 0) {  
                        continue;  
                    }  
                    if (out != null && line.contains(myPID)) { 
                    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);   
                        String curDate = format.format(new Date(System.currentTimeMillis()));                      	
                        out.write((curDate + "  " + line + "\n").getBytes());  
                    }  
                }  
            } catch (IOException e) {  
                e.printStackTrace();  
            } finally {  
                if (logcatProc != null) {  
                    logcatProc.destroy();  
                    logcatProc = null;  
                }  
                if (bufferedReader != null) {  
                    try {  
                        bufferedReader.close();  
                        bufferedReader = null;  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
                if (out != null) {  
                    try {  
                        out.close();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                    out = null;  
                }  
            }  
        }  
    }  
    
    /**
     * ֹͣ��־��¼
     */
    public void stop() {  
        if (logThread != null) {  
            logThread.stopLogs();  
            logThread = null;  
        }  
    }
  
}

