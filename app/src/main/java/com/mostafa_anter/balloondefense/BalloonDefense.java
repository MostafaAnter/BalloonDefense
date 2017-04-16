package com.mostafa_anter.balloondefense;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class BalloonDefense extends Activity {

	Surface view;
	WakeLock WL;

	InterstitialAd mInterstitialAd;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//ad--------------------------------------------------------------------------CHANGE ME 1 (change advert unit id)
		AdView ad = new AdView(this);
		ad.setAdSize(AdSize.SMART_BANNER);
		ad.setAdUnitId("ca-app-pub-1992576069870831/8376653905");
		RelativeLayout layout = new RelativeLayout(this);

		// ins ad
		mInterstitialAd = new InterstitialAd(this);
		mInterstitialAd.setAdUnitId("ca-app-pub-1992576069870831/6062315905");

		mInterstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				requestNewInterstitial();
			}
		});
		requestNewInterstitial();

		//layout
		view = new Surface(this, this);	
		
		LayoutParams params1 = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		ad.setLayoutParams(params1);
		
		layout.addView(view);
		layout.addView(ad);
		setContentView(layout);
		
		//ad-------------------------------------------------------------------------CHANGE ME 2 (add testing devices if you wish)
		AdRequest request = new AdRequest.Builder().build();
		ad.loadAd(request);
		
		//wake-lock
		PowerManager PM = (PowerManager)getSystemService(Context.POWER_SERVICE);
		WL = PM.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Graphics");
		WL.acquire();
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation();
		if(rotation == 0)
			view.default_lanscape = true;
		if(rotation == 180)
			view.default_lanscape = true;
		if(rotation == 90)
			view.default_lanscape = false;
		if(rotation == 270)
			view.default_lanscape = false;
	}

	private void requestNewInterstitial() {
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
				.build();

		mInterstitialAd.loadAd(adRequest);
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    super.onKeyDown(keyCode, event);
        if(keyCode == KeyEvent.KEYCODE_BACK){
        	view.back();
            return false;
        }

        return false;
    }
	
	protected void onPause(){
		super.onPause();
		view.pause();
		WL.release();
		
	}
	
	protected void onResume(){
		super.onResume();
		view.resume();
		WL.acquire();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		WL.release();
	
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mInterstitialAd.isLoaded()) {
			mInterstitialAd.show();
		}
	}
}