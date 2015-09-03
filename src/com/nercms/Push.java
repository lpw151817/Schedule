package com.nercms;

import nercms.schedule.activity.ChatDetail;
import nercms.schedule.activity.ContactDetail;
import nercms.schedule.activity.MeetingDetail;
import nercms.schedule.activity.TaskDetail;
import nercms.schedule.fragment.Meeting;

import com.google.gson.Gson;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.wxapp.service.AppApplication;
import android.wxapp.service.R;
import android.wxapp.service.dao.AffairDao;
import android.wxapp.service.dao.ConferenceDao;
import android.wxapp.service.jerry.model.affair.QueryAffairInfoResponse;
import android.wxapp.service.jerry.model.conference.ConferenceUpdateQueryResponse;
import android.wxapp.service.jerry.model.conference.ConferenceUpdateQueryResponseItem;
import android.wxapp.service.jerry.model.mqtt.MqttResponse;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.MySharedPreference;

/*
 * 使用方法：
 * 1、创建对象：client = new Push();
 * 2、初始化：client.init("testClient", "test.mosquitto.org", 1883);
 * 3、设置回调函数：Callbacks mCallbacks = new Callbacks(); cleint.setCallbacks(mCallbacks);
 * 4、连接服务器：cleint.login();
 * 5、订阅/发布消息：cleint.addTag("tag1", 1);  cleint.pushMsgToTag("tag1", String.valueOf(pubCount), 1);
 * 		
 * */

public class Push {

	// ///topic：personid
	// ///clientid:m_personid
	// ///message:{type:int,id:int}

	private final String TAG = getClass().getName();

	public static String SERVER_URL = "202.114.66.77";
	private static final int PORT = 1883;
	private final String TOPIC_HEADER = "nercms/schedule/";
	public static String PERSON_ID = "";// personid
	private final int QOS = 1;
	// Qos 0: 至多一次,消息发布完全依赖底层网络,会发生消息丢失或重复;
	// Qos 1: 至少一次,确保消息到达,但消息重复可能会发生;
	// Qos 2: 只有一次, 确保消息到且只到达一次.

	private volatile static Push _unique_instance = null;

	Context c;

	public static Push get_instance(Context c) {
		if (null == _unique_instance) {
			synchronized (Push.class) {
				if (null == _unique_instance) {
					_unique_instance = new Push(c);
				}
			}
		}
		return _unique_instance;
	}

	public void ini() {
		Log.e("mqtt ini", "ini()");
		// clientid为m_开头
		init("m_" + PERSON_ID, SERVER_URL, PORT);
		setCallbacks(new ICallBacks() {

			@Override
			public int onMessageArrivedFunc(String client_id, String tag, int msg_len, String msg, int msg_qos,
					int duplicate_count) {
				Log.v(TAG, msg);
				// TODO 进行到达消息的处理
				try {
					WebRequestManager manager = new WebRequestManager(AppApplication.getInstance(), c);
					String userId = MySharedPreference.get(c, MySharedPreference.USER_ID, null);
					Gson gson = new Gson();
					MqttResponse response = gson.fromJson(msg, MqttResponse.class);
					Class target = null;
					Bundle b = null;
					b = new Bundle();
					String content = "您有新的";
					if (response != null) {
						switch (Integer.parseInt(response.getType())) {
						// 事务
						case 1:
							AffairDao affairDao = new AffairDao(c);
							QueryAffairInfoResponse affair = affairDao.getAffairInfoByAid(response.getId());
							// 如果不存在，则说明是新添加的
							if (affair == null) {
								manager.getAffair(response.getId());
							}
							// 如果存在，则说明是修改的信息
							else {

							}
							b.putBoolean("isNotice", true);
							b.putInt("aid", Integer.parseInt(response.getId()));
							target = TaskDetail.class;
							content += "事务";
							break;
						// 会议
						case 2:
							target = MeetingDetail.class;
							ConferenceDao conferenceDao = new ConferenceDao(c);
							ConferenceUpdateQueryResponseItem conference = conferenceDao
									.getConferenceByCid(response.getId());
							if (conference == null)
								manager.getConference(response.getId());
							b.putBoolean("isNotice", true);
							b.putString("conference_id", response.getId());
							content += "会议";
							break;
						// 个人消息
						case 3:
							target = ChatDetail.class;
							b.putInt("entrance_type", 1);
							content += "消息";
							break;
						// 群组消息
						case 4:
							target = ChatDetail.class;
							b.putInt("entrance_type", 1);
							content += "消息";
							break;
						// 事务反馈
						case 5:
							target = ChatDetail.class;
							b.putInt("entrance_type", 2);
							b.putString("task_id", response.getId());
							// TODO
							b.putInt("task_status", -1);
							content += "反馈";
							break;

						}

						showNotification(Push.this.c, target, b, content, "调度系统", content);

					}
				} catch (Exception e) {
					Log.e(TAG, "response.getType() 解析错误");
					e.printStackTrace();
				}
				return 0;
			}

			@Override
			public void onDeliveryCompleteFunc(String client_id, int tocken) {
				Log.v(TAG, "onDeliveryCompleteFunc ");
			}

			@Override
			public void onConnectionLostFunc(String client_id, String cause) {
				Log.v(TAG, "onConnectionLostFunc \t" + cause);
			}
		});
		login();
		// sub
		addTag(TOPIC_HEADER + "m_" + PERSON_ID, QOS);
	}

