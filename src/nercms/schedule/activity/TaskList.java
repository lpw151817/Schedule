package nercms.schedule.activity;

import java.util.ArrayList;

import nercms.schedule.R;
import nercms.schedule.adapter.TaskListViewAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.AffairDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.model.AffairModel;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;


public class TaskList extends BaseActivity {
	
	// 2014-5-23 WeiHao

	// ȫ�ֱ���
	private WebRequestManager webRequestManager;
	private static final String TAG = "TaskListActivity";
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	
	// ����ID
	private String userID;

	// ������ͣ�1-��������2-��������
	private int entranceType = -1;

	// ���״̬�� 1-�����У�δ��ɣ���2-����ɣ�3-���ӳ�
	private int entranceStatus = -1;

	// �������
	private String activityTitle;

	// �ؼ�
	private TextView tvActivityTitle;
	private Button btnBack;
	private ListView mListView;
	private TaskListViewAdapter mAdapter;
	private Handler mHandler;
	private TextView mName, mContent, mParticipator, mDeadline, mReply;

	// ����
	private AffairDao affairdao;
	private ArrayList<AffairModel> mList;// �����б�

	//�ؼ�
	ListView taskListView;
	TaskListViewAdapter taskListViewAdapter;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_list);  

		// ��ʼ���������������
		webRequestManager = new WebRequestManager(AppApplication.getInstance(),
				TaskList.this);

		// ׼���û���Ϣ
		userID = MySharedPreference.get(TaskList.this,
				MySharedPreference.USER_ID, "");

		// ����������洫�����������ͺ�����״̬��ʼ����ڱ���
		entranceType = getIntent().getIntExtra("type", -1);
		entranceStatus = getIntent().getIntExtra("status", -1);

		initActionBar();

		// ��ʼ���ؼ�������
		initViewAndData();

		// ��ʼ���б�������
		initAdapter();


	}
	
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("�����б�");
	}

	private void initViewAndData() {
		// tvActivityTitle = (TextView) findViewById(R.id.tv_activity_title);

		// btnBack = (Button) findViewById(R.id.btn_back);
		// btnBack.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// TaskList.this.finish();
		// }
		// });

		mListView = (ListView) findViewById(R.id.task_list);
		affairdao = daoFactory.getAffairDao(TaskList.this);
		
		activityTitle = "";
		
		// ����׼��
		if (entranceType == 1) { // ��������
			activityTitle = "�������� - ";
			if (entranceStatus == 1) { // �����У�δ��ɣ�
				activityTitle += "������";
				// ��ȡ����
				mList = affairdao.getSendAffairByStatus(userID, 1);
			} else if (entranceStatus == 2) { // �����
				activityTitle += "�����";
				mList = affairdao.getSendAffairByStatus(userID, 2);
			} else if (entranceStatus == 3) { // �ӳ�
				activityTitle += "���ӳ�";
				mList = affairdao.getSendAffairByStatus(userID, 3);
			} else {
				mList = null;
			}
		} else if (entranceType == 2) { // ��������
			activityTitle = "�������� - ";
			if (entranceStatus == 1) { // �����У�δ��ɣ�
				activityTitle += "������";
				mList = affairdao.getReceiveAffairByStatus(userID, 1);
			} else if (entranceStatus == 2) { // �����
				activityTitle += "�����";
				mList = affairdao.getReceiveAffairByStatus(userID, 2);
			} else if (entranceStatus == 3) { // �ӳ�
				activityTitle += "���ӳ�";
				mList = affairdao.getReceiveAffairByStatus(userID, 3);
			} else {
				mList = null;
			}
		}
		getSupportActionBar().setTitle(activityTitle);
	}
	
	private void initAdapter() {
		taskListViewAdapter = new TaskListViewAdapter(mList, TaskList.this,
				entranceType, entranceStatus);
		mListView.setAdapter(taskListViewAdapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		initViewAndData();
		initAdapter();
		super.onResume();
	}
}