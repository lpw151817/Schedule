package nercms.schedule.activity;

import java.util.ArrayList;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.model.ContactModel;
import android.wxapp.service.model.CustomerContactModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class Profile extends SherlockActivity{

	private WebRequestManager webRequestManager;
	
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao = null;
	
	private TextView name;
	private TextView id;
	private TextView orgDesc;// �����ڵ�����
	private TextView position;// ְ��
	private TextView rank;// ְ��

	private TextView mobile;// �ֻ�
	private TextView email;// ����
	private TextView address;// ��ַ
	
	private String userID;// ����ID
	private StructuredStaffModel mySSM;// ��½�û����˵�SSMģ��
	private ArrayList<ContactModel> myContactList ;
	public static final String TAG = "Profile";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		userID = MySharedPreference.get(Profile.this,
				MySharedPreference.USER_ID, null);
		
		webRequestManager = new WebRequestManager(AppApplication.getInstance(),
				this);

		// ��ʼ���ؼ�
		initView();
		// ��ʼ��ActionBar
		initActionBar();
		// ��ʼ����������
		initData();
		
		initHandler();
		
		
	}

	
	private void initView() {
		name = (TextView) findViewById(R.id.my_name);
		id = (TextView) findViewById(R.id.my_id);
		orgDesc = (TextView) findViewById(R.id.my_org);
		position = (TextView) findViewById(R.id.my_position);
		rank = (TextView) findViewById(R.id.my_rank);
		mobile = (TextView) findViewById(R.id.my_phone);
		email = (TextView) findViewById(R.id.my_mail);
		address = (TextView) findViewById(R.id.my_address);		
	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("�ҵ�����");
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem modify = menu.add(0, 1, 0, "�޸�����");
		modify.setIcon(R.drawable.ic_action_modify);
		modify.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// ���������ҳ
			finish();
			break;
		case 1: // �޸�����
			showModifyDialog();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	private void showModifyDialog() {
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		  View modifyView = layoutInflater.inflate(R.layout.modify_pwd, null);
		  final EditText old_pwd =(EditText) modifyView.findViewById(R.id.edit_old_pwd);
		  final EditText new_pwd =(EditText) modifyView.findViewById(R.id.edit_new_pwd);
		 new AlertDialog.Builder(Profile.this).setTitle("�޸�����")
		 .setView(modifyView)
		 .setPositiveButton("ȷ��",new DialogInterface.OnClickListener() { 
	            public void onClick(DialogInterface dialog, int i) { 
	            	String _oldPwd = old_pwd.getText().toString();
	            	String _newPwd = new_pwd.getText().toString();
	                Log.i("TAG", "������"+_oldPwd+"������"+_newPwd); 
	                webRequestManager.changePassword(userID, _oldPwd, _newPwd);
	            } 
	        })

		 .setNegativeButton("ȡ��", null).create().show();
	}

	private void initData() {
		personDao = daoFactory.getPersonDao(Profile.this);	
		mySSM = personDao.getSSMByID(userID);
		id.setText(userID);
		name.setText(mySSM.getName());
		orgDesc.setText(mySSM.getOrgDescription());
		position.setText(mySSM.getPosition());
		rank.setText(mySSM.getRank());
		
		myContactList = personDao.getContactListByID(userID);
		if (myContactList != null) {
			for (int i = 0; i < myContactList.size(); i++) {
				ContactModel cm = myContactList.get(i);
				switch (cm.getType()) {
				case 2:
					String _mobile = cm.getContent();
					mobile.setText(_mobile);
					break;
				case 5:
					String _email = cm.getContent();
					email.setText(_email);
					break;
				case 6:
					String _address = cm.getContent();
					address.setText(_address);
					break;
				default:
					break;
				}
				cm.getType();
			}
		}
	}
	
	private void initHandler() {
		// TODO Auto-generated method stub
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				
				case Constant.CHANGE_PASSWORD_REQUEST_SUCCESS:
					Utils.showShortToast(Profile.this, "�޸�����ɹ�");
					Profile.this.finish();
					break;
				case Constant.CHANGE_PASSWORD_REQUEST_FAIL:
					Utils.showShortToast(Profile.this, "�޸�����ʧ��");
					break;
							}
						}
				};
				MessageHandlerManager.getInstance().register(handler,
						Constant.CHANGE_PASSWORD_REQUEST_SUCCESS, "Profile");
				MessageHandlerManager.getInstance().register(handler,
						Constant.CHANGE_PASSWORD_REQUEST_FAIL, "Profile");
			}
}
