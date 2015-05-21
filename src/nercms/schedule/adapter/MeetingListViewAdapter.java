package nercms.schedule.adapter;

import java.util.ArrayList;

import nercms.schedule.R;
import nercms.schedule.activity.MeetingDetail;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.dao.ConferenceDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.model.ConferenceModel;

public class MeetingListViewAdapter extends BaseAdapter {

	private DAOFactory daoFactory = DAOFactory.getInstance();
	private ConferenceDao conDao;

	private Intent intent;
	private Context context;
	private ArrayList<ConferenceModel> conList;

	public MeetingListViewAdapter(Context context,
			ArrayList<ConferenceModel> conList) {
		this.context = context;
		this.conList = conList;
		this.conDao = daoFactory.getConferenceDao(context);
	}

	@Override
	public int getCount() {
		return conList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return conList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder=null;
		if(convertView==null){
			LayoutInflater inflater=LayoutInflater.from(context);
			convertView = inflater
					.inflate(R.layout.metting_fragment_item, null);
			holder = new Holder();
			holder.iconIv = (ImageView) convertView
					.findViewById(R.id.meeting_item_head);
			holder.timeTv = (TextView) convertView
					.findViewById(R.id.meeting_item_time);
			holder.statusTv = (TextView) convertView
					.findViewById(R.id.meeting_item_status);
			holder.titleTv = (TextView) convertView
					.findViewById(R.id.meeting_item_title);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		final ConferenceModel con = conList.get(position);

		if (con.getStatus() == 3) { // ԤԼ��
			// iconͼ��ΪĬ�ϵ�ԤԼ
			holder.timeTv.setText(con.getReservationTime());
			holder.statusTv.setText("ԤԼ��");
		} else if (con.getStatus() == 2) { // �������
			holder.iconIv.setImageResource(R.drawable.meeting_over);
			// ��ʾ����ʱ��
			// ���޽���ʱ�����ݣ��Դ���ʱ����� 2014-7-9 WeiHao
			holder.timeTv.setText(con.getCreateTime());
			holder.statusTv.setText("�ѽ���");
		}

		holder.titleTv.setText(con.getConferenceName());

		// �����ת�������������
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ..
				intent = new Intent(context, MeetingDetail.class);
				intent.putExtra("conference_id", con.getConferenceID());
				context.startActivity(intent);
			}
		});

		// ��������ȷ��ɾ��
		convertView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				// ..
				String messageString = "";
				if (con.getStatus() == 3) { // ԤԼ��
					messageString = "ȷ��ɾ����ԤԼ���飿\nɾ���󣬸�ԤԼҲ���ᱻȡ����";
				} else if (con.getStatus() == 2) { // �������
					messageString = "ȷ��ɾ���˻����¼��";
				}
				new AlertDialog.Builder(context)
						.setTitle("ɾ��")
						.setMessage(messageString)
						.setPositiveButton("ɾ��",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										// ..
										conDao.deleteConferenceByID(con
												.getConferenceID());
										conList.remove(position);
										MeetingListViewAdapter.this
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
		ImageView iconIv;
		TextView timeTv, statusTv, titleTv;
	}

}
