package nercms.schedule.activity;

import java.io.File;

import nercms.schedule.R;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.MyLog;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.jerry.model.person.LoginResponse;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MQTT;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author jiaocuina@gmail.com
 * @date 2014-03-02
 * @version V1.0
 * @description �����û������� ��¼ϵͳ
 * 
 * @version V1.1
 * @author WEIHAO
 * @date 2014-6-25
 * @new �û���¼һϵ�е��߼������� ���ؽ�������ʾ����ϵ�����ر�����ɺ���ת
 * @version V1.3
 * @new ��¼�ɹ���ע��MQTT
 */
public class Login extends SherlockActivity {

	private EditText etUserName; // �û����༭��
	private EditText etPassword; // ����༭��

	private Button btnLogin;// ��¼��ť

	private String inputUserName = null;
	private String inputPassword = null;

	private Handler handler;

	private WebRequestManager webRequestManager;

	private String TAG = "Login";

	private ProgressDialog progressDialog = null;

	private PersonDao personDao;

	private String lastUserID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		System.out.println("ping +" + LocalConstant.APP_SERVER_IP + ">>>>>>>>>>" + Utils.serverPing());

		Log.v("Login", "Login onCreate");
		webRequestManager = new WebRequestManager(AppApplication.getInstance(), Login.this);

		personDao = DAOFactory.getInstance().getPersonDao(Login.this);

		initActionBar();

		etUserName = (EditText) findViewById(R.id.login_user_edit);
		etPassword = (EditText) findViewById(R.id.login_passwd_edit);

		// Ĭ����ʾ�ϴε�¼���û�ID
		etUserName.setText(MySharedPreference.get(Login.this, MySharedPreference.USER_NAME, ""));

