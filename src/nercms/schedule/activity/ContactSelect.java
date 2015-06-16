package nercms.schedule.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.adapter.PersonSelectAdapter;
import nercms.schedule.adapter.PersonSelectAdapter.DataChanged;
import nercms.schedule.adapter.SuperTreeViewAdapter;
import nercms.schedule.adapter.TreeViewAdapter;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.person.Org;
import android.wxapp.service.model.OrgNodeModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.imooc.treeview.utils.Node;

public class ContactSelect extends BaseActivity implements DataChanged {

	private static final String TAG = "ContactSelect";

	// ExpandableListView expandableListView1;
	// TreeViewAdapter adapter;
	// SuperTreeViewAdapter superAdapter;
	// private int check_count = 0;

	ListView listView;
	PersonSelectAdapter<Org> adapter;

	List<Node> selectedPerson;

	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao dao;
	public ArrayList<OrgNodeModel> orgNodeSecondList;
	public Map<String, Map<String, ArrayList<StructuredStaffModel>>> bigTreeMap;
	private Handler handler;
	private ArrayList<String[]> check_contact_id_list = new ArrayList<String[]>(0);
	private MenuItem select_ok;
	private String userID;// ����ID
	// ��ϵ��ѡ����ڣ�1-����������������ѡ��2-����Ự;3-������������ѡ�񣨶������4-������鷢����ѡ�񣨶����
	private int entranceFlag;

