package com.askhmer.lockscreen.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.askhmer.lockscreen.R;
import com.askhmer.lockscreen.adapter.FullScreenImageAdapter;
import com.askhmer.lockscreen.fragment.NativgationDrawerFragment;
import com.askhmer.lockscreen.model.LockScreenBackgroundDto;
import com.askhmer.lockscreen.model.ScreenListener;
import com.askhmer.lockscreen.network.API;
import com.askhmer.lockscreen.network.CheckInternet;
import com.askhmer.lockscreen.network.MySingleton;
import com.askhmer.lockscreen.utils.CPIservice;
import com.askhmer.lockscreen.utils.LockscreenService;
import com.askhmer.lockscreen.utils.LockscreenUtils;
import com.askhmer.lockscreen.utils.MyBroadCastReciever;
import com.askhmer.lockscreen.utils.SharedPreferencesFile;
import com.askhmer.lockscreen.utils.ToggleSwitchButtonByDy;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.thefinestartist.finestwebview.FinestWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LockScreenActivity extends Activity implements
		LockscreenUtils.OnLockStatusChangedListener,ScreenListener {

	// User-interface
	private Button btnUnlock;

	/*nativagation drawer*/
	private LinearLayout rightDrawer;
	private DrawerLayout drawerLayout;

	// Member variables
	private LockscreenUtils mLockscreenUtils;
	private ViewPager imageViewPager;
	private FullScreenImageAdapter fullScreenImageAdapter;
	private MyBroadCastReciever mReceiver;
	private RelativeLayout relativeLayout;
	private int position = 1;
	private SharedPreferencesFile mSharedPref;
	private ToggleSwitchButtonByDy toggleWebsite, toggleVideo, toggleCall, toggleInstall, toggleDefult;

//	private ArrayList<CompanyDto> arrList;

	private ArrayList<LockScreenBackgroundDto> pathFile;
	// Set appropriate flags to make the screen appear over the keyguard
	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(
				WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		this.getWindow().addFlags(
				/*WindowManager.LayoutParams.FLAG_FULLSCREEN
						|*/ WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						);

		super.onAttachedToWindow();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*Fresco.initialize(this);*/
		setContentView(R.layout.activity_lockscreen);
		init();

		mSharedPref = new SharedPreferencesFile(getApplicationContext(),SharedPreferencesFile.FILE_INFORMATION_TEMP);
		relativeLayout = (RelativeLayout) findViewById(R.id.relative_main);

		// unlock screen in case of app get killed by system
		if (getIntent() != null && getIntent().hasExtra("kill")
				&& getIntent().getExtras().getInt("kill") == 1) {
			/*enableKeyguard();*/
			unlockHomeButton();
		} else {

			try {
				// disable keyguard
				/*disableKeyguard();*/

				// lock home button
				lockHomeButton();

				if (mSharedPref.getBooleanSharedPreference(SharedPreferencesFile.SERVICELOCK) == false) {
					// start service for observing intents
					startService(new Intent(this, LockscreenService.class));
				}

				// listen the events get fired during the call
				StateListener phoneStateListener = new StateListener();
				TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
				telephonyManager.listen(phoneStateListener,
						PhoneStateListener.LISTEN_CALL_STATE);

			} catch (Exception e) {
			}

		}

		toggleVideo = (ToggleSwitchButtonByDy) findViewById(R.id.toggle_video);
		toggleCall = (ToggleSwitchButtonByDy) findViewById(R.id.toggle_call);
		toggleWebsite = (ToggleSwitchButtonByDy) findViewById(R.id.toggle_website);
		toggleInstall = (ToggleSwitchButtonByDy) findViewById(R.id.toggle_install);
		toggleDefult = (ToggleSwitchButtonByDy) findViewById(R.id.toggle_defult);

		/*request to server*/
		/*if (new CheckInternet().isConnect(getApplicationContext()) != true) {
			relativeLayout.setBackgroundResource(R.drawable.temp);
		}*/
		/*Thread myThread = null;*/

		/*Runnable runnable = new CountDownRunner();
		myThread= new Thread(runnable);
		myThread.start();*/

		switchButtonDefult();

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		mReceiver = new MyBroadCastReciever(this);
		registerReceiver(mReceiver, filter);
	}

	private void init() {
		mLockscreenUtils = new LockscreenUtils();
		imageViewPager = (ViewPager) findViewById(R.id.view_pager);

		/*set up drawer*/

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		rightDrawer = (LinearLayout) findViewById(R.id.right_drawer);
		/*drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);*/

		LinearLayout btnDrawable = (LinearLayout) findViewById(R.id.btn_drawbler);
		btnDrawable.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerLayout.openDrawer(rightDrawer);
			}
		});

		/*display currrent date*/
		getCurrentDate();
	}

	@Override
	public void displayMessage() {
		/*close drawer*/
		if (drawerLayout.isDrawerOpen(rightDrawer)) {
			drawerLayout.closeDrawer(rightDrawer);
		}
		if (new CheckInternet().isConnect(this)) {
			/*get time*/
			getCurrentDate();

			/*check if bigger then 10 recreate*/
			if (position > 5) {
				position = 1;
				Log.e("response________", position +"");
				fullScreenImageAdapter.delete();
				fullScreenImageAdapter = null;
				for (int i = 1; i <= 2 ; i++) {
					lockScreenRequestServer("On start");
				}
			}else {
				lockScreenRequestServer("message");
			}
			imageViewPager.setCurrentItem(position++, false);
			System.gc();
			Runtime.getRuntime().gc();
		}else {
			Log.e("response", "false no internet");
			this.finish();
		}
	}

	// Handle events of calls and unlock screen if necessary
	private class StateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				unlockHomeButton();
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			}
		}
	};

	// Don't finish Activity on Back press
	@Override
	public void onBackPressed() {
		return;
	}

	// Handle button clicks
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
				|| (keyCode == KeyEvent.KEYCODE_POWER)
				|| (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
				|| (keyCode == KeyEvent.KEYCODE_CAMERA)) {
			return true;
		}
		if ((keyCode == KeyEvent.KEYCODE_HOME)) {

			return false;
		}

		return false;

	}

	// handle the key press events here itself
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
				|| (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
				|| (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
			return false;
		}
		if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

			return false;
		}
		return false;
	}

	// Lock home button
	public void lockHomeButton() {
		mLockscreenUtils.lock(LockScreenActivity.this);
	}

	// Unlock home button and wait for its callback
	public void unlockHomeButton() {
		mLockscreenUtils.unlock();
	}

	// Simply unlock device when home button is successfully unlocked
	@Override
	public void onLockStatusChanged(boolean isLocked) {
		if (!isLocked) {
			unlockDevice();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		unlockHomeButton();
		try {
			unregisterReceiver(mReceiver);
		}catch (NullPointerException e) {

		}
		pathFile.clear();
		if (fullScreenImageAdapter != null) {
			fullScreenImageAdapter.clearData();
		}
		System.gc();
		Runtime.getRuntime().gc();
	}

	@SuppressWarnings("deprecation")
	private void disableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.disableKeyguard();
	}

	@SuppressWarnings("deprecation")
	private void enableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.reenableKeyguard();
	}

	//Simply unlock device by finishing the activity
	private void unlockDevice()
	{
		finish();
	}

	/*public String getShiftOfDate(Calendar cal) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm a");
		String fullDateFormat = dateFormat.format(cal.getTime());
		return fullDateFormat.substring(fullDateFormat.length() - 2);
	}*/

	/*public void doWork() {
		runOnUiThread(new Runnable() {
			public void run() {
				try {
					TextView txtCurrentTime = (TextView) findViewById(R.id.time);
					TextView txtCurrentShift = (TextView) findViewById(R.id.shift);
					TextView txtCurrentDate = (TextView) findViewById(R.id.date);

					SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
					Calendar cal = Calendar.getInstance();

					String time = dateFormat.format(cal.getTime());

					txtCurrentTime.setText(time.substring(0, time.length() - 3));
					txtCurrentShift.setText(getShiftOfDate(cal).toUpperCase());
					txtCurrentDate.setText(new SimpleDateFormat("dd/MM/yy").format(cal.getTime()));

				} catch (Exception e) {
				}
			}
		});
	}*/

	/*class CountDownRunner implements Runnable{
		// @Override
		public void run() {
			while(!Thread.currentThread().isInterrupted()){
				try {
					doWork();
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}catch(Exception e){
				}
			}
		}
	}*/

	private void getCurrentDate() {
		TextView txtCurrentDate = (TextView) findViewById(R.id.date);
		Calendar cal = Calendar.getInstance();
		txtCurrentDate.setText(new SimpleDateFormat("dd/MM/yy").format(cal.getTime()));
	}

	public void lockScreenRequestServer(final String note) {
		if (pathFile == null){
			pathFile = new ArrayList<LockScreenBackgroundDto>();
		}
		final String cashId = mSharedPref.getStringSharedPreference(SharedPreferencesFile.KEY_INFORMATION_TEMP_CASHID);
		if (cashId != null) {
			StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://www.medayi.com/locknet/locknet_api.php",
					new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							Log.e("response", response);
							if (!response.isEmpty()) {
								try {
									JSONObject jsonObj = new JSONObject(response);
									LockScreenBackgroundDto dto = new LockScreenBackgroundDto();
									dto.setuId(jsonObj.getString("uid"));
									dto.setLockBasicPrice(jsonObj.getString("lock_basic_price"));
									dto.setLockViewPrice(jsonObj.getString("lock_view_price"));
									dto.setImageUrl(jsonObj.getString("image"));
									dto.setWebUrl(jsonObj.getString("url"));
									dto.setType(jsonObj.getString("left_type"));
									dto.setGoogleId(jsonObj.getString("google_id"));
									pathFile.add(dto);
									fullScreenImageAdapter.notifyDataSetChanged();
									selectSwitchButton();
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}else {
								Toast.makeText(LockScreenActivity.this, "No data", Toast.LENGTH_SHORT).show();
							}
						}
					}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					switchButtonDefult();
				}
			}){
				@Override
				protected Map<String, String> getParams() throws AuthFailureError {
					Map<String, String> params = new HashMap<>();
					params.put("cash_slide_id", cashId);
					return params;
				}
				@Override
				public void deliverError(VolleyError error) {
					Toast.makeText(LockScreenActivity.this, "Your phone no have internet connection", Toast.LENGTH_LONG).show();
					finish();
					if (error instanceof NoConnectionError) {
						Cache.Entry entry = this.getCacheEntry();
						if(entry != null) {
							Response<String> response = parseNetworkResponse(new NetworkResponse(entry.data, entry.responseHeaders));
							deliverResponse(response.result);
							return;
						}
					}
					super.deliverError(error);
				}
			};
			MySingleton.getInstance(this).addToRequestQueue(stringRequest);
			if (fullScreenImageAdapter == null) {
				fullScreenImageAdapter = new FullScreenImageAdapter(this, pathFile);
				imageViewPager.setAdapter(fullScreenImageAdapter);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		for (int i = 1; i <= 2 ; i++) {
			lockScreenRequestServer("On start");
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}



	public void requestPointToServer(final String sliding, final String keyOfPoint, final String point, final String uId){

		StringRequest stringRequest = new StringRequest(Request.Method.POST, API.REQUESTPOINT,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {

					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		}){
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> params = new HashMap<>();
				params.put("cash_slide_id", mSharedPref.getStringSharedPreference(SharedPreferencesFile.KEY_INFORMATION_TEMP_CASHID));
				params.put("cash_password", mSharedPref.getStringSharedPreference(SharedPreferencesFile.KEY_INFORMATION_TEMP_PASSWORD));
				params.put("token_id", mSharedPref.getStringSharedPreference(SharedPreferencesFile.KEY_INFORMATION_TEMP_TOKEN));
				params.put("sliding", sliding);
				params.put("uid", uId);
				params.put(keyOfPoint, point);
				return params;
			}
		};
		MySingleton.getInstance(this).addToRequestQueue(stringRequest);
	}

	private void requestVideo(String url) {
		StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.e("error_show", response);
						try {
							JSONObject jsonObj = new JSONObject(response);
							if (jsonObj.getString("rst").equals("110")) {
								Intent intent = new Intent(getApplicationContext(),YoutubeVideo.class);
								intent.putExtra("movie_id", jsonObj.getString("movie_id"));
								intent.putExtra("movie_time", jsonObj.getString("movie_time"));
								intent.putExtra("movie_end_url", jsonObj.getString("movie_end_url"));
								intent.putExtra("uid", pathFile.get(imageViewPager.getCurrentItem()).getuId());
								intent.putExtra("lock_view_price", pathFile.get(imageViewPager.getCurrentItem()).getLockViewPrice());
								startActivity(intent);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Toast.makeText(LockScreenActivity.this, "No internet!", Toast.LENGTH_SHORT).show();
			}
		}){
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				Map<String, String> params = new HashMap<>();
				params.put("uid", pathFile.get(imageViewPager.getCurrentItem()).getuId());
				return params;
			}
		};
		MySingleton.getInstance(this).addToRequestQueue(stringRequest);

	}

	public void switchButtonWebsite() {
		toggleCall.setVisibility(View.GONE);
		toggleVideo.setVisibility(View.GONE);
		toggleDefult.setVisibility(View.GONE);
		toggleInstall.setVisibility(View.GONE);
		toggleWebsite.setVisibility(View.VISIBLE);

		toggleWebsite.setOnTriggerListener(new ToggleSwitchButtonByDy.OnTriggerListener() {
			@Override
			public void toggledUp() {
				if (new CheckInternet().isConnect(getApplicationContext()) == true) {
					if (pathFile.size() > 0) {
						String unlockPrice = pathFile.get(imageViewPager.getCurrentItem()).getLockBasicPrice();
						String uId = pathFile.get(imageViewPager.getCurrentItem()).getuId();
						requestPointToServer("right", "lock_basic_price", unlockPrice, uId);
					}
				}
				unlockHomeButton();
			}

			@Override
			public void toggledDown() {
				if (pathFile.size() > 0) {
					String url = pathFile.get(imageViewPager.getCurrentItem()).getWebUrl();
					String uId = pathFile.get(imageViewPager.getCurrentItem()).getuId();
					String urlPrice = pathFile.get(imageViewPager.getCurrentItem()).getLockViewPrice();

					if (!url.isEmpty()) {
						if (new CheckInternet().isConnect(getApplicationContext()) == true) {
							requestPointToServer("left", "lock_view_price", urlPrice, uId);
						}
						/*https://github.com/TheFinestArtist/FinestWebView-Android*/
						try {
							new FinestWebView.Builder(getApplicationContext())
									.statusBarColorRes(R.color.colorPrimaryDark)
									.toolbarColorRes(R.color.colorPrimary)
									.titleColorRes(R.color.finestWhite)
									.urlColorRes(R.color.finestWhite)
									.iconDefaultColorRes(R.color.finestWhite)
									.progressBarColorRes(R.color.finestWhite)
									.stringResRefresh(R.string.m_refresh)
									.stringResShareVia(R.string.m_share)
									.stringResCopyLink(R.string.m_copy)
									.stringResOpenWith(R.string.m_open)
									.menuTextGravity(Gravity.CENTER)
									.toolbarScrollFlags(0)
									.showSwipeRefreshLayout(false)
									.show(url);
						}catch (Exception e){

						}
					} else {
						new SweetAlertDialog(LockScreenActivity.this)
								.setTitleText("Sorry this banner no link!")
								.show();
					}
				} else {
					new SweetAlertDialog(LockScreenActivity.this)
							.setTitleText("Sorry your phone no internet!")
							.show();
				}
			}
		});

		toggleWebsite.setRotation(90.0f);
	}

	public void switchButtonVideo() {
		toggleCall.setVisibility(View.GONE);
		toggleWebsite.setVisibility(View.GONE);
		toggleDefult.setVisibility(View.GONE);
		toggleInstall.setVisibility(View.GONE);
		toggleVideo.setVisibility(View.VISIBLE);

		toggleVideo.setOnTriggerListener(new ToggleSwitchButtonByDy.OnTriggerListener() {
			@Override
			public void toggledUp() {
				if (new CheckInternet().isConnect(getApplicationContext()) == true) {
					if (pathFile.size() > 0) {
						String unlockPrice = pathFile.get(imageViewPager.getCurrentItem()).getLockBasicPrice();
						String uId = pathFile.get(imageViewPager.getCurrentItem()).getuId();
						requestPointToServer("right", "lock_basic_price", unlockPrice, uId);
					}
				}
				unlockHomeButton();
			}

			@Override
			public void toggledDown() {
				if (pathFile.size() > 0) {
					String url = pathFile.get(imageViewPager.getCurrentItem()).getWebUrl();

					if (!url.isEmpty()) {
						requestVideo(url);
					} else {
						new SweetAlertDialog(LockScreenActivity.this)
								.setTitleText("Sorry this banner no link!")
								.show();
					}
				} else {
					new SweetAlertDialog(LockScreenActivity.this)
							.setTitleText("Sorry your phone no internet!")
							.show();
				}
			}
		});

		toggleVideo.setRotation(90.0f);
	}

	public void switchButtonCall() {
		toggleWebsite.setVisibility(View.GONE);
		toggleVideo.setVisibility(View.GONE);
		toggleDefult.setVisibility(View.GONE);
		toggleInstall.setVisibility(View.GONE);
		toggleCall.setVisibility(View.VISIBLE);

		toggleCall.setOnTriggerListener(new ToggleSwitchButtonByDy.OnTriggerListener() {
			@Override
			public void toggledUp() {
				if (new CheckInternet().isConnect(getApplicationContext()) == true) {
					if (pathFile.size() > 0) {
						String unlockPrice = pathFile.get(imageViewPager.getCurrentItem()).getLockBasicPrice();
						String uId = pathFile.get(imageViewPager.getCurrentItem()).getuId();
						requestPointToServer("right", "lock_basic_price", unlockPrice, uId);
					}
				}
				unlockHomeButton();
			}

			@Override
			public void toggledDown() {
				if (pathFile.size() > 0) {
					String url = pathFile.get(imageViewPager.getCurrentItem()).getWebUrl();
					String uId = pathFile.get(imageViewPager.getCurrentItem()).getuId();
					String urlPrice = pathFile.get(imageViewPager.getCurrentItem()).getLockViewPrice();

					if (!url.isEmpty()) {
						if (new CheckInternet().isConnect(getApplicationContext()) == true) {
							requestPointToServer("left", "lock_view_price", urlPrice, uId);
						}
						Uri number = Uri.parse(url);
						Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
						startActivity(callIntent);
					} else {
						new SweetAlertDialog(LockScreenActivity.this)
								.setTitleText("Sorry this banner no link!")
								.show();
					}
				} else {
					new SweetAlertDialog(LockScreenActivity.this)
							.setTitleText("Sorry your phone no internet!")
							.show();
				}
			}
		});

		toggleCall.setRotation(90.0f);
	}

	public void switchButtonInstall() {
		toggleWebsite.setVisibility(View.GONE);
		toggleVideo.setVisibility(View.GONE);
		toggleDefult.setVisibility(View.GONE);
		toggleCall.setVisibility(View.GONE);
		toggleInstall.setVisibility(View.VISIBLE);

		toggleInstall.setOnTriggerListener(new ToggleSwitchButtonByDy.OnTriggerListener() {
			@Override
			public void toggledUp() {
				if (new CheckInternet().isConnect(getApplicationContext()) == true) {
					if (pathFile.size() > 0) {
						String unlockPrice = pathFile.get(imageViewPager.getCurrentItem()).getLockBasicPrice();
						String uId = pathFile.get(imageViewPager.getCurrentItem()).getuId();
						requestPointToServer("right", "lock_basic_price", unlockPrice, uId);
					}
				}
				unlockHomeButton();
			}

			@Override
			public void toggledDown() {
				if (pathFile.size() > 0) {
					String url = pathFile.get(imageViewPager.getCurrentItem()).getWebUrl();
					String uId = pathFile.get(imageViewPager.getCurrentItem()).getuId();
					String googleId = pathFile.get(imageViewPager.getCurrentItem()).getGoogleId();
					String urlPrice = pathFile.get(imageViewPager.getCurrentItem()).getLockViewPrice();

					if (!url.isEmpty()) {
						if (new CheckInternet().isConnect(getApplicationContext()) == true) {
							/*testing*/
							if (appInstalledOrNot(googleId) == false) {
								stopService(new Intent(getApplicationContext(), CPIservice.class));
								Intent intent = new Intent(getApplicationContext(), CPIservice.class);
								intent.putExtra("packageName", googleId);
								intent.putExtra("install_price", urlPrice);
								intent.putExtra("uId", uId);
								startService(intent);
							}

							/*open play store*/
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						}
					} else {
						new SweetAlertDialog(LockScreenActivity.this)
								.setTitleText("Sorry this banner no link!")
								.show();
					}
				} else {
					new SweetAlertDialog(LockScreenActivity.this)
							.setTitleText("Sorry your phone no internet!")
							.show();
				}
			}
		});

		toggleInstall.setRotation(90.0f);
	}

	public void switchButtonDefult() {
		toggleCall.setVisibility(View.GONE);
		toggleVideo.setVisibility(View.GONE);
		toggleWebsite.setVisibility(View.GONE);
		toggleInstall.setVisibility(View.GONE);
		toggleDefult.setVisibility(View.VISIBLE);

		toggleDefult.setOnTriggerListener(new ToggleSwitchButtonByDy.OnTriggerListener() {
			@Override
			public void toggledUp() {
				unlockHomeButton();
			}

			@Override
			public void toggledDown() {
				new SweetAlertDialog(LockScreenActivity.this)
						.setTitleText("Sorry your phone no internet!")
						.show();
			}
		});

		toggleDefult.setRotation(90.0f);
	}

	public void selectSwitchButton() {
		try{
			String type = pathFile.get(imageViewPager.getCurrentItem()).getType();
			if (type.equals("1")) {
				switchButtonWebsite();
			}else if (type.equals("2")) {
				switchButtonVideo();
			}else if (type.equals("3")){
				switchButtonCall();
			}else if (type.equals("4")){
				switchButtonInstall();
			}
		}catch (Exception e) {
			switchButtonWebsite();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
		Runtime.getRuntime().gc();
	}

	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = getPackageManager();
		boolean app_installed;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		}
		catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}
}