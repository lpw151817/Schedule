package nercms.schedule.adapter;

import java.util.ArrayList;

import nercms.schedule.R;
import nercms.schedule.activity.ChatDetail;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.MessageDao;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.model.MessageModel;
import android.wxapp.service.util.MySharedPreference;

public class ChatFragmentListAdapter extends BaseAdapter {

	private DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao;
	private MessageDao msgDao;

	private Intent intent;

	private Context context;

	private ArrayList<MessageModel> msgList;

	private String userID = null;

	public ChatFragmentListAdapter(Context context,
			ArrayList<MessageModel> msgList) {
		this.context = context;
		this.msgList = msgList;
		this.personDao = daoFactory.getPersonDao(context);
		this.msgDao = daoFactory.getMessageDao(context);
		// �����û�ID
		userID = MySharedPreference
				.get(context, MySharedPreference.USER_ID, "");
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return msgList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return msgList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.chat_fragment_item, null);
			holder = new Holder();
			holder.nameText = (TextView) convertView
					.findViewById(R.id.message_list_name_tv);
			holder.contentText = (TextView) convertView
					.findViewById(R.id.message_list_content_tv);
			holder.timeText = (TextView) convertView
					.findViewById(R.id.message_list_time_tv);
			holder.unreadText = (TextView) convertView
					.findViewById(R.id.message_list_tips_num_tv);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		final MessageModel msg = msgList.get(position);
		final String objectID; // ��Ϣ����ID

		// 2014-7-30 WeiHao
		// ��������
		String name = "";
		if (msg.getIsGroup() == 1) { // �ж�ΪȺ��Ϣ
			objectID = Integer.toString(msg.getReceiverID());
			name = personDao.getOrgNodeByOrgID(objectID).getDescription(); // Ⱥ����
		} else { // �ж�Ϊ������Ϣ

			if (userID.equals(String.valueOf(msg.getSenderID()))) { // ����Ϊ������Ϣ������
				objectID = Integer.toString(msg.getReceiverID());
			} else { // ����Ϊ������Ϣ������
				objectID = Integer.toString(msg.getSenderID());
			}
			name = personDao.getSSMByID(objectID).getName();
		}

		final String tempName = name;

		holder.nameText.setText(name);
		// ʱ��
		holder.timeText.setText(msg.getSendTime());
		// ����һ����Ϣ
		// 2014-6-16 WeiHao
		// ���Ϊ�ı���Ϣ����ʾ�ı����ݣ����Ϊ������Ϣ����ʾ��Ӧ������
		if (msg.getAttachmentType() == 2) {
			holder.contentText.setText("[ͼƬ]");
		} else if (msg.getAttachmentType() == 3) {
			holder.contentText.setText("[��Ƶ]");
		} else {
			holder.contentText.setText(msg.getDescription());
		}

		// δ����Ϣ��
		int unreadNum = msgDao.getUnreadNumByIDs(userID, objectID);
		if (unreadNum != 0) {
			holder.unreadText.setVisibility(View.VISIBLE);
			holder.unreadText.setText(String.valueOf(unreadNum));
		} else {
			holder.unreadText.setVisibility(View.GONE);
		}

		// ���������Ϣ����¼�����ת����Ϣ�Ի�����
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ��������ö������Ϣ���Ϊ�Ѷ�
				msgDao.updateMessageIsRead(userID, objectID);

				// ..
				intent = new Intent(context, ChatDetail.class);
				intent.putExtra("entrance_type", 1);
				intent.putExtra("selected_id", Integer.parseInt(objectID));
				intent.putExtra("selected_name", tempName);
				context.startActivity(intent);

			}
		});

		// ���ó����¼�������ɾ���Ի���
		convertView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				new AlertDialog.Builder(context)
						.setTitle("ɾ��")
						.setMessage("ȷ��ɾ���� " + tempName + " ����Ϣ��¼��")
						.setPositiveButton("ɾ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// ɾ������˵���Ϣ��¼��ˢ���б�
										msgDao.deleteMessages(userID, objectID);
										msgList.remove(position);
										ChatFragmentListAdapter.this
												.notifyDataSetChanged();
									}
								}).setNegativeButton("ȡ��", null).create()
						.show();

				return false;
			}
		});

		return convertView;
	}

	class Holder {
		TextView nameText, contentText, timeText, unreadText;
	}
}