		btnLogin = (Button) findViewById(R.id.login_login_btn);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MyLog.i(TAG, "��¼��ť���");
				login_mainschedule();
			}
		});

	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("��¼");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// ���ð�ť
		MenuItem setting = menu.add(0, 1, 0, "����");
		setting.setIcon(R.drawable.ofm_setting_icon);
		setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		setting.setVisible(false);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			startActivity(new Intent(Login.this, Setting.class));
			break;

		default:
			break;
		}
		return true;
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				// ��¼�ɹ�
				case Constant.LOGIN_REQUEST_SUCCESS:

					// �����û�ID
					String userID = ((LoginResponse) msg.obj).getUid();

					MySharedPreference.save(Login.this, MySharedPreference.USER_ID, userID);

					MySharedPreference.save(Login.this, MySharedPreference.USER_NAME, inputUserName);

					// 2014-6-24 WeiHao
					// 2.mqtt����
					Log.v("Login", "id:" + userID);
					MQTT.CLIENT_ID = userID;
					MQTT mqtt = MQTT.get_instance();
					mqtt.publish_message(MQTT.SUBSCRIBE_TOPIC_PREFIX + MQTT.CLIENT_ID, "Registration", 0);
					// 3.��������������ϵ�ˣ�������ݱ�Ϊ�գ�
					if (personDao.isDBTNull()) {
						// todo ������֯�ṹ��
						webRequestManager.getAllPerson();
					} else {
						lastUserID = MySharedPreference
								.get(Login.this, MySharedPreference.USER_ID, null);
						if (lastUserID != null && lastUserID.equalsIgnoreCase(userID)) {
							// 4.ȡ��������ʾ
							progressDialog.dismiss();
							// 5.��ת��������
							startActivity(new Intent(Login.this, Main.class));
							Login.this.finish();
						} else {
							// �л���½�û�������������ϵ��
							webRequestManager.getAllPerson();
						}
					}

					// д��־
					MyLog.i(TAG, "�û���" + userID + " ��½�ɹ�");
					break;
				// ��¼ ʧ��
				case Constant.LOGIN_REQUEST_FAIL:
					MyLog.i(TAG, "��¼ʧ��");
					progressDialog.dismiss();
					String errorCode = ((NormalServerResponse) msg.obj).getEc();
					new AlertDialog.Builder(Login.this)
							.setIcon(getResources().getDrawable(R.drawable.login_error_icon))
							.setTitle("��¼ʧ��").setMessage(Utils.getErrorMsg(errorCode)).create().show();
					break;
				// ������ϵ�����
				case Constant.SAVE_ALL_PERSON_SUCCESS:
					// 4.ȡ��������ʾ
					progressDialog.dismiss();
					// 5.��ת��������
					startActivity(new Intent(Login.this, Main.class));
					Login.this.finish();
					break;

				default:
					break;
				}

			}
		};

		// ע��Handler
		MessageHandlerManager.getInstance().register(handler, Constant.LOGIN_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().register(handler, Constant.LOGIN_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_ALL_PERSON_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
	}

	// �ж��û������������Ҫ�����¼�ɹ�

	public void login_mainschedule() {

		// 2014-6-24 WeiHao
		// 1.��ʾ������
		progressDialog = ProgressDialog.show(Login.this, "���ڵ�¼", "Ŭ������������...���Ժ�~");
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		// ���������
		InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		mInputMethodManager.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

		if (!checkInternet()) {
			progressDialog.dismiss();
			return;
		}

		if (!checkSDCard()) {
			progressDialog.dismiss();
			return;
		}

		inputUserName = etUserName.getText().toString().trim();
		inputPassword = etPassword.getText().toString().trim();

		if (inputUserName == null || inputUserName.equals("") || inputPassword == null
				|| inputPassword.equals("")) {
			progressDialog.dismiss();
			new AlertDialog.Builder(Login.this)
					.setIcon(getResources().getDrawable(R.drawable.login_error_icon)).setTitle("��¼����")
					.setMessage("�ʺŻ������벻��Ϊ�գ�\n��������ٵ�¼��").create().show();
			return;
		}
		// ������������е�¼��֤
		String imsi = Utils.getIMSI(Login.this);
		webRequestManager.loginVarification(inputUserName, inputPassword, imsi);
	}

	@Override
	protected void onResume() {
		// ע���������ע��Handler
		initHandler();
		Log.v("Login", "OnResume,ע���������ע��Handler");
		super.onResume();
	}

	public boolean checkSDCard() {
		// SD���жϲ���ʾ
		if (Utils.isExistSDCard()) {
			if (Utils.getSDFreeSize() < 200) { // �洢�ռ�С��200MB
				Toast.makeText(Login.this, "����SD���洢�ռ䲻�㣬�뼰ʱ��������������ޣ�", Toast.LENGTH_SHORT).show();
				return false;
			} else { // SD�������ҿռ��㹻������Ŀ¼׼��
				// ����һ���ļ��ж��󣬸�ֵΪ�ⲿ�洢����Ŀ¼
				File sdcardDir = Environment.getExternalStorageDirectory();
				// �õ�һ��·����������sdcard�ĸ���·��
				String path1 = sdcardDir.getPath() + "/nercms-Schedule/Attachments";
				File filePath1 = new File(path1);
				// �õ�һ��·����������sdcard�ĸ�������ͼ·��
				String path2 = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail";
				File filePath2 = new File(path2);

				if (!filePath1.exists()) {
					// �������ڣ�����Ŀ¼
					filePath1.mkdirs();
				}

				if (!filePath2.exists()) {
					filePath2.mkdirs();
				}
				return true;
			}
		} else {
			Toast.makeText(Login.this, "δ��⵽SD�������������ޣ�", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private boolean checkInternet() {
		ConnectivityManager connectivityManager;
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {
			String name = info.getTypeName();
			Log.d(TAG, "��ǰ�������ƣ�" + name);
			// pingӦ�÷�����
			// if (Utils.serverPing()) {
			// return true;
			// } else {
			// Toast.makeText(Login.this, "�������Ӳ�����", Toast.LENGTH_SHORT)
			// .show();
			// return false;
			// }
			return true;
		} else {
			Toast.makeText(Login.this, "���������", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// ������ؼ�
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// ������ڵ�¼��ȡ����¼
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
				AppApplication.getInstance().myQueue.cancelAll(this);
				Utils.showShortToast(this, "ȡ����¼");
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// ע��Handler
		MessageHandlerManager.getInstance().unregister(Constant.LOGIN_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().unregister(Constant.LOGIN_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_ALL_PERSON_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
		Log.v("Login", "onDestroy,ע��Handler");
		super.onDestroy();
	}
}
