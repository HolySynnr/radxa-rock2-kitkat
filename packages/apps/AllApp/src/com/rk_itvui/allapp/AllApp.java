package com.rk_itvui.allapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.security.spec.MGF1ParameterSpec;
import java.util.Collections;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.Intent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.GridView;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.content.ComponentName;
import android.widget.AdapterView;
import android.view.View;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.SharedPreferences;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.media.AudioManager;

import android.content.ActivityNotFoundException;
import android.database.Cursor;
import android.support.v4.view.ViewPager;
import android.widget.Toast;
import android.widget.Button;

public class AllApp extends Activity {
	private static final String TAG = "AllApp:Activity";

	private ViewPager mViewPager;
	private MyAppGridViewAdpter mAppGridViewAdapter;
	private ContentResolver mContentResolver;
	private Handler mViewPageHandler;
	private final int COUNT_PER_PAGE = 15;
	private PageIndicator mPageIndicator;
	private long mCurrentFocusPosition;

	private AlwaysMarqueeTextView mTextView = null;
	private Button mLeftArrow = null;
	private Button mRightArrow = null;
	// private ImageView whiteBorder;
	private int focusposition = -1;// 焦点、点击的位置
	private ScaleAnimEffect animEffect;

	private final static int UPDATA_UI = 0;
	private int TOPBAR_HEIGHT = 50;
	private int ITEM_SPACING = 20;
	private static ArrayList<PackageInformation> mApkInformation = null;

	private final int MAX_APK = 10;

	private SharedPreferences mSaveEditor = null;
	private final static String SHAREDNAME = "AppInfomation";
	private final static String AppCount = "Count";
	private final static String AppName = "AppName";
	private final static String PackageName = "PackageName";
	private final static String VersionCode = "VersionCode";
	private final static String VersionName = "VersionName";
	private final static String ActivityName = "ActivityName";
	private ViewGroup rootView = null;
	