	private Push(Context c) {
		this.c = c;
		ini();
	}

	public void release() {
		Log.e("mqtt release", "release()");
		Log.e("mqtt connect state", state() + "");
		if (state() == 2) {
			// 已经连上服务器了
			logout();
			_unique_instance = null;
		}
	}

	static {
		try {
			System.loadLibrary("push"); // call .so
		} catch (UnsatisfiedLinkError e) {
			System.out.println("load lib push failed.");
		}
	}

	/**
	 * 初始化客户端 client_id：客户端的ID server_ip：服务端IP server_port:服务端端口号
	 * 
	 * @return 成功返回0，失败返回-1
	 */
	public native int init(String client_id, String server_ip, int server_port);

	/*
	 * 设置回调函数 callbacks：回调函数
	 */
	public native void setCallbacks(ICallBacks callbacks);

	/**
	 * 连接登录到服务器
	 * 
	 * @return 成功返回0，失败返回-1
	 * 
	 */
	public native int login();

	/**
	 * 订阅消息 tag:消息主题 qos:消息的qos
	 * 
	 * @param qos
	 *            Qos 0: 至多一次,消息发布完全依赖底层网络,会发生消息丢失或重复; Qos 1:
	 *            至少一次,确保消息到达,但消息重复可能会发生; Qos 2: 只有一次, 确保消息到且只到达一次.
	 * @return 成功返回0，失败返回-1
	 */
	public native int addTag(String tag, int qos);

	/**
	 * 取消订阅消息 tag:消息主题
	 * 
	 * @return 成功返回0，失败返回-1
	 */
	public native int removeTag(String tag);

	/**
	 * 客户端当前的连接状态
	 * 
	 * @return 返回2，表示已经连接到服务器 返回1，表示已创建客户端对象，还未连接到服务器
	 */
	public native int state();

	/**
	 * 发布消息 tag:消息主题 msg：消息内容 qos:消息的qos
	 * 
	 * @param qos
	 *            Qos 0: 至多一次,消息发布完全依赖底层网络,会发生消息丢失或重复; Qos 1:
	 *            至少一次,确保消息到达,但消息重复可能会发生; Qos 2: 只有一次, 确保消息到且只到达一次.
	 * @return 成功返回0，失败返回-1
	 */
	public native int pushMsgToTag(String tag, String msg, int qos);

	/**
	 * 断开连接
	 *
	 * @return 成功返回0，失败返回-1
	 */
	private native int logout();

	private long self_ptr;

	private String _client_id;

	private String _server_url;

	private void showNotification(Context c, Class<?> target, Bundle b, String trick, String title, String content) {
		NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.ic_launcher, trick, System.currentTimeMillis());
		n.flags = Notification.FLAG_AUTO_CANCEL;
		n.defaults = Notification.DEFAULT_ALL;
		long[] vibrate = { 0, 100, 200, 300 };
		n.vibrate = vibrate;
		Intent i = new Intent(c, target);
		if (b != null)
			i.putExtras(b);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		// PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(c, R.string.app_name, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		n.setLatestEventInfo(c, title, content, contentIntent);
		nm.notify(R.string.app_name, n);
	}
}
