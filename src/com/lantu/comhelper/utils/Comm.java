package com.lantu.comhelper.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.example.robot.R;

public class Comm {
	public static final boolean isDEBUG = true; 
	
	public static int SCREEN_HEIGHT = 0;
	public static int SCREEN_WIDTH = 0;
	
	public static void debug(String info) {
		if(isDEBUG) {
			System.out.println(info);
		}
	}
	public static void Log(String info) {
		android.util.Log.i(LogHelper.TAG, info);
	}	
	
	
	// 双击事件捕捉
	static int doubleClickCount = 0;         // 计算点击的次数 
	static long doubleClickFirstClick = 0;   // 第一次点击的时间 long型   
	static long doubleLastClick = 0;         // 最后一次点击的时间	 
	public static boolean isDoubleClick(Context context, int intervalTime, boolean isTip) { // 两次敲击间隔阈值，单位 ms 
        // 如果第二次点击 距离第一次点击时间过长 那么将第二次点击看为第一次点击  
        if (doubleClickFirstClick != 0 && System.currentTimeMillis() - doubleClickFirstClick > intervalTime) {  
            doubleClickCount = 0;  
        }
        doubleClickCount++;  
        if (doubleClickCount == 1) {  
            doubleClickFirstClick = System.currentTimeMillis();  
            if(isTip) {
            	Toast.makeText(context, context.getResources().getString(R.string.isExit), Toast.LENGTH_SHORT).show(); 
            }
            return false;
        } else if (doubleClickCount == 2) {  
            doubleLastClick = System.currentTimeMillis();  
            if (doubleLastClick - doubleClickFirstClick < intervalTime) { // 两次点击小于500ms 也就是连续点击    
		        doubleClickCount = 0;  
		        doubleClickFirstClick = 0;  
		        doubleLastClick = 0;  
            	return true;
            }  
        }
        doubleClickCount = 0;  
        doubleClickFirstClick = 0;  
        doubleLastClick = 0;          
        
        return false;       
	}	
	
