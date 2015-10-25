package com.weibao.robot;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android_serialport_api.MessageId;
import android_serialport_api.ReceiveSerialPortThread;
import android_serialport_api.SerialPort;

import com.baidu.speechsynthesizer.SpeechSynthesizer;
import com.baidu.speechsynthesizer.SpeechSynthesizerListener;
import com.baidu.speechsynthesizer.publicutility.SpeechError;
import com.example.robot.R;
import com.lantu.comhelper.usb2serial.FT23X_ZE613;
import com.lantu.comhelper.utils.Comm;
import com.lantu.comhelper.utils.Variable;
import com.smartboy.androidsdk.HttpRequestWatcher;
import com.smartboy.androidsdk.SmartBoyApiConfig;
import com.smartboy.androidsdk.SmartBoyApiManager;
import com.smartboy.androidsdk.tts.TTSManager;
import com.smartboy.androidsdk.voice.BaiduAPIConfig;
import com.smartboy.androidsdk.voice.VoiceRecognizeListener;
import com.smartboy.androidsdk.voice.VoiceRecognizeManager;
import com.weibao.manager.BubbleManager;
import com.weibao.mediaplayer.MyLocalMediaPlayer;
import com.weibao.robot.recognize.IVoiceRecognizeListener;
import com.weibao.robot.recognize.XunFeiRecognizer;
import com.weibao.view.WeiBaoClickView;

public class MainActivity extends Activity implements IVoiceRecognizeListener {

	public VoiceRecognizeManager recognizeManager;
	public SerialPort mSerialPort;
	public ReceiveSerialPortThread mReadThread;
	private SmartBoyApiManager apiManager;
	private TTSManager ttsManager;
	public static boolean isRunBackground = true;
	private FT23X_ZE613 mFT23X_ZE613 = null;
	private StringBuilder builderRec = new StringBuilder();
	private byte[] bytArryRec = null; // 10K 接收区
	private int bytArryRecMax = 1024 * 1024;
	private int bytArryRecPos = 0;
	private boolean isGoingToRecognize = true;
	private WeiBaoClickView iv_mic;
	private MyLocalMediaPlayer mMediaPlayer;
	private BubbleManager mBubbleManager;
	private LinearLayout ll_content;
	private ScrollView sv_content;
	XunFeiRecognizer recognizer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		BaiduAPIConfig config = new BaiduAPIConfig("oHG1EkGVAOa7OYyEfXjF0QEl",
				"8b5bf0f30c5a5356be10dffa0d8b7f93");
		recognizer = new XunFeiRecognizer(this, this, handler);
		init(config);

		initControllSeri();

