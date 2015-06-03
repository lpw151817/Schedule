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
	private TextView titleTv;// ��������
	private TextView sponsorTv;// ���鷢����
	private TextView typeTv;// ��������
	private TextView statusTv;
	private TextView speakerTv; // ������
	private TextView participatorTv;// ���������
	private TextView timeTv;// ����ʱ��

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

		// ����������
		String sponsorName = personDao.getPersonNameByID(String
				.valueOf(conference.getSponsorID()));
		sponsorTv.setText(sponsorName);
		// ��������
		titleTv.setText(conference.getConferenceName());

		// 2014-8-8
		// �����вλ���Ա�б��У���ȡ���������б�Ͳ����ߣ����ڣ��б�
		ArrayList<ConferencePersonModel> speakerList = new ArrayList<ConferencePersonModel>();
		ArrayList<ConferencePersonModel> participatorList = new ArrayList<ConferencePersonModel>();
		for (int i = 0; i < cpList.size(); i++) {
			if (cpList.get(i).getIsSpeaker() == 1) {
				speakerList.add(cpList.get(i));
			} else {
				participatorList.add(cpList.get(i));
			}
		}

		// �����������ַ���
		// ���鷢����Ĭ��Ϊ��һ��������
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

		// �����������ַ���
		String participatorNameString = "";
		if (participatorList != null) {
			String personID;
			participatorNameString = "";
			for (int i = 0; i < participatorList.size(); i++) {
				personID = String
						.valueOf(participatorList.get(i).getPersonID());
				if (i != participatorList.size() - 1) { // �����һ��
					participatorNameString += personDao
							.getPersonNameByID(personID) + " ; ";
				} else { // ���һ�����ʾ�ֺ�
					participatorNameString += personDao
							.getPersonNameByID(personID);
				}
			}
		}
		participatorTv.setText(participatorNameString);

		if (conference.getType() == 3) { // ԤԼ�Ļ���
			typeTv.setText("ԤԼ����");
			if (conference.getStatus() == 3) {
				statusTv.setText("��ԤԼ���ȴ���ʼ");
			} else if (conference.getStatus() == 2) {
				statusTv.setText("�ѽ���");
			}
			timeTv.setText(conference.getReservationTime());
		} else if (conference.getType() == 1) { // ��ʱ����
			if (conference.getStatus() == 2) {
				typeTv.setText("�ѽ���");
			}
			timeTv.setText(conference.getStartTime());
		}

	}

	// ����actionbar����
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("��������");
	}

	// ���水ť���ʱ �ж�����+���ݱ��浽����
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// ���������ҳ
			finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