	private int type = -1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_select);
		Log.d(TAG, "����ѡ��ҳ��");

		listView = (ListView) findViewById(R.id.id_tree);

		userID = MySharedPreference.get(ContactSelect.this, MySharedPreference.USER_ID, null);

		entranceFlag = getIntent().getExtras().getInt("entrance_flag");
		type = getIntent().getExtras().getInt("type");
		initActionBar();
		// ��֯����������׼��
		dao = daoFactory.getPersonDao(ContactSelect.this);

		// expandableListView1 = (ExpandableListView)
		// findViewById(R.id.expandablelistview1);
		// adapter = new TreeViewAdapter(this, 38, 2);// ���adapter���Ͳ���
		// superAdapter = new SuperTreeViewAdapter(this, stvClickEvent, 2);
		//
		// adapter.removeAll();
		// adapter.notifyDataSetChanged();
		// superAdapter.RemoveAll();
		// superAdapter.notifyDataSetChanged();

		// orgNodeSecondList = dao.getSecondOrgNode();
		// bigTreeMap = new HashMap<String, Map<String,
		// ArrayList<StructuredStaffModel>>>();
		//
		// List<SuperTreeViewAdapter.SuperTreeNode> superNodeTree = superAdapter
		// .GetTreeNode();
		// // ��һ��
		// for (int i = 0; i < orgNodeSecondList.size(); i++) {
		// SuperTreeViewAdapter.SuperTreeNode superNode = new
		// SuperTreeViewAdapter.SuperTreeNode();
		// String orgNodeSecondName = orgNodeSecondList.get(i)
		// .getDescription();
		// superNode.parent = orgNodeSecondName;
		// // �ڶ���
		// ArrayList<OrgNodeModel> orgNodeThirdList = dao
		// .getThirdOrgNode(orgNodeSecondList.get(i).getOrgCode());
		//
		// for (int j = 0; j < orgNodeThirdList.size(); j++) {
		// String orgNodeThirdName = orgNodeThirdList.get(j)
		// .getDescription();
		// String orgNodeThirdCode = orgNodeThirdList.get(j).getOrgCode();
		// ArrayList<StructuredStaffModel> ssmList = dao
		// .getSSMFromOrgCode(orgNodeThirdCode);
		// // ������
		// TreeViewAdapter.TreeNode node = new TreeViewAdapter.TreeNode();
		// // �������������Ⱥ����Ϊ��һ��Ҷ�ڵ� Ⱥ��Ҳ��ΪSSM����
		// node.parent = orgNodeThirdName;
		// //���Ⱥ��ڵ�
		// String firstLeafID = "Group" + orgNodeThirdCode ;//Ⱥ��ڵ�ID "Group1xx"
		// String firstLeaf_name = orgNodeThirdName + "Ⱥ��";
		// StructuredStaffModel firstLeaf = new StructuredStaffModel(
		// firstLeafID, orgNodeThirdCode,orgNodeThirdName, "", firstLeaf_name,
		// "", "");
		// node.childs.add(firstLeaf);
		//
		// for (int k = 0; k < ssmList.size(); k++) {
		// node.childs.add(ssmList.get(k));
		// }
		// superNode.childs.remove(dao.getSSMByID(userID));
		//
		// superNode.childs.add(node);
		// }
		// superNodeTree.add(superNode);
		// }
		// superAdapter.UpdateTreeNode(superNodeTree);
		// expandableListView1.setAdapter(superAdapter);

		try {
			List<Org> data = new ArrayList<Org>();
			data = dao.getOrg2();
			adapter = new PersonSelectAdapter<Org>(listView, this, data, 0);
			listView.setAdapter(adapter);
			adapter.setDataChangedListener(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		initHandler();
	}

	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				// case LocalConstant.SELECT_CONTACT_CHECKED:
				// check_count++;
				// check_contact_id_list.add((String[]) msg.obj);
				// select_ok.setTitle("ȷ��(" + check_count + ")");
				// break;
				// case LocalConstant.SELECT_CONTACT_UNCHECKED:
				// check_count--;
				// check_contact_id_list.remove((String[]) msg.obj);
				// select_ok.setTitle("ȷ��(" + check_count + ")");
				// break;

				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, LocalConstant.SELECT_CONTACT_CHECKED,
				"ContactSelect");
		MessageHandlerManager.getInstance().register(handler, LocalConstant.SELECT_CONTACT_UNCHECKED,
				"ContactSelect");
	}

	// actionbar��ʼ��
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("ѡ����ϵ��");
	}

	@Override
	public void onChanged(int size) {
		select_ok.setTitle("ȷ��(" + size + ")");
	}

	// �Ҳఴť
	public boolean onCreateOptionsMenu(Menu menu) {

		// ȷ����ť
		select_ok = menu.add(0, 1, 0, "ȷ��");
		select_ok.setTitle("ȷ��(0)");
		select_ok.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	// ��actionbar�еĲ˵�ע��
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:// ȡ���������ϲ�
			setResult(RESULT_CANCELED);
			finish();
			break;
		case 1: // ȷ����ť
			selectedPerson = adapter.getSelectedDate();

			// 2014-7-31 WeiHao �����ݴ�
			// String name = "";
			// String id = "";
			if (selectedPerson.size() == 0) {
				Utils.showShortToast(ContactSelect.this, "δѡ���κ���");
				// �����ϲ�
				setResult(RESULT_CANCELED);
				finish();
				break;
			} else {
				// ���ѡ������Ա�����ȷ��
				Intent intent = null;

				// Ƕ��switch��֧
				switch (entranceFlag) {

				case 1: // ��������������ѡ��
					intent = new Intent();
					intent.putExtra("data", (Serializable) selectedPerson);
					intent.putExtra("type", type);
					setResult(RESULT_OK, intent);
					this.finish();
					break;

				case 2: // ������Ϣ
					intent = new Intent(ContactSelect.this, ChatDetail.class);
					intent.putExtra("entrance_type", 1); // ��Ϣ����������
					intent.putExtra("data", (Serializable) selectedPerson);
//					// 2014-7-15 WeiHao
//					if (id.contains("Group")) {
//						intent.putExtra("selected_id", Integer.parseInt(id.substring(5)));
//					} else {
//						intent.putExtra("selected_id", Integer.parseInt(id));
//					}
					startActivity(intent);
					this.finish();
					break;
				// case 3: // ������� ������ѡ��
				// ArrayList<String> idList = new ArrayList<String>();
				// ArrayList<String> nameList = new ArrayList<String>();
				// for (int i = 0; i < check_contact_id_list.size(); i++) {
				// idList.add(check_contact_id_list.get(i)[0]);
				// nameList.add(check_contact_id_list.get(i)[1]);
				// }
				// intent = new Intent();
				// intent.putExtra("selected_name_list", nameList);
				// intent.putExtra("selected_id_list", idList);
				// setResult(RESULT_OK, intent);
				// this.finish();
				// break;
				// // 2014-8-6
				// case 4: // ������� ������ѡ��
				// ArrayList<String> idList_s = new ArrayList<String>();
				// ArrayList<String> nameList_s = new ArrayList<String>();
				// for (int i = 0; i < check_contact_id_list.size(); i++) {
				// idList_s.add(check_contact_id_list.get(i)[0]);
				// nameList_s.add(check_contact_id_list.get(i)[1]);
				// }
				// intent = new Intent();
				// intent.putExtra("selected_name_list", nameList_s);
				// intent.putExtra("selected_id_list", idList_s);
				// setResult(RESULT_OK, intent);
				// this.finish();
				// break;

				default:
					break;
				}
			}

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * ������ ���֮��ѡcheckbox �ٴε��ͬһ�ӽڵ㣬ȡ����ѡ ��ѡ֮��ȷ����ť��������Ӧ�ı�
	 */
	OnChildClickListener stvClickEvent = new OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
				int childPosition, long id) {

			Toast.makeText(ContactSelect.this, groupPosition + ";" + childPosition, Toast.LENGTH_SHORT)
					.show();
			return false;
		}
	};

}
