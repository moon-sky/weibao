package com.lantu.comhelper.utils;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.robot.R;
import com.weibao.robot.MainActivity;

public class Exit extends Application {
	private List<Activity> activityList = new LinkedList<Activity>();  
    private static Exit instance;  
    private Context gContext = null;
  
    private Exit(Context context) {
    	gContext = context;
    }  
    
    public static Exit getInstance(Context context) { 			//����ģʽ�л�ȡΨһ��ExitApplicationʵ��   
	    if(null == instance) {  
	    	instance = new Exit(context);  
	    }  
	    return instance;   
    }  
    
    public void addActivity(Activity activity) {//���Activity��������    
    	this.activityList.add(activity);  
    }  
      
    public void exit() {  						//��������Activity��finish
	    for(Activity activity:activityList) {  
	    	
	    	if(activity.getLocalClassName().equals("Main")) { 
	    		MainActivity.isRunBackground = false;	// Main ������Ҫ�ر��߳�
	    	}
	    	
	    	activity.finish();  
	    }  
//	    System.exit(0);  // ���������������������Ӳ�˳����û����鲻��
    }  
    
    public Activity getLastActivity() {
    	if(this.activityList == null || this.activityList.size() == 0) {
			return null; 
    	} else {
    		return this.activityList.get(this.activityList.size()-1);
    	}
    }
    
	/**
	 * �ر�App
	 * @param context
	 */
	public void close(Context context) { 
		
		Dialog exitDialog = new AlertDialog.Builder(context)
		.setIcon(R.drawable._48_exit)
		.setTitle(R.string.isExit)
		.setNegativeButton(R.string.isCancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss(); 
			}
		})		
		.setPositiveButton(R.string.isOk, new DialogInterface.OnClickListener() { 
             
            @Override 
            public void onClick(DialogInterface dialog, int which) { 
//				MyApp.hideIconStatusbar();		// �ر�״̬��ͼ��
				
				dialog.dismiss(); 
				Exit.getInstance(gContext).exit();
            } 
        }) 
		.create();

		Comm.showDiaog(exitDialog);	 	
    }
	public void close() { 
//		MyApp.hideIconStatusbar();		// �ر�״̬��ͼ��
		Exit.getInstance(gContext).exit();
    }	
	
	public boolean isRun(boolean isTip) {
		if(Comm.isDoubleClick(gContext, 1000, isTip)) {
			Exit.getInstance(gContext).close();
		}
        return false;
	}
	
}
