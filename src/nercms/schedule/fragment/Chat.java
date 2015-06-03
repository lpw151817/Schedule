package nercms.schedule.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nercms.schedule.R;
import nercms.schedule.adapter.ChatFragmentListAdapter;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.MessageDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.model.MessageModel;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockFragment;

public class Chat extends SherlockFragment{
	
	private DAOFactory daoFactory = DAOFactory.getInstance();

	private ListView recentMsgListView;

	private ChatFragmentListAdapter recentMsgAdapter;

	private MessageDao msgDao;

	private String userID;

	private Handler handler;

	private WebRequestManager webRequestManager;

	public static Chat newInstance(){
		Chat chatFragment = new Chat();
		return chatFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.chat_fragment, null);

		webRequestManager = new WebRequestManager(AppApplication.getInstance(),
				getActivity());

		userID = MySharedPreference.get(getActivity(),
				MySharedPreference.USER_ID, "");

		recentMsgListView = (ListView) v.findViewById(R.id.chat_list);
		
//		initData();

		initHandler();

		return v;
	}

	private void initData() {
		// 查询最新消息
		msgDao = daoFactory.getMessageDao(getActivity());
		ArrayList<MessageModel> recentMsgList = msgDao
				.getRecentMessageListByUserID(userID);

		// 对最新消息列表按照发送时间进行重新排序
		for (int i = 0; i < recentMsgList.size(); i++) {
			Collections.sort(recentMsgList, new Comparator<MessageModel>() {

				@Override
				public int compare(MessageModel m1, MessageModel m2) {
					return m2.getSendTime().compareTo(m1.getSendTime());
				}

			});
		}

		// 填充
		recentMsgAdapter = new ChatFragmentListAdapter(getActivity(),
				recentMsgList);

		recentMsgListView.setAdapter(recentMsgAdapter);
	}
	
	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {

				case Constant.SAVE_MESSAGE_SUCCESS:
					initData();
					break;

				default:
					break;
				}

			}

		};

		MessageHandlerManager.getInstance().register(handler,
				Constant.SAVE_MESSAGE_SUCCESS, "Main");
	}

	@Override
	public void onResume() {
		super.onResume();
		initData();
	}

}