		ttsManager.startTTS("您好，欢迎光临，请问你有什么需要帮忙的吗");
		handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
				"您好，欢迎光临，请问你有什么需要帮忙的吗").sendToTarget();
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MessageId.MESSAGE_RECOGNIZE_RESULT:// 识别结果
				String result = (String) msg.obj;
				if (result != null) {
					apiManager.requestSmartboyAPI(result);
				}
				mBubbleManager.addRightBubble(result);
				break;
			case MessageId.MESSAGE_RECEIVE_CONTROLL_SERIALPORT:
				byte[] buffer = (byte[]) msg.obj;
				Toast.makeText(MainActivity.this, buffer.toString(),
						Toast.LENGTH_SHORT).show();
				try {
					mSerialPort.handleSerialPortSendInfo(buffer, handler);
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				break;
			case MessageId.MESSAGE_START_RECOGNIZE:
				if (isGoingToRecognize) {
					recognizer.startRecord();
					// recognizeManager
					// .startRecognize(new VoiceRecognitionConfig());
				}
				break;
			case MessageId.MESSAGE_STOP_RECOGNIZE:
				ttsManager.startTTS("欢迎您再次光临，谢谢");
				handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
						"欢迎您再次光临，谢谢").sendToTarget();
				break;
			case MessageId.MESSAGE_WELCOME_TIP:
				ttsManager.startTTS("您好，欢迎光临，请问你有什么需要帮忙的吗");
				handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
						"您好，欢迎光临，请问你有什么需要帮忙的吗").sendToTarget();
				break;
			case MessageId.MESSAGE_START_RECOGNIZE_MP3:
				mMediaPlayer.playLocalResource(R.raw.charging, handler,
						MessageId.MESSAGE_START_RECOGNIZE);
				break;
			case MessageId.MESSAGE_ADD_LEFT_BUTTON:
				mBubbleManager.addLeftBubble((String) (msg.obj));
				break;
			case MessageId.MESSAGE_ADD_RIGHT_BUTTON:
				mBubbleManager.addRightBubble((String) (msg.obj));
				break;

			}
		}
	};

	/**
	 * 初始化控制串口
	 */
	private void initControllSeri() {
		mFT23X_ZE613 = new FT23X_ZE613(this);

		new RunBackgroundAsyncTask().executeOnExecutor(
				Executors.newCachedThreadPool(), "runBackground ok");
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.mFT23X_ZE613.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isRunBackground = false;
		this.mFT23X_ZE613.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		this.mFT23X_ZE613.onStop();
	}

	private void init(BaiduAPIConfig config) {
		ll_content = (LinearLayout) findViewById(R.id.ll_content);
		sv_content = (ScrollView) findViewById(R.id.sv_content);

		// 音乐播放器
		mMediaPlayer = new MyLocalMediaPlayer(this);
		// 气泡管理
		mBubbleManager = new BubbleManager(this, ll_content, sv_content,
				handler);
		// 语音识别
		recognizeManager = new VoiceRecognizeManager(this, config,
				new VoiceRecognizeListener() {

					@Override
					public void onStartRecognize() {
						Log.d("测试", "onStartRecognize");
					}

					@Override
					public void onSpeechStart() {
						Log.d("测试", "onSpeechStart");
					}

					@Override
					public void onSpeechEnd() {
						Log.d("测试", "onSpeechEnd");
					}

					@Override
					public void onRecognizeResult(String arg0) {
						Log.d("测试", "onRecognizeResult" + arg0);
						handler.obtainMessage(
								MessageId.MESSAGE_RECOGNIZE_RESULT, arg0)
								.sendToTarget();
					}

					@Override
					public void onRecognizeError(String arg0) {
						Log.d("测试", "onRecognizeError" + arg0);
						handler.obtainMessage(
								MessageId.MESSAGE_START_RECOGNIZE_MP3)
								.sendToTarget();
					}
				});

		// api请求
		apiManager = new SmartBoyApiManager(new SmartBoyApiConfig(this,
				"242bd30c98f7dc66cc9a0e85bf7e267f"), new HttpRequestWatcher() {

			@Override
			public void onSuceess(String arg0) {
				Log.d("测试", "onSuceess" + arg0);
				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(arg0);
					if (arg0 != null) {
						String request = (String) (jsonObject.get("text"));
						ttsManager.startTTS(request);
						handler.obtainMessage(
								MessageId.MESSAGE_ADD_LEFT_BUTTON, request)
								.sendToTarget();
					}
				} catch (JSONException e) {
					e.printStackTrace();
					ttsManager.startTTS("不好意思，json字符串返回异常");
					handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
							"不好意思，json字符串返回异常").sendToTarget();
				}
			}

			@Override
			public void onError(String arg0) {
				Log.d("测试", "onError" + arg0);
				if (arg0 != null) {
					ttsManager.startTTS("网络请求失败");
					handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
							"网络请求失败").sendToTarget();
				}
			}
		});

		// tts
		ttsManager = new TTSManager(this, config,
				new SpeechSynthesizerListener() {

					@Override
					public void onSynthesizeFinish(SpeechSynthesizer arg0) {
					}

					@Override
					public void onStartWorking(SpeechSynthesizer arg0) {
					}

					@Override
					public void onSpeechStart(SpeechSynthesizer arg0) {
					}

					@Override
					public void onSpeechResume(SpeechSynthesizer arg0) {
					}

					@Override
					public void onSpeechProgressChanged(SpeechSynthesizer arg0,
							int arg1) {
					}

					@Override
					public void onSpeechPause(SpeechSynthesizer arg0) {
						Log.d("测试", "onSpeechPause" + arg0);
					}

					@Override
					public void onSpeechFinish(SpeechSynthesizer arg0) {
						Log.d("测试", "onSpeechFinish" + arg0);
						handler.obtainMessage(
								MessageId.MESSAGE_START_RECOGNIZE_MP3)
								.sendToTarget();
					}

					@Override
					public void onNewDataArrive(SpeechSynthesizer arg0,
							byte[] arg1, boolean arg2) {
					}

					@Override
					public void onError(SpeechSynthesizer arg0, SpeechError arg1) {
						Log.d("测试", "onError" + arg0);
					}

					@Override
					public void onCancel(SpeechSynthesizer arg0) {
					}

					@Override
					public void onBufferProgressChanged(SpeechSynthesizer arg0,
							int arg1) {
					}
				});

		iv_mic = (WeiBaoClickView) findViewById(R.id.iv_redpoint);
		iv_mic.setRecognizeManager(recognizer);
		iv_mic.setTtsManager(ttsManager);
		iv_mic.setApiManager(apiManager);
		iv_mic.setHandler(handler);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		isRunBackground = false;
		return super.onKeyDown(keyCode, event);
	}

	// 主Activity运行进程
	private class RunBackgroundAsyncTask extends
			AsyncTask<String, String, String> {
		/** 接收数据后确保先更新UI再发送下条指令 */
		private boolean LOCK = false; // 接收数据后，确保先更新UI，然后再发送下一条采集指令，否则容易错包，将错误的数据放到
										// UI 上了

		@Override
		protected void onPreExecute() { // 1> 在execute(Params... params)被调用后立即执行
			super.onPreExecute(); // 此函数体可以更新 UI
			// 10M
			bytArryRec = new byte[bytArryRecMax];
			bytArryRecPos = 0;
		}

		@Override
		protected String doInBackground(String... params) { // 2>
															// 在onPreExecute()
															// 后执行，无法更新
															// UI，多执行较耗时的操作
			int timeout = 0;
			try {
				while (isRunBackground) {
					Thread.sleep(200);

					if (isRunBackground && mFT23X_ZE613.isOk()) {

						// 接收机制处理@=@
						if (mFT23X_ZE613.getUnDealCount() > 0) {

							LOCK = true;
							this.publishProgress("edit_recArea");

							while (LOCK) { // 将返回数据结果用于线程更新
								try {
									Thread.sleep(10);
									if (timeout++ == 500) {
										LOCK = false;
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
									LOCK = false;
								}
							}
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				isRunBackground = false;
			}
			return "";
		}

		@Override
		// 3> doInBackground函数中调用 publishProgress(Progress... values)
		// 后系统调用这个方法，可更新UI主件 cmd = values[1], valid = values[2]
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

			if (values[0].equals("btn_send")) {
				// btn_send.performClick();
			} else if (values[0].equals("edit_recArea")) {
				byte[] bytArr = mFT23X_ZE613.getRecBytes(mFT23X_ZE613
						.getUnDealCount());
				// 第一个是要复制的数组，第二个是从要复制的数组的第几个开始，第三个是复制到那，四个是复制到的数组第几个开始，最后一个是复制长度
				System.arraycopy(bytArr, 0, bytArryRec, bytArryRecPos,
						bytArr.length);
				bytArryRecPos += bytArr.length;

				builderRec.append(Comm.byteArray2String(bytArr, 0,
						bytArr.length, Variable.getInstance(MainActivity.this)
								.isRecHex()));
				LOCK = false;

				if (bytArr[1] == 0) {
					isGoingToRecognize = true;
					handler.obtainMessage(MessageId.MESSAGE_WELCOME_TIP)
							.sendToTarget();
				} else {
					isGoingToRecognize = false;
					handler.obtainMessage(MessageId.MESSAGE_STOP_RECOGNIZE)
							.sendToTarget();
				}
				bytArr = null;
			}
		}

		@Override
		protected void onPostExecute(String result) { // 4>
														// 当后台结束，调用此方法，Result传递进来
			super.onPostExecute(result);
		}
	}

	@Override
	public void onReadyForSpeach(Object param) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResults(ArrayList<String> result, int type) {
		handler.obtainMessage(MessageId.MESSAGE_RECOGNIZE_RESULT, result.get(0))
				.sendToTarget();

	}

	@Override
	public void onEndOfRecord(Object param) {
		iv_mic.initVoiceView();
	}

	@Override
	public void onBeginRecord() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError() {
		// TODO 主动询问
		apiManager.requestSmartboyQuestion();
	}

	@Override
	public void onVolumeChange(int volume) {
		if (recognizer.isRecording())
			iv_mic.updateRecordingAnima(volume, false);
	}
}
