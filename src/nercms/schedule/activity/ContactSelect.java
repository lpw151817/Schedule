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
	private String userID;// 本人ID
	// 联系人选择入口：1-发起任务中责任人选择；2-发起会话;3-发起会议参与者选择（多个）；4-发起会议发言人选择（多个）
	private int entranceFlag;

	private int type = -1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_select);
		Log.d(TAG, "进入选择页面");

		listView = (ListView) findViewById(R.id.id_tree);

		userID = MySharedPreference.get(ContactSelect.this, MySharedPreference.USER_ID, null);

		entranceFlag = getIntent().getExtras().getInt("entrance_flag");
		type = getIntent().getExtras().getInt("type");
		initActionBar();
		// 组织机构树数据准备
		dao = daoFactory.getPersonDao(ContactSelect.this);

		// expandableListView1 = (ExpandableListView)
		// findViewById(R.id.expandablelistview1);
		// adapter = new TreeViewAdapter(this, 38, 2);// 添加adapter类型参数
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
		// // 第一层
		// for (int i = 0; i < orgNodeSecondList.size(); i++) {
		// SuperTreeViewAdapter.SuperTreeNode superNode = new
		// SuperTreeViewAdapter.SuperTreeNode();
		// String orgNodeSecondName = orgNodeSecondList.get(i)
		// .getDescription();
		// superNode.parent = orgNodeSecondName;
		// // 第二层
		// ArrayList<OrgNodeModel> orgNodeThirdList = dao
		// .getThirdOrgNode(orgNodeSecondList.get(i).getOrgCode());
		//
		// for (int j = 0; j < orgNodeThirdList.size(); j++) {
		// String orgNodeThirdName = orgNodeThirdList.get(j)
		// .getDescription();
		// String orgNodeThirdCode = orgNodeThirdList.get(j).getOrgCode();
		// ArrayList<StructuredStaffModel> ssmList = dao
		// .getSSMFromOrgCode(orgNodeThirdCode);
		// // 第三层
		// TreeViewAdapter.TreeNode node = new TreeViewAdapter.TreeNode();
		// // 第三层首先添加群组作为第一个叶节点 群组也作为SSM类型
		// node.parent = orgNodeThirdName;
		// //添加群组节点
		// String firstLeafID = "Group" + orgNodeThirdCode ;//群组节点ID "Group1xx"
		// String firstLeaf_name = orgNodeThirdName + "群组";
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
				// select_ok.setTitle("确定(" + check_count + ")");
				// break;
				// case LocalConstant.SELECT_CONTACT_UNCHECKED:
				// check_count--;
				// check_contact_id_list.remove((String[]) msg.obj);
				// select_ok.setTitle("确定(" + check_count + ")");
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

	// actionbar初始化
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("选择联系人");
	}

	@Override
	public void onChanged(int size) {
		select_ok.setTitle("确定(" + size + ")");
	}

	// 右侧按钮
	public boolean onCreateOptionsMenu(Menu menu) {

		// 确定按钮
		select_ok = menu.add(0, 1, 0, "确定");
		select_ok.setTitle("确定(0)");
		select_ok.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	// 将actionbar中的菜单注册
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:// 取消，返回上层
			setResult(RESULT_CANCELED);
			finish();
			break;
		case 1: // 确定按钮
			selectedPerson = adapter.getSelectedDate();

			// 2014-7-31 WeiHao 加入容错
			// String name = "";
			// String id = "";
			if (selectedPerson.size() == 0) {
				Utils.showShortToast(ContactSelect.this, "未选中任何人");
				// 返回上层
				setResult(RESULT_CANCELED);
				finish();
				break;
			} else {
				// 如果选择了人员并点击确定
				Intent intent = null;

				// 嵌套switch分支
				switch (entranceFlag) {

				case 1: // 发起任务责任人选择
					intent = new Intent();
					intent.putExtra("data", (Serializable) selectedPerson);
					intent.putExtra("type", type);
					setResult(RESULT_OK, intent);
					this.finish();
					break;

				case 2: // 发起消息
					intent = new Intent(ContactSelect.this, ChatDetail.class);
					intent.putExtra("entrance_type", 1); // 消息详情界面入口
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
				// case 3: // 发起会议 参与者选择
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
				// case 4: // 发起会议 发言人选择
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
	 * 监听器 点击之后勾选checkbox 再次点击同一子节点，取消勾选 勾选之后，确定按钮中数字相应改变
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
