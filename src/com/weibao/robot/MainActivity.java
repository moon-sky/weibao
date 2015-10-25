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
	private byte[] bytArryRec = null; // 10K ������
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

		ttsManager.startTTS("���ã���ӭ���٣���������ʲô��Ҫ��æ����");
		handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
				"���ã���ӭ���٣���������ʲô��Ҫ��æ����").sendToTarget();
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MessageId.MESSAGE_RECOGNIZE_RESULT:// ʶ����
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
				ttsManager.startTTS("��ӭ���ٴι��٣�лл");
				handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
						"��ӭ���ٴι��٣�лл").sendToTarget();
				break;
			case MessageId.MESSAGE_WELCOME_TIP:
				ttsManager.startTTS("���ã���ӭ���٣���������ʲô��Ҫ��æ����");
				handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
						"���ã���ӭ���٣���������ʲô��Ҫ��æ����").sendToTarget();
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
	 * ��ʼ�����ƴ���
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

		// ���ֲ�����
		mMediaPlayer = new MyLocalMediaPlayer(this);
		// ���ݹ���
		mBubbleManager = new BubbleManager(this, ll_content, sv_content,
				handler);
		// ����ʶ��
		recognizeManager = new VoiceRecognizeManager(this, config,
				new VoiceRecognizeListener() {

					@Override
					public void onStartRecognize() {
						Log.d("����", "onStartRecognize");
					}

					@Override
					public void onSpeechStart() {
						Log.d("����", "onSpeechStart");
					}

					@Override
					public void onSpeechEnd() {
						Log.d("����", "onSpeechEnd");
					}

					@Override
					public void onRecognizeResult(String arg0) {
						Log.d("����", "onRecognizeResult" + arg0);
						handler.obtainMessage(
								MessageId.MESSAGE_RECOGNIZE_RESULT, arg0)
								.sendToTarget();
					}

					@Override
					public void onRecognizeError(String arg0) {
						Log.d("����", "onRecognizeError" + arg0);
						handler.obtainMessage(
								MessageId.MESSAGE_START_RECOGNIZE_MP3)
								.sendToTarget();
					}
				});

		// api����
		apiManager = new SmartBoyApiManager(new SmartBoyApiConfig(this,
				"242bd30c98f7dc66cc9a0e85bf7e267f"), new HttpRequestWatcher() {

			@Override
			public void onSuceess(String arg0) {
				Log.d("����", "onSuceess" + arg0);
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
					ttsManager.startTTS("������˼��json�ַ��������쳣");
					handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
							"������˼��json�ַ��������쳣").sendToTarget();
				}
			}

			@Override
			public void onError(String arg0) {
				Log.d("����", "onError" + arg0);
				if (arg0 != null) {
					ttsManager.startTTS("��������ʧ��");
					handler.obtainMessage(MessageId.MESSAGE_ADD_LEFT_BUTTON,
							"��������ʧ��").sendToTarget();
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
						Log.d("����", "onSpeechPause" + arg0);
					}

					@Override
					public void onSpeechFinish(SpeechSynthesizer arg0) {
						Log.d("����", "onSpeechFinish" + arg0);
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
						Log.d("����", "onError" + arg0);
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

	// ��Activity���н���
	private class RunBackgroundAsyncTask extends
			AsyncTask<String, String, String> {
		/** �������ݺ�ȷ���ȸ���UI�ٷ�������ָ�� */
		private boolean LOCK = false; // �������ݺ�ȷ���ȸ���UI��Ȼ���ٷ�����һ���ɼ�ָ��������״��������������ݷŵ�
										// UI ����

		@Override
		protected void onPreExecute() { // 1> ��execute(Params... params)�����ú�����ִ��
			super.onPreExecute(); // �˺�������Ը��� UI
			// 10M
			bytArryRec = new byte[bytArryRecMax];
			bytArryRecPos = 0;
		}

		@Override
		protected String doInBackground(String... params) { // 2>
															// ��onPreExecute()
															// ��ִ�У��޷�����
															// UI����ִ�нϺ�ʱ�Ĳ���
			int timeout = 0;
			try {
				while (isRunBackground) {
					Thread.sleep(200);

					if (isRunBackground && mFT23X_ZE613.isOk()) {

						// ���ջ��ƴ���@=@
						if (mFT23X_ZE613.getUnDealCount() > 0) {

							LOCK = true;
							this.publishProgress("edit_recArea");

							while (LOCK) { // ���������ݽ�������̸߳���
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
		// 3> doInBackground�����е��� publishProgress(Progress... values)
		// ��ϵͳ��������������ɸ���UI���� cmd = values[1], valid = values[2]
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);

			if (values[0].equals("btn_send")) {
				// btn_send.performClick();
			} else if (values[0].equals("edit_recArea")) {
				byte[] bytArr = mFT23X_ZE613.getRecBytes(mFT23X_ZE613
						.getUnDealCount());
				// ��һ����Ҫ���Ƶ����飬�ڶ����Ǵ�Ҫ���Ƶ�����ĵڼ�����ʼ���������Ǹ��Ƶ��ǣ��ĸ��Ǹ��Ƶ�������ڼ�����ʼ�����һ���Ǹ��Ƴ���
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
														// ����̨���������ô˷�����Result���ݽ���
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
		// TODO ����ѯ��
		apiManager.requestSmartboyQuestion();
	}

	@Override
	public void onVolumeChange(int volume) {
		if (recognizer.isRecording())
			iv_mic.updateRecordingAnima(volume, false);
	}
}
