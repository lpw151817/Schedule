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

		if (con.getStatus() == 3) { // 预约中
			// icon图标为默认的预约
			holder.timeTv.setText(con.getReservationTime());
			holder.statusTv.setText("预约中");
		} else if (con.getStatus() == 2) { // 会议结束
			holder.iconIv.setImageResource(R.drawable.meeting_over);
			// 显示结束时间
			// 暂无结束时间数据，以创建时间代替 2014-7-9 WeiHao
			holder.timeTv.setText(con.getCreateTime());
			holder.statusTv.setText("已结束");
		}

		holder.titleTv.setText(con.getConferenceName());

		// 点击跳转到会议详情界面
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ..
				intent = new Intent(context, MeetingDetail.class);
				intent.putExtra("conference_id", con.getConferenceID());
				context.startActivity(intent);
			}
		});

		// 长按弹出确认删除
		convertView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				// ..
				String messageString = "";
				if (con.getStatus() == 3) { // 预约中
					messageString = "确定删除此预约会议？\n删除后，该预约也将会被取消。";
				} else if (con.getStatus() == 2) { // 会议结束
					messageString = "确定删除此会议记录？";
				}
				new AlertDialog.Builder(context)
						.setTitle("删除")
						.setMessage(messageString)
						.setPositiveButton("删除",
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
								}).setNegativeButton("取消", null).create()
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
