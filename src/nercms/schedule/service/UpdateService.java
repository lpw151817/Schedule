package nercms.schedule.service;

import java.util.Timer;
import java.util.TimerTask;

import nercms.schedule.R;
import nercms.schedule.activity.Login;
import nercms.schedule.activity.Main;
import nercms.schedule.utils.MyLog;
import nercms.schedule.utils.Utils;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.wxapp.service.AppApplication;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

public class UpdateService extends Service {
	private Timer mTimer;
	private TimerTask mTask;
	private Handler mHandler;
	private String TAG = "UpdateService";
	private WebRequestManager webRequestManager;
	// ����һСʱһ�ν�������
	// private final long TASK_PERIOD = 1000 * 60 * 60;
	private final long TASK_PERIOD = 1000 * 10;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		ini();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mTimer == null || mTask == null)
			ini();
		this.mTimer.schedule(mTask, 0, TASK_PERIOD);
		return super.onStartCommand(intent, flags, startId);
	}

	private void ini() {
		if (this.webRequestManager == null)
			webRequestManager = new WebRequestManager(AppApplication.getInstance(),
					getApplicationContext());
		if (this.mTask == null) {
			this.mTask = new TimerTask() {
				@Override
				public void run() {
					// ��ȡ��֯�����Ϣ���������ݿ�
					webRequestManager.getOrgCodeUpdate();
					webRequestManager.getOrgPersonUpdate();
					// ��ȡ�������
					webRequestManager.getAffairUpdate("1");
					// ��ȡ��Ϣ����
					webRequestManager.getMessageUpdate("1");
				}
			};
		}
		if (this.mTimer == null) {
			this.mTimer = new Timer();
		}

		iniHandler();
	}

	private void iniHandler() {
		this.mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// ��������Ļص�
				switch (msg.what) {
				// ����orgcodeʧ��
				case Constant.SAVE_ORG_CODE_FAIL:
					MyLog.e(TAG, "����orgcodeʧ��");
					break;
				// ����orgcode�ɹ�
				case Constant.SAVE_ORG_CODE_SUCCESS:
					MyLog.e(TAG, "����orgcode�ɹ�");
					break;
				// ��ȡ��֯���ʧ��
				case Constant.QUERY_ORG_NODE_REQUEST_FAIL:
					MyLog.i(TAG, "��ȡ���ʧ��");
					showAlterDialog("��ȡ���ʧ��",
							Utils.getErrorMsg(((NormalServerResponse) msg.obj).getEc()),
							R.drawable.login_error_icon);
					break;
				// �洢orgperson�ɹ�
				case Constant.SAVE_ORG_PERSON_SUCCESS:
					MyLog.e(TAG, "����orgperson�ɹ�");
					break;
				// �洢orgpersonʧ��
				case Constant.SAVE_ORG_PERSON_FAIL:
					MyLog.e(TAG, "����orgpersonʧ��");
					break;
				// ��ȡorgpersonʧ��
				case Constant.QUERY_ORG_PERSON_REQUEST_FAIL:
					showAlterDialog("��ȡorgpersonʧ��",
							Utils.getErrorMsg(((NormalServerResponse) msg.obj).getEc()),
							R.drawable.login_error_icon);
					break;
				// ������ϵ�����
				case Constant.SAVE_ALL_PERSON_SUCCESS:
					break;
				default:
					break;
				}
			}

		};

		// handler��ע��
		MessageHandlerManager.getInstance().register(mHandler, Constant.QUERY_ORG_NODE_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_ORG_CODE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		this.mTask.cancel();
		this.mTask = null;
		this.mTimer.cancel();
		this.mTimer = null;

		// handler������
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_ORG_NODE_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_ORG_CODE);
	}

	// ///////////////////�ڲ���ʾdialog�ķ���
	private void showAlterDialog(String title, String content, Integer icon, String pB,
			OnClickListener pbListener, String nB, OnClickListener nbListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
		builder.setTitle(title);
		if (content != null)
			builder.setMessage(content);
		if (icon != null)
			builder.setIcon(icon);
		if (pB != null && pbListener != null)
			builder.setPositiveButton(pB, pbListener);
		if (nB != null && nbListener != null)
			builder.setNegativeButton(nB, nbListener);
		builder.create().show();
	}

	protected void showAlterDialog(String title, String content, int icon, String pB,
			OnClickListener pbListener) {
		showAlterDialog(title, content, icon, pB, pbListener, null, null);
	}

	protected void showAlterDialog(String title, String content, int icon) {
		showAlterDialog(title, content, icon, null, null, null, null);
	}

	protected void showAlterDialog(String title, String content) {
		showAlterDialog(title, content, null, null, null, null, null);
	}

	protected void showAlterDialog(String title) {
		showAlterDialog(title, null, null, null, null, null, null);
	}
}
