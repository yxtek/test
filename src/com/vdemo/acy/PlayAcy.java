package com.vdemo.acy;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.utils.Log;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import com.vdemo.R;
import com.vdemo.bean.ScreenBean;
import com.vdemo.utils.LocUtil;

import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;

public class PlayAcy extends Activity {
	/** 当前视频路径 */
	private String path = Environment.getExternalStorageDirectory() + "/1.mp4";
	/** 当前声音 */
	private int mVolume = -1;
	/** 最大音量 */
	private int mMaxVolume;
	/** 当前亮度 */
	private float mBrightness = -1f;
	/** 手势数目 */
	private int finNum=0;
	
	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg;
	private ImageView mOperationPercent;
	private VideoView mVideoView;
	private GestureDetector gestDetector;
	private ScaleGestureDetector scaleDetector;

	private ScreenBean screenBean;
	
	private long mPosition;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (!LibsChecker.checkVitamioLibs(this))
			return;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.acy_play);
		init();
		
		if(null!=icicle){
			
			mPosition = icicle.getLong("pos", 0);
					
			if (mPosition > 0) {
				mVideoView.seekTo(mPosition);
				mPosition = 0;
			}
			mVideoView.start();
		}
	}

	@Override
	protected void onResume() {
		if (mPosition > 0) {
			mVideoView.seekTo(mPosition);
			mPosition = 0;
		}
		super.onResume();
		mVideoView.start();
	}
	
	
	@Override
	protected void onPause() {
		mPosition = mVideoView.getCurrentPosition();
		mVideoView.stopPlayback();
		
		super.onPause();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong("pos", mPosition);
		super.onSaveInstanceState(outState);
	}
	
	private void init() {
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		mOperationPercent = (ImageView) findViewById(R.id.operation_percent);

		mMaxVolume = LocUtil.getMaxVolume(this);
		gestDetector = new GestureDetector(this, new SingleGestureListener());
		scaleDetector = new ScaleGestureDetector(this,
				new MultiGestureListener());

		screenBean = LocUtil.getScreenPix(this);
		
		path = "http://27.221.23.134/youku/6973F6C87CE4883F024B075A3C/0300080100527D560FD5B70297595FFBA6A0D6-FC61-0DBB-441F-DF0FC2697ECC.mp4";
		if (path == "") {
			return;
		} else {
//			mVideoView.setVideoPath(path);
			mVideoView.setVideoURI(Uri.parse(path));
			mVideoView.setMediaController(new MediaController(this));
			mVideoView.requestFocus();

			mVideoView
					.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mediaPlayer) {
							mediaPlayer.setPlaybackSpeed(1.0f);
						}
					});
		}
	}

	/** 定时隐藏 */
	private Handler mDismissHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mVolumeBrightnessLayout.setVisibility(View.GONE);
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		finNum=event.getPointerCount();

		if (1 == finNum) {
			gestDetector.onTouchEvent(event);
			switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				endGesture();
			}
		} else if (2 ==finNum) {
			scaleDetector.onTouchEvent(event);
		}
		return true;
	}

	/** 手势结束 */
	private void endGesture() {
		mVolume = -1;
		mBrightness = -1f;

		// 隐藏
		mDismissHandler.removeMessages(0);
		mDismissHandler.sendEmptyMessageDelayed(0, 500);
	}

	/**
	 * 视频缩放
	 */
	public void changeLayout(int size) {
		mVideoView.setVideoLayout(size, 0);
	}

	/**
	 * 声音大小
	 * 
	 * @param percent
	 */
	public void changeVolume(float percent) {
		if (mVolume == -1) {
			mVolume = LocUtil.getCurVolume(this);
			if (mVolume < 0)
				mVolume = 0;
			// 显示
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		// 变更声音
		LocUtil.setCurVolume(this, index);

		// 变更进度条
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width
				* index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}

	/**
	 * 亮度大小
	 * 
	 * @param percent
	 */
	public void changeBrightness(float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			if (mBrightness < 0.01f)
				mBrightness = 0.01f;
			// 显示
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		WindowManager.LayoutParams lpa = getWindow().getAttributes();
		lpa.screenBrightness = mBrightness + percent;
		if (lpa.screenBrightness > 1.0f)
			lpa.screenBrightness = 1.0f;
		else if (lpa.screenBrightness < 0.01f)
			lpa.screenBrightness = 0.01f;
		getWindow().setAttributes(lpa);

		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}

	/**
	 * 单点触屏
	 * 
	 * @author jin
	 * 
	 */
	private class SingleGestureListener implements
			android.view.GestureDetector.OnGestureListener {

		@Override
		public boolean onDown(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			Log.d("Fling", velocityY);
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			// TODO Auto-generated method stub
			if(2==finNum){
				return false;
			}
			
			float moldX = e1.getX();
			float moldY = e1.getY();
			float y = e2.getY();
			if (moldX > screenBean.getsWidth() * 9.0 / 10)// 右边滑动
				changeVolume((moldY - y) / screenBean.getsHeight());
			else if (moldX < screenBean.getsWidth() / 10.0)// 左边滑动
				changeBrightness((moldY - y) / screenBean.getsHeight());
			return false;
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	/**
	 * 多点缩放
	 * 
	 * @author jin
	 * 
	 */
	private class MultiGestureListener implements OnScaleGestureListener {

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			// 返回true ，才能进入onscale()函数
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			// TODO Auto-generated method stub
			float oldDis = detector.getPreviousSpan();
			float curDis = detector.getCurrentSpan();
			if (oldDis - curDis > 50) {
				// 缩小
				changeLayout(0);
				Toast.makeText(PlayAcy.this, "缩小", 1000).show();
			} else if (oldDis - curDis < -50) {
				// 放大
				changeLayout(1);
				Toast.makeText(PlayAcy.this, "放大", 1000).show();
			}
		}

	}
}