	// 得到屏幕信息
	public static void adaptScreenOption(Activity activity, int row, int col, int top, int bottom, int left, int right) { 
		DisplayMetrics metrics = new DisplayMetrics();						
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);	
		SCREEN_HEIGHT = metrics.heightPixels;
		SCREEN_WIDTH = metrics.widthPixels;
	}
	
	// 得到屏幕信息
	public static int getScreenHeight(Activity activity) { 
		DisplayMetrics metrics = new DisplayMetrics();						
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);	
		return SCREEN_HEIGHT = metrics.heightPixels;
	}
	
	
	
	
	/**
	 * 弹出对话框提示信息
	 * @param tiltle
	 * @param msg
	 */
	public static void showDiaog(android.app.Dialog dialog) { 
		if(dialog.isShowing()) {
			dialog.dismiss();
		} 
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();		
	}
	public static void showDialog(android.app.ProgressDialog dialog) {
		if(dialog.isShowing()) {
			dialog.dismiss();
		} 
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();			
	}
	public static void showDialog(android.app.AlertDialog dialog) {
		if(dialog.isShowing()) {
			dialog.dismiss();
		} 
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();			
	}	
	public static void showDialog(Context context, int icon, String title, String msg) {
		android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context).create();  
		dialog.setIcon(icon);
		dialog.setTitle(title);  
        dialog.setMessage(msg);  
        
        dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, context.getString(R.string.isOk), new DialogInterface.OnClickListener(){ 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				dialog = null;
			}});
        Comm.showDiaog(dialog);		
	}
	public static void showDialog(Context context, int icon, int title, int msg) {
		android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context).create();  
		dialog.setIcon(icon);
		dialog.setTitle(context.getText(title));  
        dialog.setMessage(context.getText(msg));  
		
        dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, context.getString(R.string.isOk), new DialogInterface.OnClickListener(){ 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();	
				dialog = null;
			}});
        Comm.showDiaog(dialog);
	}	
	
	// 运行时得到当前sdk版本
	public static boolean isValidSDKVersion() {
		int SDKVerMin = 12;
        try {
        	if(Integer.valueOf(android.os.Build.VERSION.SDK_INT) > SDKVerMin) {
        		return true;
        	}
        } catch (NumberFormatException e) {
        	e.printStackTrace();
        	return false;
        }
        return false;
    } 
	
	/**
	 * 对网络连接状态进行判断 
	 * @param context
	 * @return
	 */
	public static boolean isNetWork(Context context) {  
		boolean flag = false;
		
	    ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	    if(connManager.getActiveNetworkInfo() != null) {  
	    	flag = connManager.getActiveNetworkInfo().isAvailable();
	    } 
	    return flag; 	    	
	}
   public static void openNetSetting(final Context context) { 
    	AlertDialog dialog = new AlertDialog.Builder(context).create();  
    	dialog.setTitle(context.getString(R.string.info_net));  
        dialog.setMessage(context.getString(R.string.err_noNet));  
		
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.isOk), new DialogInterface.OnClickListener(){ 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = null;  
				try {  
					if(android.os.Build.VERSION.SDK_INT > 10) {  
						intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);  
					}else {  
						intent = new Intent();  
						ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");  
						intent.setComponent(comp);  
						intent.setAction("android.intent.action.VIEW");  
					}
					context.startActivity(intent); 
				} catch (Exception e) {  
					e.printStackTrace();  
				}				
			}});
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getString(R.string.isCancel), new DialogInterface.OnClickListener(){ 
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}});  
        Comm.showDialog(dialog);
	} 	
   
	public static String getTime(String format) { 
		SimpleDateFormat sDateFormat = null;
		String date = null;
		
		if(format.equals("yyyy-MM-dd HH:mm:ss")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA); 
			date = sDateFormat.format(new java.util.Date());
		} else if(format.equals("yyyyMMddHHmmss")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);
			date = sDateFormat.format(new java.util.Date());
		} else if(format.equals("yyMMddHHmmss")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date()).substring(2);
		} else if(format.equals("yyyyMMddHHmmssSSS")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date());
		} else if(format.equals("yyyy-MM-dd HH:mm")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date());
		} else if(format.equals("yyyy-MM-dd")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date());	
		} else if(format.equals("ago_7_SS")) {			// 得到当前时间前7天的时间串
			Date dDate = new Date((new Date()).getTime() - 7*24*60*60*1000);
			sDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
			date = sDateFormat.format(dDate);			
		} else if(format.equals("ago_7")) {			// 得到当前时间前7天的时间串
			Date dDate = new Date((new Date()).getTime() - 7*24*60*60*1000);
			sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
			date = sDateFormat.format(dDate);
		} else if(format.equals("ago_7_S")) {		// 得到当前时间前7天的时间串
			Date dDate = new Date((new Date()).getTime() - 7*24*60*60*1000);
			sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
			date = sDateFormat.format(dDate);
		}
		
		return date;
	}
	public static String getTime(String format, long milSeconds, String type) {
		SimpleDateFormat sDateFormat = null;
		String date = null;
		
		if(format.equals("yyyy-MM-dd HH:mm:ss")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA); 
			date = sDateFormat.format(new java.util.Date(milSeconds));
		} else if(format.equals("yyyyMMddHHmmss")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);
			date = sDateFormat.format(new java.util.Date(milSeconds));
		} else if(format.equals("yyMMddHHmmss")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date(milSeconds)).substring(2);
		} else if(format.equals("yyyyMMddHHmmssSSS")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date(milSeconds));
		} else if(format.equals("yyyy-MM-dd HH:mm")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date(milSeconds));
		} else if(format.equals("yyyy-MM-dd")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date(milSeconds));	
		} else if(format.equals("yy-MM-dd HH:mm")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date(milSeconds));	
		} else if(format.equals("yy-MM-dd HH:mm:ss")) {
			sDateFormat = new SimpleDateFormat(format, Locale.CHINA);      
			date = sDateFormat.format(new java.util.Date(milSeconds));	
		}
		
		if(type != null && type.equals("url")) {
			date = date.replaceAll(" ", "%20");
		}
		
		return date;
	}	   
	
	public static String byteArray2String(byte[] bytArr, int pos, int len, boolean isHex) { 
		StringBuffer strBuf = new StringBuffer();
		String tmp = null;
		
		if(isHex) {
			for(int i=pos; i<(pos+len); i++) {
				tmp = Integer.toHexString(bytArr[i] & 0xFF).toUpperCase(Locale.CHINA);
				tmp = (tmp.length() == 1) ? ("0" + tmp) : tmp;
				strBuf.append(tmp + " ");
			}
			return strBuf.substring(0, strBuf.length());
		} else {
	        return new String(bytArr, pos, len); 
		}
	}   
	
    /**
     *  @Description    : 这个包名的程序是否在运行
     *  @Method_Name    : isRunningApp
     *  @param context 上下文
     *  @param packageName 判断程序的包名
     *  @return 必须加载的权限        <uses-permission android:name="android.permission.GET_TASKS"> 
     *  @return         : boolean
     */
    public static boolean isRunningApp(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(100);
        for (RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(packageName) && info.baseActivity.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }	
    
    /**
     * 转换图片成圆形
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public Bitmap toRoundBitmap(Bitmap bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float roundPx;
            float left,top,right,bottom,dst_left,dst_top,dst_right,dst_bottom;
            if (width <= height) {
                    roundPx = width / 2;
                    top = 0;
                    bottom = width;
                    left = 0;
                    right = width;
                    height = width;
                    dst_left = 0;
                    dst_top = 0;
                    dst_right = width;
                    dst_bottom = width;
            } else {
                    roundPx = height / 2;
                    float clip = (width - height) / 2;
                    left = clip;
                    right = width - clip;
                    top = 0;
                    bottom = height;
                    width = height;
                    dst_left = 0;
                    dst_top = 0;
                    dst_right = height;
                    dst_bottom = height;
            }
            
            Bitmap output = Bitmap.createBitmap(width,
                            height, Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            
            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect src = new Rect((int)left, (int)top, (int)right, (int)bottom);
            final Rect dst = new Rect((int)dst_left, (int)dst_top, (int)dst_right, (int)dst_bottom);
            final RectF rectF = new RectF(dst);

            paint.setAntiAlias(true);
            
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, src, dst, paint);
            return output;
    }    
	
}