	public static final String APP_LAUNCH_ACTION = "com.rockchip.itvbox.APP_LAUNCH_ACTION";
	public static final String EXTRA_PACKAGE = "package";

	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "==================================Activity:onCreate");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.app_activity);

		rootView = (ViewGroup) findViewById(R.id.app_layout);

		mLeftArrow = (Button) findViewById(R.id.arrow_left);
		mRightArrow = (Button) findViewById(R.id.arrow_right);
		mLeftArrow.setOnClickListener(mOnClickListener);
		mRightArrow.setOnClickListener(mOnClickListener);

		animEffect = new ScaleAnimEffect();

		mApkInformation = new ArrayList<PackageInformation>();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		ScreenInfo.DENSITY = displayMetrics.densityDpi;
		ScreenInfo.WIDTH = displayMetrics.widthPixels;
		ScreenInfo.HEIGHT = displayMetrics.heightPixels;

		new Thread(getApkInfoRunnable).start();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (mAppGridViewAdapter != null) {
			mViewPager.setAdapter(null);
			mAppGridViewAdapter = null;
		}
		super.onDestroy();
	}

	// This snippet hides the system bars.
	@TargetApi(19)
	private void hideSystemUI() {
		// Set the IMMERSIVE flag.
		// Set the content to appear under the system bars so that the content
		// doesn't resize when the system bars hide and show.
		if (Build.VERSION.SDK_INT >= 19) {
			Log.d(TAG, "*********hideSystemUI************");
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
					WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE
							| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
																	// bar
							| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
							| View.SYSTEM_UI_FLAG_IMMERSIVE);
			getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
					new View.OnSystemUiVisibilityChangeListener() {

						@Override
						public void onSystemUiVisibilityChange(int visibility) {
							// TODO Auto-generated method stub
							Log.d(TAG,
									"**********onSystemUiVisibilityChange**********");
							int fullscreenFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
									| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
									| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
									| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide
																			// nav
																			// bar
									| View.SYSTEM_UI_FLAG_FULLSCREEN // hide
																		// status
																		// bar
									| View.SYSTEM_UI_FLAG_IMMERSIVE;
							if (visibility != fullscreenFlags)
								;
							Handler mH = new Handler();
							mH.postDelayed(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									hideSystemUI();
								}
							}, 1000);

						}
					});
		}
	}

	@Override
	public void onResume() {
		Log.d(TAG, "==================================Activity:onResume");
		if (Build.VERSION.SDK_INT >= 19)
			hideSystemUI();
		super.onResume();

		// mAppGridViewAdapter.notifyDataSetChanged();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	private void createGridView() {
		mViewPager = (ViewPager) findViewById(R.id.app_viewpager);
		mPageIndicator = (PageIndicator) findViewById(R.id.page_indicator);
		mAppGridViewAdapter = new MyAppGridViewAdpter(getApplicationContext(),
				this, mApkInformation);
		mAppGridViewAdapter.setCountPerPage(COUNT_PER_PAGE);
		mAppGridViewAdapter.setOnItemClickListener(mOnItemClickListener);
		mViewPager.setAdapter(mAppGridViewAdapter);
		mViewPager.setOnPageChangeListener(mOnPageChangeListener);
		mContentResolver = getContentResolver();
		mViewPageHandler = new Handler();

		mPageIndicator.setVisibility(View.VISIBLE);
		mPageIndicator.setDotCount(mAppGridViewAdapter.getCount());
		mPageIndicator.setActiveDot(0);

		if (1 == mAppGridViewAdapter.getCount()) {
			mLeftArrow.setVisibility(View.GONE);
			mRightArrow.setVisibility(View.GONE);
		}

	}

	void startApplication(int position) {
		try {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			ComponentName componentName = new ComponentName(mApkInformation
					.get(position).getPackageName(), mApkInformation.get(
					position).getActivityName());
			intent.setComponent(componentName);

			Intent startAPPintent = new Intent();
			startAPPintent.setAction(APP_LAUNCH_ACTION);
			startAPPintent.putExtra(EXTRA_PACKAGE, mApkInformation
					.get(position).getPackageName());
			sendBroadcast(startAPPintent);
			Log.d(TAG, "startAPP:"+ mApkInformation
					.get(position).getPackageName());
			
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (SecurityException e) {
			Toast.makeText(this, R.string.activity_not_found,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	// this function is used to filter Apk information. Return true means
	// filter,then the Apk information do not showing in screen
	// return false means the Apk show in screen.
	public boolean filterApk(String packagenName) {
		if ((packagenName.compareTo("com.rockchip.settings") == 0)
				// || (packagenName.compareTo("com.rk.setting") == 0)
				|| (packagenName.compareTo("com.twitter.android") == 0)
				|| (packagenName.compareTo("com.google.android.youtube") == 0)
				// || (packagenName.compareTo("com.android.browser") == 0)
				|| (packagenName.compareTo("com.google.android.apps.books") == 0)
				|| (packagenName.compareTo("com.adobe.flashplayer") == 0)
				// || (packagenName.compareTo("com.android.gallery3d") == 0)
				|| (packagenName
						.compareTo("com.google.android.apps.genie.geniewidget") == 0)
				|| (packagenName.compareTo("com.android.calculator2") == 0)
				|| (packagenName.compareTo("com.android.calendar") == 0)
				|| (packagenName.compareTo("com.android.videoeditor") == 0)
				|| (packagenName.compareTo("com.android.deskclock") == 0)
				|| (packagenName.compareTo("com.android.development") == 0)
				|| (packagenName.compareTo("com.cooliris.media") == 0)
				// || (packagenName.compareTo("com.android.music") == 0)
				|| (packagenName.compareTo("com.android.quicksearchbox") == 0)
				|| (packagenName.compareTo("com.android.camera") == 0)
				|| (packagenName.compareTo("com.android.spare_parts") == 0)
				|| (packagenName.compareTo("com.android.speechrecorder") == 0)
				|| (packagenName.compareTo("com.appside.android.VpadMonitor") == 0)
				|| (packagenName.compareTo("com.rk.youtube") == 0)
				|| (packagenName.compareTo("com.android.contacts") == 0)
				|| (packagenName.compareTo("com.google.android.talk") == 0)
				|| (packagenName.compareTo("com.google.android.apps.maps") == 0)
				|| (packagenName.compareTo("com.rk_itvui.allapp") == 0)
				|| (packagenName.compareTo("com.rk_itvui.rkxbmc") == 0)
				|| (packagenName.compareTo("com.android.soundrecorder") == 0)
		// || (packagenName.compareTo("android.rk.RockVideoPlayer") == 0)
		) {
			return true;
		}

		if (SDKConfig.getIsAndroid40()) {
			if ((packagenName.compareTo("com.rockchip.settings") == 0)) {
				return true;
			}
		}
		if (SDKConfig.getIsAndroid23()) {
			if ((packagenName.compareTo("com.android.settings") == 0)) {
				return true;
			}
		}

		return false;
	}

	public void getLauncherApk() {
		Intent intent = new Intent(Intent.ACTION_MAIN, null);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		PackageManager pm = getPackageManager();

		List<ResolveInfo> resolve = pm.queryIntentActivities(intent, 0);
		Collections.sort(resolve, new ResolveInfo.DisplayNameComparator(pm));

		mApkInformation.clear();

		for (int i = 0; i < resolve.size(); i++) {
			ResolveInfo res = resolve.get(i);

			String packageName = res.activityInfo.packageName;
			if (res.activityInfo.name
					.compareTo("com.android.contacts.DialtactsActivity") == 0)
				continue;

			if (filterApk(packageName))
				continue;

			PackageInformation apkInfor = new PackageInformation();
			apkInfor.setAppName(res.loadLabel(pm).toString());
			apkInfor.setPackageName(packageName);
			apkInfor.setIcon(res.loadIcon(pm));
			apkInfor.setActivityName(res.activityInfo.name);
			mApkInformation.add(apkInfor);
		}
	}

	private Runnable getApkInfoRunnable = new Runnable() {
		public void run() {
			getLauncherApk();

			Message msg = new Message();
			msg.what = UPDATA_UI;
			msg.arg1 = mApkInformation.size();
			mHandler.sendMessage(msg);
		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATA_UI: {
				Log.e("AllApp", "UPDATA_UI");
				createGridView();
			}
				break;
			}
		}
	};

	void openSaveEditor() {
		if (mSaveEditor == null) {
			mSaveEditor = this.getSharedPreferences(SHAREDNAME,
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		}
	}

	private void saveAppCount(String key, int value) {
		openSaveEditor();
		SharedPreferences.Editor editor = mSaveEditor.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	private AppGridViewAdpter.OnItemClickListener mOnItemClickListener = new AppGridViewAdpter.OnItemClickListener() {

		public void onItemClick(ViewPager parent, View view, int position) {
			startApplication(position);
		}
	};

	private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageSelected(int pageindex) {
			// TODO Auto-generated method stub
			mPageIndicator.setActiveDot(pageindex);

			if (0 == pageindex) {
				mLeftArrow.setBackgroundResource(R.drawable.left_icon);
				mRightArrow.setBackgroundResource(R.drawable.right_icon_select);
			} else if ((mAppGridViewAdapter.getCount() - 1) == pageindex) {
				mLeftArrow.setBackgroundResource(R.drawable.left_icon_select);
				mRightArrow.setBackgroundResource(R.drawable.right_icon);
			} else {
				mLeftArrow.setBackgroundResource(R.drawable.left_icon_select);
				mRightArrow.setBackgroundResource(R.drawable.right_icon_select);
			}

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}
	};

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();

			switch (id) {
			case R.id.arrow_left:
				mViewPager.arrowScroll(View.FOCUS_LEFT);
				break;

			case R.id.arrow_right:
				mViewPager.arrowScroll(View.FOCUS_RIGHT);
				break;

			default:
				break;
			}
		}
	};

	private class MyAppGridViewAdpter extends AppGridViewAdpter {
		private final String TAG = "MyCursorPagerAdapter";
		private LayoutInflater mInflater;
		private AllApp mAllApp;

		private int mImageIdIdx;
		private int mImageNameIdx;
		private int randomTemp = -1;

		class ViewHolder {
			LinearLayout[] mLayoutItems = new LinearLayout[COUNT_PER_PAGE];
			LinearLayout[] mBgItems = new LinearLayout[COUNT_PER_PAGE];
			ImageView[] mImageItems = new ImageView[COUNT_PER_PAGE];
			AlwaysMarqueeTextView[] mTextItems = new AlwaysMarqueeTextView[COUNT_PER_PAGE];

		}

		private ViewHolder mView;

		Integer[] mBackground = { R.drawable.appmanager_color0,
				R.drawable.appmanager_color1, R.drawable.appmanager_color2,
				R.drawable.appmanager_color3, R.drawable.appmanager_color4,
				R.drawable.appmanager_color5, R.drawable.appmanager_color6,
				R.drawable.appmanager_color7 };

		class ViewItemHolder {
			int position;
		}

		public void setActivity(AllApp newactivity) {
			mAllApp = newactivity;
		}

		public MyAppGridViewAdpter(Context context, AllApp activity,
				ArrayList<PackageInformation> list) {
			super(context, activity, list);
			// TODO Auto-generated constructor stub
			mInflater = (LayoutInflater) LayoutInflater.from(mContext);
			mAllApp = activity;
		}

		@Override
		public View newView(Context context, ViewGroup parent, int pageindex) {
			// TODO Auto-generated method stub
			ViewHolder vh = new ViewHolder();
			View view = mInflater.inflate(R.layout.item_view, null);
			vh.mLayoutItems[0] = (LinearLayout) view
					.findViewById(R.id.view_item0);
			vh.mLayoutItems[1] = (LinearLayout) view
					.findViewById(R.id.view_item1);
			vh.mLayoutItems[2] = (LinearLayout) view
					.findViewById(R.id.view_item2);
			vh.mLayoutItems[3] = (LinearLayout) view
					.findViewById(R.id.view_item3);
			vh.mLayoutItems[4] = (LinearLayout) view
					.findViewById(R.id.view_item4);
			vh.mLayoutItems[5] = (LinearLayout) view
					.findViewById(R.id.view_item5);
			vh.mLayoutItems[6] = (LinearLayout) view
					.findViewById(R.id.view_item6);
			vh.mLayoutItems[7] = (LinearLayout) view
					.findViewById(R.id.view_item7);
			vh.mLayoutItems[8] = (LinearLayout) view
					.findViewById(R.id.view_item8);
			vh.mLayoutItems[9] = (LinearLayout) view
					.findViewById(R.id.view_item9);
			vh.mLayoutItems[10] = (LinearLayout) view
					.findViewById(R.id.view_item10);
			vh.mLayoutItems[11] = (LinearLayout) view
					.findViewById(R.id.view_item11);
			vh.mLayoutItems[12] = (LinearLayout) view
					.findViewById(R.id.view_item12);
			vh.mLayoutItems[13] = (LinearLayout) view
					.findViewById(R.id.view_item13);
			vh.mLayoutItems[14] = (LinearLayout) view
					.findViewById(R.id.view_item14);

			vh.mBgItems[0] = (LinearLayout) view.findViewById(R.id.view_bg0);
			vh.mBgItems[1] = (LinearLayout) view.findViewById(R.id.view_bg1);
			vh.mBgItems[2] = (LinearLayout) view.findViewById(R.id.view_bg2);
			vh.mBgItems[3] = (LinearLayout) view.findViewById(R.id.view_bg3);
			vh.mBgItems[4] = (LinearLayout) view.findViewById(R.id.view_bg4);
			vh.mBgItems[5] = (LinearLayout) view.findViewById(R.id.view_bg5);
			vh.mBgItems[6] = (LinearLayout) view.findViewById(R.id.view_bg6);
			vh.mBgItems[7] = (LinearLayout) view.findViewById(R.id.view_bg7);
			vh.mBgItems[8] = (LinearLayout) view.findViewById(R.id.view_bg8);
			vh.mBgItems[9] = (LinearLayout) view.findViewById(R.id.view_bg9);
			vh.mBgItems[10] = (LinearLayout) view.findViewById(R.id.view_bg10);
			vh.mBgItems[11] = (LinearLayout) view.findViewById(R.id.view_bg11);
			vh.mBgItems[12] = (LinearLayout) view.findViewById(R.id.view_bg12);
			vh.mBgItems[13] = (LinearLayout) view.findViewById(R.id.view_bg13);
			vh.mBgItems[14] = (LinearLayout) view.findViewById(R.id.view_bg14);

			vh.mImageItems[0] = (ImageView) view
					.findViewById(R.id.item_imageview0);
			vh.mImageItems[1] = (ImageView) view
					.findViewById(R.id.item_imageview1);
			vh.mImageItems[2] = (ImageView) view
					.findViewById(R.id.item_imageview2);
			vh.mImageItems[3] = (ImageView) view
					.findViewById(R.id.item_imageview3);
			vh.mImageItems[4] = (ImageView) view
					.findViewById(R.id.item_imageview4);
			vh.mImageItems[5] = (ImageView) view
					.findViewById(R.id.item_imageview5);
			vh.mImageItems[6] = (ImageView) view
					.findViewById(R.id.item_imageview6);
			vh.mImageItems[7] = (ImageView) view
					.findViewById(R.id.item_imageview7);
			vh.mImageItems[8] = (ImageView) view
					.findViewById(R.id.item_imageview8);
			vh.mImageItems[9] = (ImageView) view
					.findViewById(R.id.item_imageview9);
			vh.mImageItems[10] = (ImageView) view
					.findViewById(R.id.item_imageview10);
			vh.mImageItems[11] = (ImageView) view
					.findViewById(R.id.item_imageview11);
			vh.mImageItems[12] = (ImageView) view
					.findViewById(R.id.item_imageview12);
			vh.mImageItems[13] = (ImageView) view
					.findViewById(R.id.item_imageview13);
			vh.mImageItems[14] = (ImageView) view
					.findViewById(R.id.item_imageview14);

			vh.mTextItems[0] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview0);
			vh.mTextItems[1] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview1);
			vh.mTextItems[2] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview2);
			vh.mTextItems[3] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview3);
			vh.mTextItems[4] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview4);
			vh.mTextItems[5] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview5);
			vh.mTextItems[6] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview6);
			vh.mTextItems[7] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview7);
			vh.mTextItems[8] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview8);
			vh.mTextItems[9] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview9);
			vh.mTextItems[10] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview10);
			vh.mTextItems[11] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview11);
			vh.mTextItems[12] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview12);
			vh.mTextItems[13] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview13);
			vh.mTextItems[14] = (AlwaysMarqueeTextView) view
					.findViewById(R.id.item_textview14);

			for (LinearLayout v : vh.mLayoutItems) {
				v.setOnClickListener(mOnClickListener);
				v.setOnLongClickListener(mOnLongClickListener);

				v.setOnFocusChangeListener(mOnFocusChangeListener);
			}
			view.setTag(vh);
			return view;
		}

		@Override
		public void bindView(View view, Context context, int pageindex) {
			// TODO Auto-generated method stub
			ViewHolder vh = (ViewHolder) view.getTag();
			mView = vh;
			int i = 0;
			final int StartPostition = pageindex * COUNT_PER_PAGE;
			int position = StartPostition + i;
			while (i < COUNT_PER_PAGE && position < getListCount()) {
				ViewItemHolder holder = new ViewItemHolder();
				holder.position = position;
				vh.mLayoutItems[i].setClickable(true);
				vh.mLayoutItems[i].setFocusable(true);
				vh.mLayoutItems[i].setTag(holder);
				if (position == mCurrentFocusPosition) {
					vh.mLayoutItems[i].requestFocus();
				}
				Random rnd = new Random();
				vh.mBgItems[i]
						.setBackgroundResource(mBackground[createRandom(7)]);
				vh.mImageItems[i].setImageDrawable(mApkInformation
						.get(position).getIcon());
				vh.mTextItems[i].setText(mApkInformation.get(position)
						.getAppName());
				vh.mTextItems[i].setTextColor(Color.WHITE);
				;
				i++;
				position = StartPostition + i;
			}
		}

		private int createRandom(int size) {
			Random random = new Random();
			int randomIndex = random.nextInt(size);
			// 如果本次随机与上次一样，重新随机
			while (randomIndex == randomTemp) {
				randomIndex = random.nextInt(size);
			}
			randomTemp = randomIndex;
			return randomIndex;
		}

		private void focusSearch(View view, long position) {
			if (view != null) {
				LinkedList<View> linkList = new LinkedList<View>();
				linkList.offer(view);
				ArrayList<View> hasSearchList = new ArrayList<View>();
				hasSearchList.add(view);
				while (!linkList.isEmpty()) {
					View v = linkList.poll();
					if (v != null) {
						View focus = v.focusSearch(View.FOCUS_LEFT);
						if (focus != null && !hasSearchList.contains(focus)) {
							ViewItemHolder holder = (ViewItemHolder) focus
									.getTag();
							if (holder != null) {
								if (holder.position == position) {
									focus.requestFocus();
									return;
								}
							}
							linkList.offer(focus);
							hasSearchList.add(focus);
						}
						focus = v.focusSearch(View.FOCUS_UP);
						if (focus != null && !hasSearchList.contains(focus)) {
							ViewItemHolder holder = (ViewItemHolder) focus
									.getTag();
							if (holder != null) {
								if (holder.position == position) {
									focus.requestFocus();
									return;
								}
							}
							linkList.offer(focus);
							hasSearchList.add(focus);
						}
						focus = v.focusSearch(View.FOCUS_RIGHT);
						if (focus != null && !hasSearchList.contains(focus)) {
							ViewItemHolder holder = (ViewItemHolder) focus
									.getTag();
							if (holder != null) {
								if (holder.position == position) {
									focus.requestFocus();
									return;
								}
							}
							linkList.offer(focus);
							hasSearchList.add(focus);
						}
						focus = v.focusSearch(View.FOCUS_DOWN);
						if (focus != null && !hasSearchList.contains(focus)) {
							ViewItemHolder holder = (ViewItemHolder) focus
									.getTag();
							if (holder != null) {
								if (holder.position == position) {
									focus.requestFocus();
									return;
								}
							}
							linkList.offer(focus);
							hasSearchList.add(focus);
						}
					}
				}
			}
		}

		private void showOnFocusAnimation(View mFocusView, final int position) {
			mFocusView.bringToFront();
			animEffect.setAttributs(1.0f, 1.10f, 1.0f, 1.10f, 100);
			mFocusView.startAnimation(animEffect.createAnimation());
		}

		private void showLooseFocusAinimation(View mFocusView,
				final int position) {
			animEffect.setAttributs(1.10f, 1.0f, 1.10f, 1.0f, 100);
			mFocusView.startAnimation(animEffect.createAnimation());
			// bgs[position].setVisibility(View.GONE);
		}

		@Override
		protected void onContentChanged() {
			// TODO Auto-generated method stub
			super.onContentChanged();
			Log.d(TAG, "onContentChanged");
			mPageIndicator.setDotCount(mAppGridViewAdapter.getCount());
			mPageIndicator.setActiveDot(mViewPager.getCurrentItem());
			mCurrentFocusPosition = 0;
		}

		private View.OnClickListener mOnClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (v.getTag() != null && mOnItemClickListener != null) {
					ViewItemHolder holder = (ViewItemHolder) v.getTag();
					mOnItemClickListener.onItemClick(null, v, holder.position);
				}
			}
		};

		private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				if (v.getTag() != null && mOnItemLongClickListener != null) {
					ViewItemHolder holder = (ViewItemHolder) v.getTag();
					mOnItemLongClickListener.onItemLongClick(null, v,
							holder.position);
					return true;
				}
				return false;
			}
		};

		private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View mFocusView, boolean hasFocus) {
				// TODO Auto-generated method stub
				switch (mFocusView.getId()) {
				case R.id.view_item0:
					focusposition = 0;
					break;
				case R.id.view_item1:
					focusposition = 1;
					break;
				case R.id.view_item2:
					focusposition = 2;
					break;
				case R.id.view_item3:
					focusposition = 3;
					break;
				case R.id.view_item4:
					focusposition = 4;
					break;
				case R.id.view_item5:
					focusposition = 5;
					break;
				case R.id.view_item6:
					focusposition = 6;
					break;
				case R.id.view_item7:
					focusposition = 7;
					break;
				case R.id.view_item8:
					focusposition = 8;
					break;
				case R.id.view_item9:
					focusposition = 9;
					break;
				case R.id.view_item10:
					focusposition = 10;
					break;
				case R.id.view_item11:
					focusposition = 11;
					break;
				case R.id.view_item12:
					focusposition = 12;
					break;
				case R.id.view_item13:
					focusposition = 13;
					break;
				case R.id.view_item14:
					focusposition = 14;
					break;
				}
				if (hasFocus) {
					ViewItemHolder holder = (ViewItemHolder) mFocusView
							.getTag();
					if (holder != null) {
						int currentPage = (int) (mCurrentFocusPosition / COUNT_PER_PAGE);
						int nextPage = holder.position / COUNT_PER_PAGE;
						if (currentPage == nextPage) {
							mCurrentFocusPosition = holder.position;
						} else {
							if (currentPage > nextPage)
								mCurrentFocusPosition = mCurrentFocusPosition - 11;
							else
								mCurrentFocusPosition = mCurrentFocusPosition + 11;

							if (mCurrentFocusPosition >= mAppGridViewAdapter
									.getListCount()
									|| mCurrentFocusPosition < 0)
								mCurrentFocusPosition = holder.position;

							if (mCurrentFocusPosition != holder.position)
								focusSearch(mFocusView, mCurrentFocusPosition);
						}
					}
					if (mFocusView.isFocused()) {
						showOnFocusAnimation(mFocusView,
								(int) (mCurrentFocusPosition % COUNT_PER_PAGE));
					}

				} else {
					if (focusposition == mCurrentFocusPosition % COUNT_PER_PAGE)
						showLooseFocusAinimation(mFocusView, focusposition);
				}
			}
		};
	}
}
