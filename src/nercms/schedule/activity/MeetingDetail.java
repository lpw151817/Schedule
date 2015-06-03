package nercms.schedule.activity;

import java.util.ArrayList;

import nercms.schedule.R;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.dao.ConferenceDao;
import android.wxapp.service.dao.ConferencePersonDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
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
	private DAOFactory daoFactory;
	private PersonDao personDao;
	private ConferenceDao conferenceDao;
	private ConferencePersonDao conferencePersonDao;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_detail);

		conferenceID = getIntent().getExtras().getString("conference_id");
		daoFactory = DAOFactory.getInstance();

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
		personDao = daoFactory.getPersonDao(MeetingDetail.this);
		conferenceDao = daoFactory.getConferenceDao(MeetingDetail.this);
		conferencePersonDao = daoFactory
				.getConferencePersonDao(MeetingDetail.this);
		ConferenceModel conference = conferenceDao
				.getConferenceByID(conferenceID);
		ArrayList<ConferencePersonModel> cpList = conferencePersonDao
				.getConferencePersonListByID(conferenceID);

		// 发起人姓名
		String sponsorName = personDao.getPersonNameByID(String
				.valueOf(conference.getSponsorID()));
		sponsorTv.setText(sponsorName);
		// 会议主题
		titleTv.setText(conference.getConferenceName());

		// 2014-8-8
		// 从所有参会人员列表中，提取出发言人列表和参与者（听众）列表
		ArrayList<ConferencePersonModel> speakerList = new ArrayList<ConferencePersonModel>();
		ArrayList<ConferencePersonModel> participatorList = new ArrayList<ConferencePersonModel>();
		for (int i = 0; i < cpList.size(); i++) {
			if (cpList.get(i).getIsSpeaker() == 1) {
				speakerList.add(cpList.get(i));
			} else {
				participatorList.add(cpList.get(i));
			}
		}

		// 发言人姓名字符串
		// 会议发起人默认为第一个发言人
		String speakerNameString = sponsorName;
		if (speakerList != null) {
			String personID;
//			speakerNameString += " ; ";
			for (int i = 0; i < speakerList.size(); i++) {
				personID = String.valueOf(speakerList.get(i).getPersonID());
				if (i != speakerList.size() - 1) {
					speakerNameString += personDao.getPersonNameByID(personID)
							+ " ; ";
				} else {
					speakerNameString += personDao.getPersonNameByID(personID);
				}
			}
		}
		speakerTv.setText(speakerNameString);

		// 参与者姓名字符串
		String participatorNameString = "";
		if (participatorList != null) {
			String personID;
			participatorNameString = "";
			for (int i = 0; i < participatorList.size(); i++) {
				personID = String
						.valueOf(participatorList.get(i).getPersonID());
				if (i != participatorList.size() - 1) { // 非最后一项
					participatorNameString += personDao
							.getPersonNameByID(personID) + " ; ";
				} else { // 最后一项，不显示分号
					participatorNameString += personDao
							.getPersonNameByID(personID);
				}
			}
		}
		participatorTv.setText(participatorNameString);

		if (conference.getType() == 3) { // 预约的会议
			typeTv.setText("预约会议");
			if (conference.getStatus() == 3) {
				statusTv.setText("已预约，等待开始");
			} else if (conference.getStatus() == 2) {
				statusTv.setText("已结束");
			}
			timeTv.setText(conference.getReservationTime());
		} else if (conference.getType() == 1) { // 即时会议
			if (conference.getStatus() == 2) {
				typeTv.setText("已结束");
			}
			timeTv.setText(conference.getStartTime());
		}

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
