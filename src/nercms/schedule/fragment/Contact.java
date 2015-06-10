package nercms.schedule.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.adapter.SuperTreeViewAdapter;
import nercms.schedule.adapter.TreeViewAdapter;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.person.OrgInfo;
import android.wxapp.service.model.CustomerModel;
import android.wxapp.service.model.OrgNodeModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author jiaocuina@gmail.com
 * @date 2014-04-14
 * @version V1.0
 * @description 04-16�ύ��κ��������
 */

@SuppressLint("HandlerLeak")
public class Contact extends SherlockFragment {

	private static final String TAG = "ContactFragment";

	// �ؼ����
	ExpandableListView expandableListView;
	// View mContainerView;
	TreeViewAdapter adapter;
	SuperTreeViewAdapter superAdapter;

	private Button enterpriseBtn, personalBtn;

	// ��ȡ�������
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao dao;
	// public ArrayList<OrgNodeModel> orgNodeSecondList;
	// public Map<String, Map<String, ArrayList<StructuredStaffModel>>>
	// bigTreeMap;
	private Handler handler;
	private String userID;

	public String[] groups = { "����ͨѶ¼" };

	public static Contact newInstance() {
		Contact contactsFragment = new Contact();
		return contactsFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.contact_fragment, null);

		personalBtn = (Button) view.findViewById(R.id.btn_contacts_personal);
		enterpriseBtn = (Button) view.findViewById(R.id.btn_contacts_company);
		expandableListView = (ExpandableListView) view.findViewById(R.id.expandablelistview);
		// mContainerView=(View)view.findViewById(R.id.container);
		personalBtn.setOnClickListener(listener);
		enterpriseBtn.setOnClickListener(listener);
		// jiaocuina 0528��Ӳ��� ������selectҳ���contactҳ������adapter
		adapter = new TreeViewAdapter(getActivity(), 38, 1);
		superAdapter = new SuperTreeViewAdapter(getActivity(), null, 1);
		// Ĭ��ѡ�� ��ҵ
		enterpriseBtn.performClick();

		initHandler();

