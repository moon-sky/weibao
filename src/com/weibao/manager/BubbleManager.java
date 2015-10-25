package com.weibao.manager;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.robot.R;
import com.weibao.robot.ITtsCallBack;

public class BubbleManager {
	private Context context;
	private LinearLayout ll_bubble;
	private Handler handler;
	private ScrollView sv_content;

	private ITtsCallBack ttsCallBackWatcher = null;

	/**
	 * 构造方法
	 * 
	 * @param ctx
	 * @param ll_Bubble
	 * @param scrollView
	 * @param handler
	 */
	public BubbleManager(Context ctx, LinearLayout ll_Bubble,
			ScrollView scrollView, Handler handler) {
		this.context = ctx;
		this.ll_bubble = ll_Bubble;
		this.handler = handler;
		this.sv_content = scrollView;

	}

	public ITtsCallBack getTtsCallBackWatcher() {
		return ttsCallBackWatcher;
	}

	public void setTtsCallBackWatcher(ITtsCallBack ttsCallBackWatcher) {
		this.ttsCallBackWatcher = ttsCallBackWatcher;
	}

	/**
	 * 添加左气泡
	 * 
	 * @param entity
	 * @param text
	 * @param needTTS
	 * @param callBack
	 */
	public void addLeftBubble(String text) {
		View leftBubble = LayoutInflater.from(context).inflate(
				R.layout.item_bubble_left, null);
		TextView tv_content = (TextView) leftBubble
				.findViewById(R.id.tv_content);
		tv_content.setText(text);
		ll_bubble.addView(leftBubble);
		sv_content.post(new Runnable() {
			public void run() {
				sv_content.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	/**
	 * 添加右气泡
	 * 
	 * @param text
	 */
	public void addRightBubble(String text) {
		View rightBubble = LayoutInflater.from(context).inflate(
				R.layout.item_bubble_right, null);
		TextView tv_content = (TextView) rightBubble
				.findViewById(R.id.tv_content);
		tv_content.setText(text);
		ll_bubble.addView(rightBubble);
		sv_content.post(new Runnable() {
			public void run() {
				sv_content.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

}
