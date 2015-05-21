package nercms.schedule.adapter;

import java.util.ArrayList;

import nercms.schedule.R;
import nercms.schedule.activity.TaskDetail;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.dao.AffairDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.dao.PersonOnDutyDao;
import android.wxapp.service.model.AffairModel;
import android.wxapp.service.model.PersonOnDutyModel;
import android.wxapp.service.model.StructuredStaffModel;

public class TaskListViewAdapter extends BaseAdapter {

	private DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao;
	private PersonOnDutyDao podDao;
	private AffairDao affairDao;
	private PersonOnDutyModel pod;
	private StructuredStaffModel podSSM;

	private Intent intent;

	private int entranceType;
	private int entraceStatus;

	private ArrayList<AffairModel> mList; // 任务列表
	private Context mContext;

	public TaskListViewAdapter(ArrayList<AffairModel> list, Context context,
			int entranceType, int entranceStatus) {
		this.mList = list;
		this.mContext = context;
		podDao = daoFactory.getPersonOnDutyDao(mContext);
		personDao = daoFactory.getPersonDao(mContext);
		affairDao = daoFactory.getAffairDao(mContext);
		this.entranceType = entranceType;
		this.entraceStatus = entranceStatus;
	}

	public void refresh(ArrayList<AffairModel> list) {
		mList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.task_list_item, null);
			holder = new Holder();
			holder.mTitleText = ((TextView) convertView
					.findViewById(R.id.itemlist_title));
			holder.mContentText = ((TextView) convertView
					.findViewById(R.id.itemlist_content));
			holder.mParticipatorText = ((TextView) convertView
					.findViewById(R.id.itemlist_participator));
			holder.mDeadlineText = ((TextView) convertView
					.findViewById(R.id.itemlist_deadline));
			// holder.mReplyText = ((TextView) convertView
			// .findViewById(R.id.itemlist_reply));
			holder.mNewTips = (ImageView) convertView
					.findViewById(R.id.itemlist_new_tips);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.mTitleText.setText(mList.get(position).getTitle());
		holder.mContentText.setText(mList.get(position).getDescription());

		// 查询任务责任人

		pod = podDao.getPersonByAffairID(mList.get(position)
				.getAffairID());
		// 根据责任人ID查询责任人姓名

		podSSM = personDao.getSSMByID(Integer.toString(pod
				.getPersonID()));

		holder.mParticipatorText.setText(podSSM.getName());
		holder.mDeadlineText.setText(mList.get(position).getEndTime());
		// holder.mReplyText.setText(Integer.toString(1));// 回复条数数据如何取
		
		if (affairDao.getAffairIsReadByID(mList.get(position).getAffairID()) == 0) {
			holder.mNewTips.setVisibility(View.VISIBLE);
		} else {
			holder.mNewTips.setVisibility(View.GONE);
		}

		//设置任务点击事件
		convertView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// 标记未已读
				affairDao.updateAffairIsRead(mList.get(position).getAffairID());

				intent = new Intent(mContext, TaskDetail.class);
				intent.putExtra("id", mList.get(position).getAffairID());
				intent.putExtra("type", entranceType);
				intent.putExtra("status", entraceStatus);
				mContext.startActivity(intent);
			}
		});
		
		return convertView;
	}

	class Holder {
		TextView mTitleText, mContentText, mParticipatorText, mDeadlineText,
				mReplyText;
		ImageView mNewTips;
	}
}


