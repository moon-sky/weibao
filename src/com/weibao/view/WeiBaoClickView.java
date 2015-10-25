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

	private float times = 5;// ���ݵ���ʶ������Ĳ�ͬ�����趨��ͬ���������� ���Լ��������仯��С
	private Bitmap mNormalBitmap;
	private Bitmap mPressedBitmap;
	private Bitmap mRecordingBitmap;

	private Paint mPaint;
	private Paint mRecognizePaint;

	private AnimatorSet mAnimatorSet;

	private float mMinRadius;
	private float mMaxRadius;
	private float mCurrentRadius;

	private float targetHight;// ��Ͳ�ĸ߶�
	private float mHight;// �ؼ����ܸ߶�
	private float pressedHight;// ��ָ����ĸ߶� pressedHight<=(mHight-targetHight)
								// ��ô���ʧЧ
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
		// ���ƺ���Ч��{��Բ��ʱ���Բ�Ŀ�ʼ��
		if (mCurrentRadius > mMinRadius) {
			canvas.drawCircle(width / 2,
					height - mMinRadius - ScreenUtil.dip2px(context, 20),
					mCurrentRadius, mRecognizePaint);
		}
		// ��ͼƬ��ͼ�ε�top&center��ʼ��
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
	 * �趨�����İ뾶
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
	 * ��������
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
	 * ����Ͳ����¼����Զ�����ʶ��Ҳ������
	 */
	private void dealActionDown() {
		if (recognizeManager == null)
			return;
		Log.d("����", "�����Ͳ");
		initVoiceView();
		ttsManager.pauseTTS();
		recognizeManager.stopRecord();
		handler.obtainMessage(MessageId.MESSAGE_START_RECOGNIZE_MP3)
				.sendToTarget();
	}

	/**
	 * �����Ͳʱ��Ĳ���
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
	 * ��ʼ��Ͳ����������¼��״̬
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
	 * ��������״̬λΪ��ʼ
	 */
	public void initVoiceView() {
		if (getmState() != STATE_NORMAL) {
			setmState(STATE_NORMAL);
			invalidate();
		}

	}

	public static interface OnRecordListener {
		/**
		 * VoiceView�����
		 */
		public void ActionDown();
	}

}
