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
	private TextView orgDesc;// 机构节点名称
	private TextView position;// 职务
	private TextView rank;// 职衔

	private TextView mobile;// 手机
	private TextView email;// 邮箱
	private TextView address;// 地址
	
	private String userID;// 本人ID
	private StructuredStaffModel mySSM;// 登陆用户本人的SSM模型
	private ArrayList<ContactModel> myContactList ;
	public static final String TAG = "Profile";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		userID = MySharedPreference.get(Profile.this,
				MySharedPreference.USER_ID, null);
		
		webRequestManager = new WebRequestManager(AppApplication.getInstance(),
				this);

		// 初始化控件
		initView();
		// 初始化ActionBar
		initActionBar();
		// 初始化部分数据
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
		getSupportActionBar().setTitle("我的资料");
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem modify = menu.add(0, 1, 0, "修改密码");
		modify.setIcon(R.drawable.ic_action_modify);
		modify.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// 左键返回主页
			finish();
			break;
		case 1: // 修改密码
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
		 new AlertDialog.Builder(Profile.this).setTitle("修改密码")
		 .setView(modifyView)
		 .setPositiveButton("确定",new DialogInterface.OnClickListener() { 
	            public void onClick(DialogInterface dialog, int i) { 
	            	String _oldPwd = old_pwd.getText().toString();
	            	String _newPwd = new_pwd.getText().toString();
	                Log.i("TAG", "旧密码"+_oldPwd+"新密码"+_newPwd); 
	                webRequestManager.changePassword(userID, _oldPwd, _newPwd);
	            } 
	        })

		 .setNegativeButton("取消", null).create().show();
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
					Utils.showShortToast(Profile.this, "修改密码成功");
					Profile.this.finish();
					break;
				case Constant.CHANGE_PASSWORD_REQUEST_FAIL:
					Utils.showShortToast(Profile.this, "修改密码失败");
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
