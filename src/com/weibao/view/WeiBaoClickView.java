package com.weibao.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android_serialport_api.MessageId;

import com.example.robot.R;
import com.smartboy.androidsdk.SmartBoyApiManager;
import com.smartboy.androidsdk.tts.TTSManager;
import com.weibao.robot.recognize.XunFeiRecognizer;

/**
 * Created by yugy on 2014/3/23.
 */
public class WeiBaoClickView extends View {

	public static final int STATE_NORMAL = 0;
	public static final int STATE_PRESSED = 1;
	public static final int STATE_RECORDING = 2;

	private Context context;

	private int mState = STATE_NORMAL;

	private float times = 5;// 根据调用识别引擎的不同，而设定不同的音量倍率 用以计算声波变化大小
	private Bitmap mNormalBitmap;
	private Bitmap mPressedBitmap;
	private Bitmap mRecordingBitmap;

	private Paint mPaint;
	private Paint mRecognizePaint;

	private AnimatorSet mAnimatorSet;

	private float mMinRadius;
	private float mMaxRadius;
	private float mCurrentRadius;

	private float targetHight;// 话筒的高度
	private float mHight;// 控件的总高度
	private float pressedHight;// 手指点击的高度 pressedHight<=(mHight-targetHight)
								// 那么点击失效
	private int mWidth;
	private Vibrator vibrator;
	private XunFeiRecognizer recognizeManager;
	private TTSManager ttsManager;
	private SmartBoyApiManager apiManager;
	private Handler handler;

	public SmartBoyApiManager getApiManager() {
		return apiManager;
	}

	public void setApiManager(SmartBoyApiManager apiManager) {
		this.apiManager = apiManager;
	}

	public XunFeiRecognizer getRecognizeManager() {
		return recognizeManager;
	}

	public void setRecognizeManager(XunFeiRecognizer recognizer) {
		this.recognizeManager = recognizer;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public TTSManager getTtsManager() {
		return ttsManager;
	}

	public void setTtsManager(TTSManager ttsManager) {
		this.ttsManager = ttsManager;
	}

	public WeiBaoClickView(Context context) {
		super(context);
		init(context);
	}

	public WeiBaoClickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public int getmState() {
		return mState;
	}

	public void setmState(int mState) {
		this.mState = mState;
	}

	public float getmMinRadius() {
		return mMinRadius;
	}

	public void setmMinRadius(float mMinRadius) {
		this.mMinRadius = mMinRadius;
	}

	public float getmMaxRadius() {
		return mMaxRadius;
	}

	public void setmMaxRadius(float mMaxRadius) {
		this.mMaxRadius = mMaxRadius;
	}

	public float getCurrentRadius() {
		return mCurrentRadius;
	}

	public void setCurrentRadius(float currentRadius) {
		mCurrentRadius = currentRadius;
		invalidate();
	}

	private void init(Context context) {
		this.context = context;
		mNormalBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.vs_micbtn_off);
		mPressedBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.vs_micbtn_pressed);
		mRecordingBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.vs_micbtn_off);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mRecognizePaint = new Paint();
		mRecognizePaint.setAntiAlias(true);
		mRecognizePaint.setColor(Color.argb(255, 211, 211, 211));// 97, 181,
																	// 207,
																	// 129

		mMinRadius = mNormalBitmap.getWidth() / 2;

		targetHight = mNormalBitmap.getHeight();
		mCurrentRadius = mMinRadius;
		if (android.os.Build.VERSION.SDK_INT > 10) {
			mAnimatorSet = new AnimatorSet();
		}
		vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mMaxRadius = Math.max(w, h) / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		mHight = height;
		mWidth = width;
		// 绘制呼吸效果{画圆的时候从圆心开始画
		if (mCurrentRadius > mMinRadius) {
			canvas.drawCircle(width / 2,
					height - mMinRadius - ScreenUtil.dip2px(context, 20),
					mCurrentRadius, mRecognizePaint);
		}
		// 画图片从图形的top&center开始画
		switch (mState) {
		case STATE_NORMAL:
			canvas.drawBitmap(mNormalBitmap, width / 2 - mMinRadius, height - 2
					* mMinRadius - ScreenUtil.dip2px(context, 20), mPaint);
			break;
		case STATE_PRESSED:
			canvas.drawBitmap(mPressedBitmap, width / 2 - mMinRadius, height
					- 2 * mMinRadius - ScreenUtil.dip2px(context, 20), mPaint);
			break;
		case STATE_RECORDING:
			canvas.drawBitmap(mRecordingBitmap, width / 2 - mMinRadius, height
					- 2 * mMinRadius - ScreenUtil.dip2px(context, 20), mPaint);
		}
		super.onDraw(canvas);
	}

	/**
	 * 设定动画的半径
	 * 
	 * @param radius
	 */
	private void animateRadius(float radius) {
		if (radius <= mCurrentRadius) {
			return;
		}
		if (radius > mMaxRadius) {
			radius = mMaxRadius;
		} else if (radius < mMinRadius) {
			radius = mMinRadius;
		}
		if (radius == mCurrentRadius) {
			return;
		}
		animateSelfRadius(radius);
	}

	/**
	 * 呼吸动画
	 * 
	 * @param radius
	 */
	public void animateSelfRadius(float radius) {
		if (android.os.Build.VERSION.SDK_INT > 10) {
			if (mAnimatorSet.isRunning()) {
				mAnimatorSet.cancel();
			}
			mAnimatorSet = null;
			mAnimatorSet = new AnimatorSet();
			mAnimatorSet.playSequentially(
					ObjectAnimator.ofFloat(this, "CurrentRadius",
							getCurrentRadius(), radius).setDuration(50),
					ObjectAnimator.ofFloat(this, "CurrentRadius", radius,
							mMinRadius).setDuration(400));
			mAnimatorSet.start();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (ttsManager == null || recognizeManager == null)
				return false;
			return actionDown(event);
		case MotionEvent.ACTION_UP:
		default:
			return super.onTouchEvent(event);
		}
	}

	/**
	 * 处理话筒点击事件。自动开启识别也用它。
	 */
	private void dealActionDown() {
		if (recognizeManager == null)
			return;
		Log.d("测试", "点击话筒");
		initVoiceView();
		ttsManager.pauseTTS();
		recognizeManager.stopRecord();
		handler.obtainMessage(MessageId.MESSAGE_START_RECOGNIZE_MP3)
				.sendToTarget();
	}

	/**
	 * 点击话筒时候的操作
	 * 
	 * @param event
	 * @return
	 */
	private boolean actionDown(MotionEvent event) {
		pressedHight = event.getY();
		if (pressedHight > (mHight - targetHight)) {
			dealActionDown();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 开始话筒呼吸动画，录音状态
	 * 
	 * @param volume
	 * @param isBaiduRecognizer
	 */
	public void updateRecordingAnima(int volume, boolean isBaiduRecognizer) {
		float radius_interval = getmMaxRadius() - getmMinRadius();
		if (isBaiduRecognizer) {
			times = radius_interval * 2 / 100;
		} else
			times = radius_interval * 2 / 30;
		float radius = (times + 2) * volume + getmMinRadius() - 12;
		animateRadius(radius);
	}

	/**
	 * 设置所有状态位为初始
	 */
	public void initVoiceView() {
		if (getmState() != STATE_NORMAL) {
			setmState(STATE_NORMAL);
			invalidate();
		}

	}

	public static interface OnRecordListener {
		/**
		 * VoiceView被点击
		 */
		public void ActionDown();
	}

}
