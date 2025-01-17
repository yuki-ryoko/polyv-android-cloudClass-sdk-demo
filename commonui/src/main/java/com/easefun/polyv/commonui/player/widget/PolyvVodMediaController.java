package com.easefun.polyv.commonui.player.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.ScreenUtils;
import com.easefun.polyv.businesssdk.service.PolyvNoLeakHandler;
import com.easefun.polyv.businesssdk.vodplayer.PolyvVodVideoView;
import com.easefun.polyv.commonui.PolyvCommonMediacontroller;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.player.IPolyvBusinessMediaController;
import com.easefun.polyv.commonui.player.PolyvVodVideoHelper;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.utils.PolyvScreenUtils;
import com.easefun.polyv.foundationsdk.utils.PolyvTimeUtils;

public class PolyvVodMediaController extends PolyvCommonMediacontroller<PolyvVodVideoView> implements
        IPolyvBusinessMediaController<PolyvVodVideoView, PolyvVodVideoHelper>, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static final String TAG = PolyvVodMediaController.class.getSimpleName();
    private static final int HIDE = 12;
    private static final int SHOW_PROGRESS = 13;
    // 控制栏是否是显示状态
    private boolean isShowing;
    // 进度条是否处于拖动的状态
    private boolean status_dragging;
    //port controller，land controller
    private ImageView ivPlaypause, ivPlaypauseLand, ivOrientation;
    private TextView tvCurrenttime, tvCurrenttimeLand, tvTotaltime, tvTotaltimeLand;
    private SeekBar sbPlayprogress, sbPlayprogressLand;
    //speed layout
    private RelativeLayout rlSpeed, definitionLayout;
    private LinearLayout speedContainer;
    private Button btSpeedPort, btSpeedLand, bt_speed_10, bt_speed_12, bt_speed_15, bt_speed_20;
    private PolyvVodVideoHelper polyvVodVideoHelper;


    private ImageView pbPptVideoSwitch;
    private ImageView pbSubviewShow;
    private ImageView pbPptVideoSwitchLand;
    private ImageView pbSubviewShowLand;

    private PolyvNoLeakHandler noLeakHandler;
    private int speedPos;
    private View speedChild;

    public PolyvVodMediaController(Context context) {
        this(context, null);
    }

    public PolyvVodMediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvVodMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // 更新显示的播放进度，以及暂停/播放按钮
    private void showProgress() {
        if (isShowing) {
            // 单位：毫秒
            int position = polyvVideoView.getCurrentPosition();
            int totalTime = polyvVideoView.getDuration() / 1000 * 1000;
            if (polyvVideoView.isCompletedState() || position > totalTime)
                position = totalTime;
            int bufPercent = polyvVideoView.getBufferPercentage();
            //在拖动进度条的时候，这里不更新
            if (!status_dragging) {
                tvCurrenttime.setText(PolyvTimeUtils.generateTime(position));
                tvCurrenttimeLand.setText(PolyvTimeUtils.generateTime(position));
                if (totalTime > 0) {
                    sbPlayprogress.setProgress((int) (1000L * position / totalTime));
                    sbPlayprogressLand.setProgress((int) (1000L * position / totalTime));
                } else {
                    sbPlayprogress.setProgress(0);
                    sbPlayprogressLand.setProgress(0);
                }
            }

            sbPlayprogress.setSecondaryProgress(1000 * bufPercent / 100);
            sbPlayprogressLand.setSecondaryProgress(1000 * bufPercent / 100);
            if (polyvVideoView.isPlaying()) {
                ivPlaypause.setSelected(false);
                ivPlaypauseLand.setSelected(false);
            } else {
                ivPlaypause.setSelected(true);
                ivPlaypauseLand.setSelected(true);
            }
            noLeakHandler.sendMessageDelayed(noLeakHandler.obtainMessage(SHOW_PROGRESS), 1000 - (position % 1000));
        }
    }

    @Override
    protected void initialView() {
        initalHandler();
        initView(getContext());
        initialBitrate();
    }

    private void initalHandler() {
        noLeakHandler = new PolyvNoLeakHandler(getContext()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HIDE:
                        hide();
                        break;
                    case SHOW_PROGRESS:
                        showProgress();
                        break;
                }
            }
        };
    }


    private void initView(Context context) {
        if (!(context instanceof Activity))
            throw new RuntimeException("must use activity inflate controller");
        this.context = (Activity) context;
        rootView = LayoutInflater.from(this.context).inflate(R.layout.polyv_controller, this);
        //port controller
        videoControllerPort = findViewById(R.id.rl_port);
        //land controller
        videoControllerLand = findViewById(R.id.rl_land);
        videoControllerLand.setVisibility(View.GONE);

        videoBack = findViewById(R.id.video_back);
        videoBack.setOnClickListener(this);

        ivPlaypause = findViewById(R.id.iv_playpause);
        ivPlaypauseLand = findViewById(R.id.iv_playpause_land);
        ivPlaypause.setOnClickListener(this);
        ivPlaypauseLand.setOnClickListener(this);
        ivOrientation = findViewById(R.id.iv_orientation);
        ivOrientation.setOnClickListener(this);

        tvCurrenttime = findViewById(R.id.tv_currenttime);
        tvCurrenttimeLand = findViewById(R.id.tv_currenttime_land);
        tvTotaltime = findViewById(R.id.tv_totaltime);
        tvTotaltimeLand = findViewById(R.id.tv_totaltime_land);
        sbPlayprogress = findViewById(R.id.sb_playprogress);
        sbPlayprogressLand = findViewById(R.id.sb_playprogress_land);

        sbPlayprogress.setOnSeekBarChangeListener(this);
        sbPlayprogressLand.setOnSeekBarChangeListener(this);


        bitrateChange = findViewById(R.id.bitrate_change);
        pbPptVideoSwitch = findViewById(R.id.pb_ppt_video_switch);
        pbSubviewShow = findViewById(R.id.pb_subview_show);
        bitrateChangeLand = findViewById(R.id.bitrate_change_land);

        pbPptVideoSwitchLand = findViewById(R.id.pb_ppt_video_switch_land);
        pbSubviewShowLand = findViewById(R.id.pb_subview_show_land);

        addListener();
    }

    private void addListener() {
        bitrateChange.setOnClickListener(this);
        bitrateChangeLand.setOnClickListener(this);
        pbPptVideoSwitch.setOnClickListener(this);
        pbPptVideoSwitchLand.setOnClickListener(this);
        pbSubviewShow.setOnClickListener(this);
        pbSubviewShowLand.setOnClickListener(this);
        bitrateChange.setOnClickListener(this);
        bitrateChangeLand.setOnClickListener(this);

    }

    public void addOtherContolLayout(View view) {
        //speed layout
        rlSpeed = view.findViewById(R.id.rl_speed);
        rlSpeed.setOnClickListener(this);
        speedContainer = view.findViewById(R.id.speed_container);
        btSpeedPort = view.findViewById(R.id.bt_speed_port);
        btSpeedLand = findViewById(R.id.bt_speed_land);
        btSpeedPort.setOnClickListener(this);
        btSpeedLand.setOnClickListener(this);
        bt_speed_10 = view.findViewById(R.id.bt_speed_10);
        bt_speed_10.setOnClickListener(this);
        bt_speed_12 = view.findViewById(R.id.bt_speed_12);
        bt_speed_12.setOnClickListener(this);
        bt_speed_15 = view.findViewById(R.id.bt_speed_15);
        bt_speed_15.setOnClickListener(this);
        bt_speed_20 = view.findViewById(R.id.bt_speed_20);
        bt_speed_20.setOnClickListener(this);
    }


    @Override
    public void release() {

    }

    @Override
    public void destroy() {
    }

    @Override
    public void setViewBitRate(String vid, int bitRate) {

    }

    @Override
    public void onPrepared(PolyvVodVideoView mp) {
        int totalTime = polyvVideoView.getDuration();
        tvTotaltime.setText(" / " + PolyvTimeUtils.generateTime(totalTime));
        tvTotaltimeLand.setText(" / " + PolyvTimeUtils.generateTime(totalTime));
        btSpeedPort.setText("1.0x");
        btSpeedLand.setText("1.0x");
        videoControllerPort.setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        if (isShowing) {
            noLeakHandler.removeMessages(HIDE);
            noLeakHandler.removeMessages(SHOW_PROGRESS);
            isShowing = !isShowing;
            setVisibility(View.GONE);

        }
        super.hide();
    }

    //重置控制栏的隐藏时间
    private void resetHideTime(int delayedTime) {
        noLeakHandler.removeMessages(HIDE);
        if (delayedTime >= 0)
            noLeakHandler.sendMessageDelayed(noLeakHandler.obtainMessage(HIDE), delayedTime);
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void setAnchorView(View view) {

    }

    @Deprecated
    @Override
    public void setMediaPlayer(MediaController.MediaPlayerControl player) {

    }

    /**
     * 显示控制栏
     *
     * @param timeout 显示的时间，<0时将一直显示
     */
    @Override
    public void show(int timeout) {
        if (!isShowing) {
            noLeakHandler.removeMessages(SHOW_PROGRESS);
            noLeakHandler.sendEmptyMessage(SHOW_PROGRESS);
            isShowing = !isShowing;
            setVisibility(View.VISIBLE);
        }
        resetHideTime(timeout);
    }

    @Override
    public void show() {
        super.show();
        if (rlSpeed != null && rlSpeed.isShown()) {
            rlSpeed.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void showOnce(View view) {

    }

    //根据视频的播放状态去暂停或播放
    public void playOrPause() {
        if (polyvVideoView.isPlaying()) {
            polyvVideoView.pause();
            ivPlaypause.setSelected(true);
            ivPlaypauseLand.setSelected(true);
        } else {
            polyvVideoView.start();
            ivPlaypause.setSelected(false);
            ivPlaypauseLand.setSelected(false);
        }
    }

    public boolean hideUI(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = rlSpeed.isShown() ? rlSpeed
                    : (definitionLayout.isShown() ? definitionLayout
                    : null);
            if (view != null) {
                int[] lo = new int[2];
                view.getLocationInWindow(lo);
                if (ev.getX() < lo[0] || ev.getY() < lo[1]) {
                    goneWithAnimation(view);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hideUI() {
        View view = rlSpeed.isShown() ? rlSpeed
                : (definitionLayout.isShown() ? definitionLayout
                : null);
        if (view != null) {
            goneWithAnimation(view);
            return true;
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        //module不能用switch-case
        if (id == R.id.iv_playpause || id == R.id.iv_playpause_land) {
            playOrPause();

        } else if (id == R.id.iv_orientation) {
            changeToLandscape();
        } else if (id == R.id.bt_speed_land) {
            hide();
            visibleWithAnimation(rlSpeed);
        } else if (id == R.id.bt_speed_port) {
            hide();
            visibleWithAnimation(rlSpeed);
        } else if (id == R.id.bt_speed_10) {
            speedPos = 0;
            polyvVideoView.setSpeed(1.0f);
            btSpeedPort.setText("1.0x");
            btSpeedLand.setText("1.0x");
            goneWithAnimation(rlSpeed);

        } else if (id == R.id.bt_speed_12) {
            speedPos = 1;
            polyvVideoView.setSpeed(1.2f);
            btSpeedPort.setText("1.25x");
            btSpeedLand.setText("1.25x");
            goneWithAnimation(rlSpeed);

        } else if (id == R.id.bt_speed_15) {
            speedPos = 2;
            polyvVideoView.setSpeed(1.5f);
            btSpeedPort.setText("1.5x");
            btSpeedLand.setText("1.5x");
            goneWithAnimation(rlSpeed);

        } else if (id == R.id.bt_speed_20) {
            speedPos = 3;
            polyvVideoView.setSpeed(2.0f);
            btSpeedPort.setText("2x");
            btSpeedLand.setText("2x");
            goneWithAnimation(rlSpeed);

        } else if (id == R.id.pb_ppt_video_switch || id == R.id.pb_ppt_video_switch_land) {
            changePPTVideoLocation();
        } else if (id == R.id.pb_subview_show || id == R.id.pb_subview_show_land) {
            showSubView();
        } else if (id == R.id.video_back) {
            if (ScreenUtils.isLandscape()) {
                changeToPortrait();
            } else {
                if (context != null) {
                    context.finish();
                }
            }
        } else if (id == R.id.rl_speed) {
            goneWithAnimation(rlSpeed);
        }

    }

    @Override
    public void updatePPTShowStatus(boolean showPPT) {
        pbPptVideoSwitchLand.setVisibility(showPPT ? VISIBLE : INVISIBLE);
        pbPptVideoSwitch.setVisibility(showPPT ? VISIBLE : INVISIBLE);
    }

    private void showSubView() {
        pbPptVideoSwitch.setVisibility(VISIBLE);
        pbPptVideoSwitchLand.setVisibility(VISIBLE);
        pbSubviewShow.setVisibility(GONE);
        pbSubviewShowLand.setVisibility(GONE);
        if (polyvVodVideoHelper != null) {
            polyvVodVideoHelper.showCamerView();
        }
    }

    public void changePPTVideoLocation() {
        if (polyvVodVideoHelper != null) {
            polyvVodVideoHelper.changePPTViewToVideoView(showPPTSubView);
            showPPTSubView = !showPPTSubView;
        }
    }

    @Override
    public void onLongBuffering(String tip) {

    }

    @Override
    public void updateControllerWithCloseSubView() {
        pbPptVideoSwitch.setVisibility(GONE);
        pbPptVideoSwitchLand.setVisibility(GONE);
        pbSubviewShow.setVisibility(VISIBLE);
        pbSubviewShowLand.setVisibility(VISIBLE);
        if (showPPTSubView) {
            pbSubviewShow.setImageResource(R.drawable.ppt);
            pbSubviewShowLand.setImageResource(R.drawable.ppt);
        } else {
            pbSubviewShow.setImageResource(R.drawable.camera);
            pbSubviewShowLand.setImageResource(R.drawable.camera);

        }
    }

    @Override
    public void addHelper(PolyvVodVideoHelper tpqPolyvCommonVideoHelper) {
        polyvVodVideoHelper = tpqPolyvCommonVideoHelper;
    }

    @Override
    public void changeToLandscape() {
        super.changeToLandscape();
        changeSpeedView(false);
    }

    @Override
    public void changeToPortrait() {
        super.changeToPortrait();
        changeSpeedView(true);
    }

    void changeSpeedView(boolean port) {
        if (port) {
            ViewGroup.LayoutParams layoutParams = rlSpeed.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            speedContainer.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            speedContainer.setOrientation(LinearLayout.VERTICAL);
            speedContainer.requestLayout();
            ViewGroup.LayoutParams layoutParams = rlSpeed.getLayoutParams();
            layoutParams.width = PolyvScreenUtils.dip2px(getContext(), 200);
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        PolyvCommonLog.d(TAG, "seekBarChangeListener onProgressChanged");
        if (!fromUser)
            return;
        int i1 = seekBar.getId();
        if (i1 == R.id.sb_playprogress || i1 == R.id.sb_playprogress_land) {
            resetHideTime(SHOW_TIME);
            status_dragging = true;
            int newPosition = (int) (polyvVideoView.getDuration() * (long) progress / 1000);
            tvCurrenttime.setText(PolyvTimeUtils.generateTime(newPosition));
            tvCurrenttimeLand.setText(PolyvTimeUtils.generateTime(newPosition));

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        PolyvCommonLog.d(TAG, "seekBarChangeListener onStartTrackingTouch");
        if (!seekBar.isSelected())
            seekBar.setSelected(true);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        PolyvCommonLog.d(TAG, "seekBarChangeListener onStopTrackingTouch");
        if (seekBar.isSelected())
            seekBar.setSelected(false);
        int i = seekBar.getId();
        if (i == R.id.sb_playprogress || i == R.id.sb_playprogress_land) {
            if (polyvVideoView.isInPlaybackStateEx() && polyvVideoView.isVodPlayMode()) {
                int seekToPosition = (int) (polyvVideoView.getDuration() * (long) seekBar.getProgress() / seekBar.getMax());
                if (!polyvVideoView.isCompletedState()) {
                    polyvVideoView.seekTo(seekToPosition);
                } else if (seekToPosition < polyvVideoView.getDuration()) {
                    polyvVideoView.seekTo(seekToPosition);
                    polyvVideoView.start();
                }
            }
            status_dragging = false;

        }
    }

    @Override
    protected void visibleWithAnimation(View view) {
        super.visibleWithAnimation(view);
        if (speedContainer != null) {
            if (speedChild != null) {
                speedChild.setSelected(false);
            }
            speedChild = speedContainer.getChildAt(speedPos);
            if (speedChild != null) {
                speedChild.setSelected(true);
            }
        }
    }
}
