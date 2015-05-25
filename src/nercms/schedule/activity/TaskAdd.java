package nercms.schedule.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.dateSelect.NumericWheelAdapter;
import nercms.schedule.dateSelect.OnWheelChangedListener;
import nercms.schedule.dateSelect.WheelView;
import nercms.schedule.layout.RoundAngleImageView;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.model.AffairAttachModel;
import android.wxapp.service.model.AffairModel;
import android.wxapp.service.model.PersonOnDutyModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.thread.ThreadManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.HttpUploadTask;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class TaskAdd extends SherlockActivity {

	private static final String TAG = "TaskAddActivity";

	// 网络请求入口
	private WebRequestManager webRequestManager;
	// 本地数据请求入口
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	// 人员相关数据请求入口
	private PersonDao personDao;
	// 本用户ID
	private String userID;
	// 本用户人员
	private StructuredStaffModel userSSM = null;

	private Handler handler;

	// 文本框控件
	private EditText etTitle; // 任务主题
	private EditText etSponsor; // 发起人
	private EditText etPod; // 责任人
	private EditText etCc; // 抄送人
	private EditText etEndTime; // 截止时间
	private EditText etContent; // 任务内容
	private ImageButton btnPodPicker; // 责任人选取按钮
	private ImageButton btnCCPicker; // 抄送选取按钮

	private LinearLayout attachPickLayout;

	private TextView tvUploadStatus;

	// 时间控件相关
	private ImageButton btn_calendar;
	private Dialog dialog;
	private static int START_YEAR = 2010, END_YEAR = 2020;

	// 附件添加按钮
	private ImageButton btnAttachPicker;
	private AffairModel affairModel;
	private ArrayList<AffairAttachModel> attachmentList;// 附件
	private PersonOnDutyModel personModel;

	// 显示大图对话框
	private Dialog imageDialog;
	// 媒体文件类型
	private final int TYPE_IMAGE = 1;
	private final int TYPE_VIDEO = 2;
	// 图片显示的宽高
	private final int IMG_WIDTH = 130;
	private final int IMG_HEIGHT = 130;
	// 每行显示缩略图数目
	private final int NUMPERROW = 3;

	// 附件缩略图展示Layout
	private LinearLayout showAttachLayout;
	// 界面显示的图片的集合
	private ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();
	// 附件存储路径
	private String imagePath;
	private String videoPath;
	// 附件集
	private ArrayList<TaskAdd.Media> mediaList = new ArrayList<TaskAdd.Media>();
	// 附件编号
	private int mediaIndex = 0;
	// 图片容器
	private nercms.schedule.layout.FixedGridLayout imageContainer;

	// 保存mediaIndex与media缩略图地址的映射
	private HashMap<Integer, String> index_path_Map = new HashMap<Integer, String>();
	// 保存mediaIndex与media缩略图地址的映射
	private HashMap<Integer, String> index_originalPath_Map = new HashMap<Integer, String>();

	// 任务ID，在onCreate()中进行初始化
	private String taskID;

	// 该任务模型
	private AffairModel task;

	// 附件上传、下载异步类
	private HttpUploadTask uploadTask;

	// 附件上传成功计数器
	private int successCounter = 0;

	private String podName = "";
	private int podID = 0; // 默认为零

	// 保存mediaIndex与layout地址的映射
	// private HashMap<Integer, RelativeLayout> index_layout_Map = new
	// HashMap<Integer, RelativeLayout>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_add);

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), this);

		// 准备用户信息
		userID = MySharedPreference.get(TaskAdd.this, MySharedPreference.USER_ID, "");
		personDao = daoFactory.getPersonDao(TaskAdd.this);
		userSSM = personDao.getSSMByID(userID);

		// 初始化ActionBar
		initActionBar();

		// 初始化Handler
		initHandler();

		// 生成任务ID
		taskID = Utils.produceTaskID(userID);

		// 控件初始化
		etTitle = (EditText) findViewById(R.id.task_title);
		etSponsor = (EditText) findViewById(R.id.task_starter);
		etPod = (EditText) findViewById(R.id.task_participator);

		etCc = (EditText) findViewById(R.id.task_participator_cc);
		etEndTime = (EditText) findViewById(R.id.task_deadline);
		etEndTime.setEnabled(false);// 时间不可编辑
		etContent = (EditText) findViewById(R.id.task_content);
		btnPodPicker = (ImageButton) findViewById(R.id.btn_pod_picker);
		btnCCPicker = (ImageButton) findViewById(R.id.btn_cc_picker);
		btn_calendar = (ImageButton) findViewById(R.id.select_time);

		tvUploadStatus = (TextView) findViewById(R.id.upload_status_textview);
		// 发起人显示
		etSponsor.setEnabled(false);
		etSponsor.setText(userSSM.getName());

		btnPodPicker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(TaskAdd.this, ContactSelect.class);
				intent.putExtra("entrance_flag", 1);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, LocalConstant.TASK_POD_SELECT_REQUEST_CODE);
			}
		});

		btn_calendar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateTimePicker();
			}
		});
		// *****************************************

		// attachment = (EditText)findViewById(R.id.task_attachment);

		// 2014-5-16 WeiHao 新增 附件上传按钮
		btnAttachPicker = (ImageButton) findViewById(R.id.select_attach);
		btnAttachPicker.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				initAttachPickBtn();
			}
		});

		// 附件缩略图展示Layout，默认不可见
		showAttachLayout = (LinearLayout) findViewById(R.id.showAttathLayout);
		showAttachLayout.setVisibility(View.GONE);

		// 图片容器初始化
		imageContainer = (nercms.schedule.layout.FixedGridLayout) findViewById(R.id.attachContainer);
		imageContainer.setCellWidth(IMG_WIDTH);
		imageContainer.setCellHeight(IMG_HEIGHT);
		imageContainer.setNumPerRow(NUMPERROW);

		attachPickLayout = (LinearLayout) findViewById(R.id.task_add_attach_pick_ll);
		attachPickLayout.setVisibility(View.VISIBLE);

	}

	// 2014-5-23 WeiHao
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("发起新任务");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// 反馈按钮
		MenuItem save = menu.add(0, 1, 0, "保存");
		save.setIcon(R.drawable.ic_action_save);
		save.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// 判断是否已输入内容
			backDetect();
			break;
		case 1: // 保存（发送）任务
			createTask();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	// 2015-5-23 WeiHao 返回检测方法
	private void backDetect() {
		String title = etTitle.getText().toString();
		String podID = etPod.getText().toString();
		String taskContent = etContent.getText().toString();
		if (!title.isEmpty() && String.valueOf(podID).isEmpty() && taskContent.isEmpty()
				&& mediaIndex == 0) {
			new AlertDialog.Builder(TaskAdd.this).setTitle("确定退出")
					.setMessage("输入的内容或已添加附件将会丢失\n确定退出任务发起?")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 删除附件
							if (mediaIndex != 0) {
								for (int i = 0; i < mediaIndex; i++) {
								}
							}
							TaskAdd.this.finish();
						}
					}).setNegativeButton("取消", null).create().show();
		} else {
			this.finish();
		}
	}

	// 2014-5-19 WeiHao 创建新任务方法
	private void createTask() {

		String title = etTitle.getText().toString();
		int sponsorID = Integer.parseInt(userID);
		PersonOnDutyModel pod = new PersonOnDutyModel(taskID, podID);
		String beginTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System
				.currentTimeMillis()));
		String endTime = etEndTime.getText().toString();
		String taskContent = etContent.getText().toString();

		// 判断是否为空
		if (title.isEmpty() || podID == 0 || taskContent.isEmpty()) {
			new AlertDialog.Builder(TaskAdd.this).setTitle("无法发起新任务").setMessage("请重新检查任务是否填写完整")
					.setPositiveButton("确定", null).create().show();
			return;
		}

		task = new AffairModel(taskID, 1, sponsorID, title, taskContent, beginTime, endTime, null, 1,
				beginTime, Constant.READ, 1);
		// 添加责任人
		task.setPerson(pod);
		// 判断附件
		if (mediaIndex != 0) { // 有附件
			// uploadTask = new HttpUploadTask(tvUploadStatus, this);

			attachmentList = new ArrayList<AffairAttachModel>();
			AffairAttachModel attach;
			TaskAdd.Media media;
			for (int i = 0; i < mediaIndex; i++) {
				media = mediaList.get(i);
				attach = new AffairAttachModel(taskID, media.getMediaType(), media.getMediaName());

				String mediaPath = media.getMediaUrl();// 媒体文件的本地路径，用户附件上传时
				String uploadUrl = LocalConstant.FILE_SERVER_ATTACH_URL;

				// 开启上传
				new HttpUploadTask(tvUploadStatus, this).execute(mediaPath, uploadUrl);

				attachmentList.add(attach);
			}

			task.setAttachments(attachmentList);

		} else { // 无附件
			task.setAttachments(null);
			// 联网发送任务到服务器
			sendAffair(task);
		}
	}

	private void sendAffair(AffairModel task) {
		List<String> ats = new ArrayList<String>();
		List<String> us = new ArrayList<String>();
		for (AffairAttachModel at : attachmentList) {
			ats.add(at.getAttachmentType() + "");
			us.add(at.getAttachmentURL());
		}

		webRequestManager.sendAffair("", this.task.getType() + "", task.getSponsorID() + "",
				task.getDescription(), task.getTitle(), task.getBeginTime(), task.getEndTime(),
				task.getCompleteTime(), task.getLastOperateType() + "", task.getLastOperateTime(), "",
				ats, us, new String[] { task.getPerson().getPersonID() + "" });
	}

	// 2014-5-21 WeiHao
	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.CREATE_AFFAIR_REQUEST_SUCCESS:
					// 任务保存到本地数据库
					ArrayList<AffairModel> affairs = new ArrayList<AffairModel>(1);
					affairs.add(task);
					ThreadManager.getInstance().startSaveAffairThread(affairs, true, TaskAdd.this);
					Utils.showShortToast(TaskAdd.this, "新建任务成功");
					TaskAdd.this.finish();
					break;
				case Constant.CREATE_AFFAIR_REQUEST_FAIL:
					// 新建任务请求失败，显示失败信息
					String failedMsg = (String) msg.obj;
					Utils.showShortToast(TaskAdd.this, "新建任务失败：" + failedMsg + "，重新发送中...");
					sendAffair(task);
					break;
				case Constant.FILE_UPLOAD_SUCCESS:
					successCounter++;
					if (successCounter == mediaIndex) {
						// 附件全部上传成功，联网发送任务到服务器
						sendAffair(task);
						successCounter = 0; // 计数器归零
					}

					break;
				case Constant.FILE_UPLOAD_FAIL:
					successCounter = 0; // 计数器归零
					Utils.showShortToast(TaskAdd.this, "附件上传失败，任务无法发送\n请检查网络连接是否正常");
					break;

				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, Constant.CREATE_AFFAIR_REQUEST_SUCCESS,
				"TaskAdd");
		MessageHandlerManager.getInstance().register(handler, Constant.CREATE_AFFAIR_REQUEST_FAIL,
				"TaskAdd");
		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_FAIL, "TaskAdd");
		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_SUCCESS, "TaskAdd");

	}

	// 2014-5-16 WeiHao 新增 返回处理
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// 缩略图地址
		String thumbnailUri;
		// 媒体文件地址
		String originalUri;

		switch (requestCode) {
		case LocalConstant.CAPTURE_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {

				// 缩略图地址
				thumbnailUri = Utils.getAttachThumbnailDir();
				if (data == null) {
					File file = new File(imagePath);
					if (file.exists())
						originalUri = imagePath;
					else {
						originalUri = "";
					}
				} else {
					originalUri = imagePath;
				}
				// 获取缩略图
				Utils.getThumbnail(originalUri, thumbnailUri);

				// 判断文件是否存在,不存在直接跳过
				File file = new File(thumbnailUri);
				if (!file.exists()) {
					return;
				}

				String imageName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
				mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, imageName, originalUri));
				// 显示所拍照片
				Uri uri = Uri.fromFile(file);
				int mediaID = mediaIndex++;
				loadMedia(imageContainer, mediaID, getThumbnailFromUri(uri), uri, TYPE_IMAGE);
				// 存储mediaId与imageOriginPath的映射
				index_originalPath_Map.put(mediaID, originalUri);
				// 存储mediaId与thumbnailUri的映射
				index_path_Map.put(mediaID, thumbnailUri);

			}

			break;
		case LocalConstant.CAPTURE_VIDEO_REQUEST_CODE:
			originalUri = videoPath;
			// 判断文件是否存在,不存在直接跳过
			File file = new File(originalUri);
			if (!file.exists()) {
				return;
			}

			// 生成视频缩略图
			Bitmap videoThumbnailBitmap = ThumbnailUtils.createVideoThumbnail(originalUri,
					Thumbnails.FULL_SCREEN_KIND);

			String videoName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
			// 将视频添加入媒体文件集合
			mediaList.add(new Media(Utils.MEDIA_TYPE_VIDEO, videoName, originalUri));
			// 显示所录制视频
			Uri uri = Uri.fromFile(file);
			int mediaID = mediaIndex++;
			loadMedia(imageContainer, mediaID, videoThumbnailBitmap, uri, TYPE_VIDEO);
			// 存储mediaID与originPath的映射
			index_originalPath_Map.put(mediaID, originalUri);

			break;
		case LocalConstant.SELECT_IMAGE_REQUEST_CODE:

			thumbnailUri = Utils.getAttachThumbnailDir();

			// 点击了返回键，对象返回为空,跳过
			if (data == null) {
				originalUri = "";
			} else {
				// 获得图片的uri 判断是否需要索引路径
				Uri selectedUri = data.getData();
				originalUri = selectedUri.getPath();

				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedUri, proj, null, null, null);
				// 获得用户选择的图片的索引值
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				// 最后根据索引值获取图片路径
				originalUri = cursor.getString(columnIndex);
				cursor.close();

				if (!(originalUri.endsWith("jpg") || originalUri.endsWith("gif")
						|| originalUri.endsWith("bmp") || originalUri.endsWith("png"))) {
					originalUri = "";
					Utils.showShortToast(TaskAdd.this, "不是图片");
					return;
				}
				imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, taskID, TaskAdd.this);

				// 将图片拷贝到附件目录下
				File fromFile = new File(originalUri);
				File toFile = new File(imagePath);
				Utils.copyFile(fromFile, toFile, true);

				// 生成缩略图
				Utils.getThumbnail(imagePath, thumbnailUri);

				// 判断文件是否存在,不存在直接跳过
				File thumbFile = new File(thumbnailUri);
				if (!thumbFile.exists()) {
					return;
				}

				String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
				mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, imageName, imagePath));
				// 显示所选择照片
				Uri thumbUri = Uri.fromFile(thumbFile);
				int mediaID1 = mediaIndex++;
				loadMedia(imageContainer, mediaID1, getThumbnailFromUri(thumbUri), thumbUri, TYPE_IMAGE);
				// 存储mediaId与imageOriginPath的映射
				index_originalPath_Map.put(mediaID1, originalUri);
				// 存储mediaId与thumbnailUri的映射
				index_path_Map.put(mediaID1, thumbnailUri);
			}

			break;
		// 2014-5-29
		case LocalConstant.TASK_POD_SELECT_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				etPod.setText(data.getStringExtra("selected_name"));

				podID = Integer.parseInt(data.getExtras().getString("selected_id"));
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 附件上传按钮响应事件方法
	 */
	private void initAttachPickBtn() {
		AlertDialog.Builder builder = new AlertDialog.Builder(TaskAdd.this);
		builder.setTitle("选择附件类型").setItems(new String[] { "图库", "拍照", "摄像" },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int which) {
						switch (which) {
						case 0:
							Utils.showShortToast(TaskAdd.this, "图库");
							Intent getAlbum = new Intent();
							// 开启Pictures画面Type设定为image
							getAlbum.setType("image/*");
							getAlbum.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(getAlbum, LocalConstant.SELECT_IMAGE_REQUEST_CODE);
							break;
						case 1:
							Utils.showShortToast(TaskAdd.this, "拍照");
							// 拍照
							Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, taskID,
									TaskAdd.this);
							Uri imageUri = Uri.fromFile(new File(imagePath));
							// 指定照片储存路径
							imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
							startActivityForResult(imageIntent, LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);
							break;
						case 2:
							Utils.showShortToast(TaskAdd.this, "摄像");
							// 摄像
							Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
							videoPath = Utils.produceAttachDir(Utils.MEDIA_TYPE_VIDEO, taskID,
									TaskAdd.this);
							Uri videoUri = Uri.fromFile(new File(videoPath));
							// 指定视频存储路径
							videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
							// 指定视频的时长限制（30s）
							videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30000);
							startActivityForResult(videoIntent, LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);

							break;

						default:
							break;
						}

					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * 加载媒体（图片|视频）
	 * 
	 * @param viewContainer
	 * @param thumbnail
	 * @param uri
	 * @param MediaType
	 */
	public void loadMedia(nercms.schedule.layout.FixedGridLayout viewContainer, int mediaId,
			Bitmap thumbnail, final Uri uri, final int MediaType) {
		// WeiHao 如果附件展示布局不可见，置未可见
		if (showAttachLayout.getVisibility() == View.GONE) {
			showAttachLayout.setVisibility(View.VISIBLE);
		}
		// 将图片缩略图添加到缩略图列表，便于新建完成后回收
		bitmapList.add(thumbnail);
		final ImageView imageView = CreateImgView(this, thumbnail, IMG_WIDTH, IMG_HEIGHT);
		setImageviewListener(uri, imageView, MediaType);

		ImageButton deleteBtn = new ImageButton(this);
		deleteBtn.setBackgroundResource(R.drawable.media_delete);

		final RelativeLayout r1 = WrapImgView(MediaType, imageView, deleteBtn);
		r1.setId(mediaId);

		deleteBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 显示删除对话框
				showDeleteMediaDialog(r1, MediaType);
			}
		});

		// 将图片添加入图片列表
		viewContainer.addView(r1);
	}

	/**
	 * 缩略图控件
	 * 
	 * @param imgview
	 * @param btn
	 * @author WEIHAO
	 * @since 2014-5-17
	 * @return
	 */
	public RelativeLayout WrapImgView(int mediaType, ImageView imgview, ImageButton btn) {
		RelativeLayout rl = new RelativeLayout(this);
		// com.nercms.workoa.layout.FixedGridLayout.LayoutParams params = new
		// com.nercms.workoa.layout.FixedGridLayout.LayoutParams(width,
		// height);
		rl.setLayoutParams(new nercms.schedule.layout.FixedGridLayout.LayoutParams(IMG_WIDTH, IMG_HEIGHT));
		rl.setPadding(2, 2, 2, 2);
		// rl.setBackgroundResource(color.white);
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		// imgview 位于父 View 的顶部，在父 View 中居左
		lp1.topMargin = 15;
		lp1.rightMargin = 15;
		rl.addView(imgview, lp1);

		RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(30, 30);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		// btn1 位于父 View 的顶部，在父 View 中水平居右
		rl.addView(btn, lp2);

		// WeiHao 媒体类型判断，显示水印
		// ...
		// Bitmap wm;
		// ImageView waterMark;
		TextView waterMark = new TextView(this);
		waterMark.setTextSize(12);
		waterMark.setBackgroundColor(getResources().getColor(R.color.darkgrey));
		waterMark.setTextColor(getResources().getColor(R.color.white));
		RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		// waterMark位于父View的左下
		lp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp3.leftMargin = 10;
		lp3.bottomMargin = 25; // 底部间距好像不起作用，暂时未找到原因

		if (mediaType == TYPE_IMAGE) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.image_preview);
			// waterMark = CreateImgView(this, wm, 30, 30);

			waterMark.setText("图片");

			rl.addView(waterMark, lp3);
		} else if (mediaType == TYPE_VIDEO) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.video_play);
			// waterMark = CreateImgView(this, wm, 50, 50);

			waterMark.setText("视频");

			rl.addView(waterMark, lp3);
		}

		return rl;
	}

	/**
	 * 创建ImageView
	 * 
	 * @param context
	 * @param id
	 * @param pic
	 * @param width
	 * @param height
	 * @return
	 */
	public ImageView CreateImgView(Context context, Bitmap pic, int width, int height) {
		// 加载图片的ImageView
		ImageView imageView = new RoundAngleImageView(context);
		// 将图片缩略图加载到ImageView
		imageView.setImageBitmap(pic);
		// // 为图片设置编号
		// imageView.setId(mediaId);
		// 设置图片显示格式
		nercms.schedule.layout.FixedGridLayout.LayoutParams params = new nercms.schedule.layout.FixedGridLayout.LayoutParams(
				width, height);
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		// imageView.setPadding(5, 5, 5, 5);
		// imageView.setBackgroundColor(R.drawable.imageview_background);
		// imageView.setBackgroundResource(R.drawable.imageview_background);
		return imageView;
	}

	/**
	 * 设置相关的监听事件
	 * 
	 * @param uri
	 * @param imageView
	 */
	public void setImageviewListener(final Uri uri, final ImageView imageView, final int MediaType) {
		// 为图片设置触摸事件
		imageView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// 按下时变半透明
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					imageView.setAlpha(50);
				// 抬起时恢复透明度
				else if (event.getAction() == MotionEvent.ACTION_UP)
					imageView.setAlpha(255);
				return false;
			}
		});
		// 为图片设置单击事件
		imageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 单击事件触发后，单击事件失效
				imageView.setEnabled(false);
				switch (MediaType) {
				case TYPE_IMAGE:
					// 点击显示大图
					showImageDialog(imageView, uri);
					break;
				case TYPE_VIDEO:
					// 点击播放视频
					Intent mIntent = new Intent(Intent.ACTION_VIEW);
					mIntent.setDataAndType(uri, "video/mp4");
					startActivity(mIntent);
					break;
				default:
					break;
				}

			}
		});

	}

	/** 显示大图对话框 */
	private void showImageDialog(final ImageView imageView, final Uri uri) {
		// 获取对话框布局并实例化
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.image_dialog, null);
		// 构造对话框
		imageDialog = new Dialog(this, R.style.imageDialog);
		imageDialog.setContentView(view);
		// 添加图片
		ImageView dialogImageView = (ImageView) view.findViewById(R.id.imageImageView);
		// 获取图片
		final Bitmap pic = getBitmapFromUri(uri);
		// 将图片缩略图加载到ImageView
		dialogImageView.setImageBitmap(pic);
		// 为图片设置单击事件
		dialogImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				imageDialog.dismiss();
				imageDialog = null;
				// 缩略图单击事件恢复
				imageView.setEnabled(true);
				// 回收图片
				pic.recycle();
			}
		});
		// 显示对话框
		imageDialog.show();
	}

	/**
	 * 删除图片或视频对话框
	 * 
	 * @param rl
	 *            删除图片或视频所在的布局
	 * @param MediaType
	 *            媒体文件类型 1-图片 2-视频
	 */
	private void showDeleteMediaDialog(final RelativeLayout rl, final int MediaType) {
		AlertDialog.Builder builder = new Builder(this);
		// 设置标题
		builder.setTitle("提示");
		// 设置显示内容
		builder.setMessage("确定删除" + (MediaType == TYPE_IMAGE ? "图片" : "视频") + "？");
		// 设置确定按钮
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (MediaType == TYPE_IMAGE) {
					// 将图片从图片列表中删除
					imageContainer.removeView(rl);
				} else if (MediaType == TYPE_VIDEO) {
					// 将视频从视频列表中删除
					imageContainer.removeView(rl);
				}
				// 将手机中的媒体文件删除
				Integer index = rl.getId();
				// 删除缩略图
				if (index_path_Map.containsKey(index)) {
					String path = index_path_Map.get(index);
					index_path_Map.remove(index);
					Utils.deleteMedia(path);
				}
				// 删除原媒体文件（新）
				if (index_originalPath_Map.containsKey(index)) {
					String path = index_originalPath_Map.get(index);
					index_originalPath_Map.remove(index);
					Utils.deleteMedia(path);
				}

				// WeiHao 媒体文件编号缩减，并判断是否已删空附件，如果已无附件，隐藏该控件
				if (--mediaIndex == 0) {
					showAttachLayout.setVisibility(View.GONE);
				}

				// WeiHao 从媒体列表移除中移除该附件
				mediaList.remove(index);

				// 对话框关闭
				dialog.dismiss();
			}
		});

		// 设置取消按钮
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 对话框关闭
				dialog.dismiss();
				// 缩略图单击事件恢复
				rl.setEnabled(true);
			}
		});
		// 显示对话框
		builder.create().show();
	}

	/** 通过图片的Uri获取图片 */
	public Bitmap getBitmapFromUri(final Uri uri) {
		ContentResolver cr = this.getContentResolver();
		// 图片输入流
		InputStream input = null;
		try {
			input = cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 获取图片缩略图并返回
		return BitmapFactory.decodeStream(input, null, null);
	}

	/** 通过图片的Uri获取图片的缩略图 */
	public Bitmap getThumbnailFromUri(final Uri uri) {
		ContentResolver cr = this.getContentResolver();
		// 图片输入流
		InputStream input = null;
		try {
			input = cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 图片缩略选项
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		// 获取图片缩略图并返回
		return BitmapFactory.decodeStream(input, null, opts);
	}

	// ------------------------------------------------------------------------------------

	// 选择截止时间 对年月日进行判断
	private void showDateTimePicker() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		// 添加大小月月份并将其转换为list,方便之后的判断
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };
		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);
		dialog = new Dialog(this);
		dialog.setTitle("请选择日期与时间");
		// 找到dialog的布局文件
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.date_time_select, null);
		// 年
		final WheelView wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));// 设置"年"的显示数据
		wv_year.setCyclic(true);// 可循环滚动
		wv_year.setLabel("年");// 添加文字
		wv_year.setCurrentItem(year - START_YEAR);// 初始化时显示的数据
		// 月
		final WheelView wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
		wv_month.setLabel("月");
		wv_month.setCurrentItem(month);
		// 日
		final WheelView wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		// 判断大小月及是否闰年,用来确定"日"的数据
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			// 闰年
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
		wv_day.setLabel("日");
		wv_day.setCurrentItem(day - 1);
		// 时
		final WheelView wv_hours = (WheelView) view.findViewById(R.id.hour);
		wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
		wv_hours.setCyclic(true);
		wv_hours.setCurrentItem(hour);
		// 分
		final WheelView wv_mins = (WheelView) view.findViewById(R.id.mins);
		wv_mins.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		wv_mins.setCyclic(true);
		wv_mins.setCurrentItem(minute);
		// 添加"年"监听
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				// 判断大小月及是否闰年,用来确定"日"的数据
				if (list_big.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if ((year_num % 4 == 0 && year_num % 100 != 0) || year_num % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		// 添加"月"监听
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				// 判断大小月及是否闰年,用来确定"日"的数据
				if (list_big.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if (((wv_year.getCurrentItem() + START_YEAR) % 4 == 0 && (wv_year.getCurrentItem() + START_YEAR) % 100 != 0)
							|| (wv_year.getCurrentItem() + START_YEAR) % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		wv_year.addChangingListener(wheelListener_year);
		wv_month.addChangingListener(wheelListener_month);
		// 根据屏幕密度来指定选择器字体的大小
		int textSize = 0;

		textSize = 15;

		wv_day.TEXT_SIZE = textSize;
		wv_hours.TEXT_SIZE = textSize;
		wv_mins.TEXT_SIZE = textSize;
		wv_month.TEXT_SIZE = textSize;
		wv_year.TEXT_SIZE = textSize;

		Button btn_sure = (Button) view.findViewById(R.id.btn_datetime_sure);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_datetime_cancel);
		// 确定按钮
		btn_sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 如果是个数,则显示为"02"的样式
				String parten = "00";
				DecimalFormat decimal = new DecimalFormat(parten);
				// 设置日期的显示
				String _nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")// 创建时间
						.format(new Date(System.currentTimeMillis()));

				String currentSelectTime = (wv_year.getCurrentItem() + START_YEAR) + "-"
						+ decimal.format((wv_month.getCurrentItem() + 1)) + "-"
						+ decimal.format((wv_day.getCurrentItem() + 1)) + " "
						+ decimal.format(wv_hours.getCurrentItem()) + ":"
						+ decimal.format(wv_mins.getCurrentItem()) + ":00";

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date nowTime = new Date();
				Date selectTime = new Date();
				try {
					nowTime = df.parse(_nowTime);
					selectTime = df.parse(currentSelectTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				long mins = (selectTime.getTime() - nowTime.getTime()) / 6000;
				if (mins < 3) {
					Utils.showShortToast(TaskAdd.this, "选择时间小于当前时间，请重新选择");
				} else {
					etEndTime.setText(currentSelectTime);
					dialog.dismiss();
				}
			}
		});
		// 取消
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		// 设置dialog的布局,并显示
		dialog.setContentView(view);
		dialog.show();
	}

	// 2014-5-16 WeiHao 新增
	// 媒体类
	private class Media {
		private int mediaType;
		private String mediaName;
		private String mediaUrl;

		public Media(int mediaType, String mediaName, String mediaUrl) {
			super();
			this.mediaType = mediaType;
			this.mediaName = mediaName;
			this.mediaUrl = mediaUrl;
		}

		public int getMediaType() {
			return mediaType;
		}

		public void setMediaType(int mediaType) {
			this.mediaType = mediaType;
		}

		public String getMediaName() {
			return mediaName;
		}

		public void setMediaName(String mediaName) {
			this.mediaName = mediaName;
		}

		public String getMediaUrl() {
			return mediaUrl;
		}

		public void setMediaUrl(String mediaUrl) {
			this.mediaUrl = mediaUrl;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backDetect();
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		// 注销handler
		MessageHandlerManager.getInstance()
				.unregister(Constant.CREATE_AFFAIR_REQUEST_SUCCESS, "TaskAdd");
		MessageHandlerManager.getInstance().unregister(Constant.CREATE_AFFAIR_REQUEST_FAIL, "TaskAdd");
		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_FAIL, "TaskAdd");
		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_SUCCESS, "TaskAdd");

		System.out.println("TaskAdd OnDestroy");
		// 回收图片内存
		for (int i = 0; i < bitmapList.size(); i++) {
			bitmapList.get(i).recycle();
			if (bitmapList.get(i) != null)
				bitmapList.get(i).recycle();
		}

		super.onDestroy();
	}

}
