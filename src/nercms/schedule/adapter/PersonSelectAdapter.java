package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.imooc.treeview.utils.Node;
import com.imooc.treeview.utils.adapter.TreeListViewAdapter;

public class PersonSelectAdapter<T> extends TreeListViewAdapter<T> {

	List<Node> selected = new ArrayList<Node>();

	public interface DataChanged {
		public void onChanged(int size);
	}

	DataChanged changed;

	public void setDataChangedListener(DataChanged listener) {
		this.changed = listener;
	}

	public List<Node> getSelectedDate() {
		return this.selected;
	}

	// public PersonSelectAdapter(ListView tree, Context context, List<T> datas,
	// int defaultExpandLevel) throws IllegalArgumentException,
	// IllegalAccessException {
	// super(tree, context, datas, defaultExpandLevel);
	// }

	private List<Node> lsSelectedPod;
	private List<Node> lsSelectedReceiver;
	int entranceFlag, type;

	public PersonSelectAdapter(ListView tree, Context context, List<T> datas,
			int defaultExpandLevel, List<Node> lsPod, List<Node> lsReceiver, int entranceFlag,
			int type) throws IllegalArgumentException, IllegalAccessException {
		super(tree, context, datas, defaultExpandLevel);
		this.lsSelectedPod = lsPod;
		this.lsSelectedReceiver = lsReceiver;
		this.entranceFlag = entranceFlag;
		this.type = type;
	}

	@Override
	public View getConvertView(final Node node, int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		// if (convertView == null) {
		if (node.getId().startsWith("p")) {
			convertView = mInflater.inflate(R.layout.list_item_select, parent, false);
			holder = new ViewHolder();
			holder.mIcon = (ImageView) convertView.findViewById(R.id.id_treenode_icon);
			holder.mPic = (ImageView) convertView.findViewById(R.id.id_treednode_pic);
			holder.mPic.setImageResource(R.drawable.orgperson);
			holder.mText = (TextView) convertView.findViewById(R.id.id_treenode_label);
			holder.mCb = (CheckBox) convertView.findViewById(R.id.isselect);
			if (selected.contains(node))
				holder.mCb.setChecked(true);
			else
				holder.mCb.setChecked(false);

			// 发起任务里的标志位
			if (entranceFlag == 1) {
				// 责任人
				if (type == 1) {
					if (lsSelectedPod != null && lsSelectedPod.contains(node)) {
						holder.mCb.setChecked(true);
						if (!selected.contains(node))
							selected.add(node);
					}
					if (lsSelectedReceiver != null && lsSelectedReceiver.contains(node)) {
						holder.mCb.setEnabled(false);
					}
				}
				// 抄送人
				else if (type == 2) {
					if (lsSelectedPod != null && lsSelectedPod.contains(node)) {
						holder.mCb.setEnabled(false);
					}
					if (lsSelectedReceiver != null && lsSelectedReceiver.contains(node)) {
						holder.mCb.setChecked(true);
						if (!selected.contains(node))
							selected.add(node);
					}
				}
			}
			// 发起会议参与者选择
			else if (entranceFlag == 3) {
				if (lsSelectedPod != null && lsSelectedPod.contains(node)) {
					holder.mCb.setEnabled(false);
				}
				if (lsSelectedReceiver != null && lsSelectedReceiver.contains(node)) {
					holder.mCb.setChecked(true);
					if (!selected.contains(node))
						selected.add(node);
				}
			}
			// 发起会议发言人选择
			else if (entranceFlag == 4) {
				if (lsSelectedPod != null && lsSelectedPod.contains(node)) {
					holder.mCb.setChecked(true);
					if (!selected.contains(node))
						selected.add(node);
				}
				if (lsSelectedReceiver != null && lsSelectedReceiver.contains(node)) {
					holder.mCb.setEnabled(false);
				}
			}

			holder.mCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked)
						selected.add(node);
					else
						selected.remove(node);
					if (changed != null)
						changed.onChanged(selected.size());
				}
			});
		} else {
			convertView = mInflater.inflate(R.layout.list_item, parent, false);
			holder = new ViewHolder();
			holder.mIcon = (ImageView) convertView.findViewById(R.id.id_treenode_icon);
			holder.mText = (TextView) convertView.findViewById(R.id.id_treenode_label);
			holder.mPic = (ImageView) convertView.findViewById(R.id.id_treednode_pic);
			holder.mPic.setImageResource(R.drawable.org);
		}
		convertView.setTag(holder);
		// } else {
		// holder = (ViewHolder) convertView.getTag();
		// }

		if (node.getIcon() == -1) {
			holder.mIcon.setVisibility(View.INVISIBLE);
		} else {
			holder.mIcon.setVisibility(View.VISIBLE);
			holder.mIcon.setImageResource(node.getIcon());
		}

		holder.mText.setText(node.getName());

		return convertView;
	}

	private class ViewHolder {
		ImageView mIcon;
		ImageView mPic;
		TextView mText;
		CheckBox mCb;
	}
}
