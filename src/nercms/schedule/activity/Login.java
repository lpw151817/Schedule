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
 * @description 输入用户名密码 登录系统
 * 
 * @version V1.1
 * @author WEIHAO
 * @date 2014-6-25
 * @new 用户登录一系列的逻辑修正； 加载进度条显示，联系人下载保存完成后跳转
 * @version V1.3
 * @new 登录成功后，注册MQTT
 */
public class Login extends SherlockActivity {

	private EditText etUserName; // 用户名编辑框
	private EditText etPassword; // 密码编辑框

	private Button btnLogin;// 登录按钮

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

		// 默认显示上次登录的用户ID
		etUserName.setText(MySharedPreference.get(Login.this, MySharedPreference.USER_NAME, ""));

		btnLogin = (Button) findViewById(R.id.login_login_btn);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MyLog.i(TAG, "登录按钮点击");
				login_mainschedule();
			}
		});

	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("登录");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// 设置按钮
		MenuItem setting = menu.add(0, 1, 0, "设置");
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
				// 登录成功
				case Constant.LOGIN_REQUEST_SUCCESS:

					// 接收用户ID
					String userID = ((LoginResponse) msg.obj).getUid();

					MySharedPreference.save(Login.this, MySharedPreference.USER_ID, userID);

					MySharedPreference.save(Login.this, MySharedPreference.USER_NAME, inputUserName);

					// 2014-6-24 WeiHao
					// 2.mqtt订阅
					Log.v("Login", "id:" + userID);
					MQTT.CLIENT_ID = userID;
					MQTT mqtt = MQTT.get_instance();
					mqtt.publish_message(MQTT.SUBSCRIBE_TOPIC_PREFIX + MQTT.CLIENT_ID, "Registration", 0);
					// 3.请求下载所有联系人（如果数据表为空）
					if (personDao.isDBTNull()) {
						// todo 更改组织结构树
						webRequestManager.getAllPerson();
					} else {
						lastUserID = MySharedPreference
								.get(Login.this, MySharedPreference.USER_ID, null);
						if (lastUserID != null && lastUserID.equalsIgnoreCase(userID)) {
							// 4.取消进度显示
							progressDialog.dismiss();
							// 5.跳转到主界面
							startActivity(new Intent(Login.this, Main.class));
							Login.this.finish();
						} else {
							// 切换登陆用户，重新下载联系人
							webRequestManager.getAllPerson();
						}
					}

					// 写日志
					MyLog.i(TAG, "用户：" + userID + " 登陆成功");
					break;
				// 登录 失败
				case Constant.LOGIN_REQUEST_FAIL:
					MyLog.i(TAG, "登录失败");
					progressDialog.dismiss();
					String errorCode = ((NormalServerResponse) msg.obj).getEc();
					new AlertDialog.Builder(Login.this)
							.setIcon(getResources().getDrawable(R.drawable.login_error_icon))
							.setTitle("登录失败").setMessage(Utils.getErrorMsg(errorCode)).create().show();
					break;
				// 保存联系人完成
				case Constant.SAVE_ALL_PERSON_SUCCESS:
					// 4.取消进度显示
					progressDialog.dismiss();
					// 5.跳转到主界面
					startActivity(new Intent(Login.this, Main.class));
					Login.this.finish();
					break;

				default:
					break;
				}

			}
		};

		// 注册Handler
		MessageHandlerManager.getInstance().register(handler, Constant.LOGIN_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().register(handler, Constant.LOGIN_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_ALL_PERSON_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
	}

	// 判断用户名与密码符合要求则登录成功

	public void login_mainschedule() {

		// 2014-6-24 WeiHao
		// 1.显示进度条
		progressDialog = ProgressDialog.show(Login.this, "正在登录", "努力加载数据中...请稍后~");
		progressDialog.setCancelable(true);
		progressDialog.setCanceledOnTouchOutside(false);
		// 隐藏软键盘
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
					.setIcon(getResources().getDrawable(R.drawable.login_error_icon)).setTitle("登录错误")
					.setMessage("帐号或者密码不能为空，\n请输入后再登录！").create().show();
			return;
		}
		// 请求服务器进行登录验证
		String imsi = Utils.getIMSI(Login.this);
		webRequestManager.loginVarification(inputUserName, inputPassword, imsi);
	}

	@Override
	protected void onResume() {
		// 注册或者重新注册Handler
		initHandler();
		Log.v("Login", "OnResume,注册或者重新注册Handler");
		super.onResume();
	}

	public boolean checkSDCard() {
		// SD卡判断并提示
		if (Utils.isExistSDCard()) {
			if (Utils.getSDFreeSize() < 200) { // 存储空间小于200MB
				Toast.makeText(Login.this, "您的SD卡存储空间不足，请及时清理，避免操作受限！", Toast.LENGTH_SHORT).show();
				return false;
			} else { // SD卡挂载且空间足够，附件目录准备
				// 创建一个文件夹对象，赋值为外部存储器的目录
				File sdcardDir = Environment.getExternalStorageDirectory();
				// 得到一个路径，内容是sdcard的附件路径
				String path1 = sdcardDir.getPath() + "/nercms-Schedule/Attachments";
				File filePath1 = new File(path1);
				// 得到一个路径，内容是sdcard的附件缩略图路径
				String path2 = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail";
				File filePath2 = new File(path2);

				if (!filePath1.exists()) {
					// 若不存在，创建目录
					filePath1.mkdirs();
				}

				if (!filePath2.exists()) {
					filePath2.mkdirs();
				}
				return true;
			}
		} else {
			Toast.makeText(Login.this, "未检测到SD卡，操作将受限！", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private boolean checkInternet() {
		ConnectivityManager connectivityManager;
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {
			String name = info.getTypeName();
			Log.d(TAG, "当前网络名称：" + name);
			// ping应用服务器
			// if (Utils.serverPing()) {
			// return true;
			// } else {
			// Toast.makeText(Login.this, "网络连接不可用", Toast.LENGTH_SHORT)
			// .show();
			// return false;
			// }
			return true;
		} else {
			Toast.makeText(Login.this, "无网络访问", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 点击返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 如果正在登录，取消登录
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
				AppApplication.getInstance().myQueue.cancelAll(this);
				Utils.showShortToast(this, "取消登录");
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// 注销Handler
		MessageHandlerManager.getInstance().unregister(Constant.LOGIN_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().unregister(Constant.LOGIN_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_ALL_PERSON_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
		Log.v("Login", "onDestroy,注册Handler");
		super.onDestroy();
	}
}