		return view;
	}

	/**
	 * @description �󶨼�����
	 */
	public OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			adapter.removeAll();
			adapter.notifyDataSetChanged();
			superAdapter.RemoveAll();
			superAdapter.notifyDataSetChanged();
			// ���������ϵ��
			if (v == personalBtn) {
				// ��ť����ɫ����
				personalBtn.setBackgroundResource(R.drawable.qb_group_header_righttab_pressed);
				enterpriseBtn.setBackgroundResource(R.drawable.qb_group_header_lefttab_normal);
				initPersonData();
			} else if (v == enterpriseBtn) { // �����ҵ��ϵ��

				personalBtn.setBackgroundResource(R.drawable.qb_group_header_righttab_normal);
				enterpriseBtn.setBackgroundResource(R.drawable.qb_group_header_lefttab_pressed);
				initEnterpriseData();
			}
		}

		private void initPersonData() {
			List<TreeViewAdapter.TreeNode> treeNode = adapter.getTreeNode();
			for (int i = 0; i < groups.length; i++) {
				TreeViewAdapter.TreeNode node = new TreeViewAdapter.TreeNode();
				node.parent = groups[i];
				Cursor cursor = selectPersonalContact(getActivity(),
						android.provider.ContactsContract.Contacts.CONTENT_URI, null);
				Log.d(TAG, "���ر�������ͨѶ¼");
				if (cursor.getCount() == 0) {
					node.childs.add(new StructuredStaffModel("", "", null, null, "������", null, null));
				} else {
					while (cursor.moveToNext()) {
						// ����ͨѶ¼ ��ϵ��ID
						String _id = cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts._ID));
						// ����ͨѶ¼ ��ϵ������
						String _name_ = cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						node.childs.add(new StructuredStaffModel(_id, "P", null, null, _name_, null,
								null));
					}
				}
				treeNode.add(node);
			}
			adapter.updateTreeNode(treeNode);
			expandableListView.setAdapter(adapter);

			// ����Ĭ��չ��
			int groupCount = expandableListView.getCount();
			for (int i = 0; i < groupCount; i++) {
				expandableListView.expandGroup(i);
			}

		}
	};

	private void initEnterpriseData() {

//		userID = MySharedPreference.get(getActivity(), MySharedPreference.USER_ID, "");
//		// ��֯����������׼��
//		adapter.removeAll();
//		adapter.notifyDataSetChanged();
//		superAdapter.RemoveAll();
//		superAdapter.notifyDataSetChanged();
//
//		dao = daoFactory.getPersonDao(getActivity());
//		List<OrgInfo> orgNodeSecondList = dao.getOrgCodeInfosFirst();
//		// bigTreeMap = new HashMap<String, Map<String,
//		// ArrayList<StructuredStaffModel>>>();
//
//		List<SuperTreeViewAdapter.SuperTreeNode> superNodeTree = superAdapter.GetTreeNode();
//
//		// // ��ӿͻ���������ڵ�
//		// OrgNodeModel customerOrgNode = new OrgNodeModel(String.valueOf(20),
//		// "�ͻ�");
//		// orgNodeSecondList.add(customerOrgNode);
//
//		for (int i = 0; i < orgNodeSecondList.size(); i++) {
//			// if (i != orgNodeSecondList.size() - 1) {
//
//			SuperTreeViewAdapter.SuperTreeNode superNode = new SuperTreeViewAdapter.SuperTreeNode();
//			String orgNodeSecondName = orgNodeSecondList.get(i).getD();
//			superNode.parent = orgNodeSecondName;
//
//			List<OrgInfo> children = dao.getOrgCodeInfos(orgNodeSecondList.get(i).getOc());
//			ArrayList<OrgNodeModel> orgNodeThirdList = dao.getThirdOrgNode(orgNodeSecondList.get(i)
//					.getOrgCode());
//
//			for (int j = 0; j < orgNodeThirdList.size(); j++) {
//				String orgNodeThirdName = orgNodeThirdList.get(j).getDescription();
//				String orgNodeThirdCode = orgNodeThirdList.get(j).getOrgCode();
//
//				ArrayList<StructuredStaffModel> ssmList = dao.getSSMFromOrgCode(orgNodeThirdCode);
//				// Ҷ�ӽڵ�
//				TreeViewAdapter.TreeNode node = new TreeViewAdapter.TreeNode();
//				node.parent = orgNodeThirdName;
//				// ���Ⱥ��ڵ�
//				String firstLeafID = "Group" + orgNodeThirdCode;// Ⱥ��ڵ�ID
//				String firstLeaf_name = orgNodeThirdName + "Ⱥ��";
//				StructuredStaffModel firstLeaf = new StructuredStaffModel(firstLeafID, orgNodeThirdCode,
//						orgNodeThirdName, "", firstLeaf_name, "", "");
//				node.childs.add(firstLeaf);
//
//				for (int k = 0; k < ssmList.size(); k++) {
//					node.childs.add(ssmList.get(k));
//				}
//				superNode.childs.add(node);
//			}
//			superNodeTree.add(superNode);
//			// } else {
//			// // 2014-6-19
//			// // �ͻ��ڵ�
//			// SuperTreeViewAdapter.SuperTreeNode customerNode = new
//			// SuperTreeViewAdapter.SuperTreeNode();
//			// customerNode.parent = "�ͻ�";
//			// ArrayList<CustomerModel> customerList =
//			// dao.getCustomersByUserID(userID);
//			// TreeViewAdapter.TreeNode node = new TreeViewAdapter.TreeNode();
//			// node.parent = "�ҵĿͻ�";
//			// CustomerModel customer;
//			// for (int j = 0; j < customerList.size(); j++) {
//			// customer = customerList.get(j);
//			// node.childs.add(new
//			// StructuredStaffModel(customer.getCustomerID(), "C", null, null,
//			// customer.getName(), null, null));
//			// }
//			// customerNode.childs.add(node);
//			// superNodeTree.add(customerNode);
//			// }
//		}
//		superAdapter.UpdateTreeNode(superNodeTree);
//		expandableListView.setAdapter(superAdapter);
	}

	// ���ұ���ͨѶ¼
	public static Cursor selectPersonalContact(Context context, Uri uri, String where) {
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(uri, null, where, null, null);
		return cursor;
	}

	@Override
	public void onResume() {
		super.onResume();
		// enterpriseBtn.performClick();
	}

	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Constant.CREATE_CUSTOMER_REQUEST_SUCCESS:
					Log.i("Customer", "customer�����");
					initEnterpriseData();
					break;
				case Constant.MODIFY_CUSTOMER_REQUEST_SUCCESS:
					Log.i("Customer", "customer���޸�");
					initEnterpriseData();
					break;
				case Constant.DELETE_CUSTOMER_REQUEST_SUCCESS:
					Log.i("Customer", "customer��ɾ��");
					initEnterpriseData();
					break;
				default:
					break;
				}
			}
		};
		MessageHandlerManager.getInstance().register(handler, Constant.CREATE_CUSTOMER_REQUEST_SUCCESS,
				"Main");
		MessageHandlerManager.getInstance().register(handler, Constant.MODIFY_CUSTOMER_REQUEST_SUCCESS,
				"Main");
		MessageHandlerManager.getInstance().register(handler, Constant.DELETE_CUSTOMER_REQUEST_SUCCESS,
				"Main");
	}

	@Override
	public void onDestroyView() {
		// unregister handler

		super.onDestroyView();
	}

}
