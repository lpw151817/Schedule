package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.dao.ConferenceDao;
import android.wxapp.service.dao.ConferencePersonDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.jerry.model.conference.ConferenceUpdateQueryResponseItem;
import android.wxapp.service.jerry.model.conference.ConferenceUpdateQueryResponseRids;
import android.wxapp.service.model.ConferenceModel;
import android.wxapp.service.model.ConferencePersonModel;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class MeetingDetail extends BaseActivity {

	private ImageView iconIv;
	private TextView titleTv;// 会议主题
	private TextView sponsorTv;// 会议发起人
	private TextView typeTv;// 会议类型
	private TextView statusTv;
	private TextView speakerTv; // 发言人
	private TextView participatorTv;// 会议参与者
	private TextView timeTv;// 会议时间

	private String conferenceID;
	private PersonDao personDao;
	private ConferenceDao conferenceDao;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_detail);

		conferenceID = getIntent().getExtras().getString("conference_id");

		initView();
		initData();
		initActionBar();
	}

	private void initView() {

		iconIv = (ImageView) findViewById(R.id.meeting_detail_icon_iv);
		titleTv = (TextView) findViewById(R.id.meeting_detail_title_tv);
		sponsorTv = (TextView) findViewById(R.id.meeting_detail_sponsor_tv);
		typeTv = (TextView) findViewById(R.id.meeting_detail_type_tv);
		statusTv = (TextView) findViewById(R.id.meeting_detail_status_tv);
		timeTv = (TextView) findViewById(R.id.meeting_detail_time_tv);
		participatorTv = (TextView) findViewById(R.id.meeting_detail_participator_tv);
		speakerTv = (TextView) findViewById(R.id.meeting_detail_speaker_tv);

	}

	private void initData() {
		personDao = new PersonDao(MeetingDetail.this);
		conferenceDao = new ConferenceDao(MeetingDetail.this);
		ConferenceUpdateQueryResponseItem data = conferenceDao.getConferenceByCid(conferenceID);
		// 发起人姓名
		String sponsorName = personDao.getPersonInfo(data.getSid()).getN();
		sponsorTv.setText(sponsorName);
		// 会议主题
		titleTv.setText(data.getN());

		// 从所有参会人员列表中，提取出发言人列表和参与者（听众）列表
		String tempSpeaker = "";
		String tempListener = "";
		List<ConferenceUpdateQueryResponseRids> rids = data.getRids();
		for (ConferenceUpdateQueryResponseRids item : rids) {
			if (item.getT().equals("1")) {// 发言人
				tempSpeaker += (personDao.getPersonInfo(item.getRid()).getN() + "/");
			} else if (item.getT().equals("2")) {// 收听人
				tempListener += (personDao.getPersonInfo(item.getRid()).getN() + "/");
			}
		}
		speakerTv.setText(tempSpeaker);
		participatorTv.setText(tempListener);
		typeTv.setText("预约会议");
		if (Long.parseLong(data.getCt()) > System.currentTimeMillis()) {
			statusTv.setText("已预约，等待开始");
			iconIv.setImageResource(R.drawable.meeting_clock);
		} else {
			statusTv.setText("已结束");
			iconIv.setImageResource(R.drawable.meeting_over);
		}
		timeTv.setText(Utils.formatDateMs(data.getCt()));

	}

	// 顶部actionbar创建
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("会议详情");
	}

	// 保存按钮点击时 判断输入+数据保存到本地
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// 左键返回主页
			finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
