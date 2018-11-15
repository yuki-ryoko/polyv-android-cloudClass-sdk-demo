package com.easefun.polyv.cloudclassdemo.view;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.easefun.polyv.businesssdk.service.PolyvLoginManager;
import com.easefun.polyv.businesssdk.vodplayer.PolyvVodSDKClient;
import com.easefun.polyv.cloudclass.config.PolyvLiveSDKClient;
import com.easefun.polyv.cloudclassdemo.R;
import com.easefun.polyv.commonui.base.PolyvBaseActivity;
import com.easefun.polyv.commonui.player.widget.PolyvSoftUtilLinearLayout;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;
import com.easefun.polyv.foundationsdk.net.PolyvResponseBean;
import com.easefun.polyv.foundationsdk.net.PolyvrResponseCallback;
import com.easefun.polyv.linkmic.PolyvLinkMicClient;

import java.io.IOException;

import io.reactivex.disposables.Disposable;
import retrofit2.adapter.rxjava2.HttpException;

/**
 * @author df
 * @create 2018/8/27
 * @Describe
 */
public class PolyvCloudClassLoginActivity extends PolyvBaseActivity implements View.OnClickListener {

    // <editor-fold defaultstate="collapsed" desc="成员变量">
    private ImageView loginLogo;
    private TextView loginLogoText;
    private EditText userId;
    private EditText channelId;
    private EditText appId;
    private EditText appSecert;
    private TextView loginTv;
    private PolyvSoftUtilLinearLayout softLayout;
    private LinearLayout playbackLayout, liveLayout;
    private EditText playbackVideoId;
    private EditText playbackAppId, playbackAppSecrect;
    private RelativeLayout liveGroupLayout;
    private RelativeLayout PlaybackGroupLayout;
    private Disposable getTokenDisposable;
    private ProgressDialog progress;

    private static final String TAG = "PolyvCloudClassLoginAct";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polyv_activity_cloudclass_login);
        initialView();

        setTestData();   // for test
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkLoginTvSelected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getTokenDisposable != null) {
            getTokenDisposable.dispose();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private void initialView() {
        initialTopLayout();
        initialLiveVideoView();
        initialPlayBackVideoView();
        intialLogoView();
    }

    private void initialTopLayout() {
        liveGroupLayout = findViewById(R.id.live_group_layout);
        PlaybackGroupLayout = findViewById(R.id.playback_group_layout);

        liveGroupLayout.setOnClickListener(this);
        PlaybackGroupLayout.setOnClickListener(this);

        liveGroupLayout.setSelected(true);
        PlaybackGroupLayout.setSelected(false);

        progress = new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.login_waiting));
        progress.setCanceledOnTouchOutside(false);
        progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (getTokenDisposable != null) {
                    getTokenDisposable.dispose();
                }
                loginTv.setEnabled(true);
            }
        });
    }

    private void initialLiveVideoView() {
        liveLayout = findViewById(R.id.live_layout);
        userId = findViewById(R.id.user_id);
        channelId = findViewById(R.id.channel_id);
        appId = findViewById(R.id.app_id);
        appSecert = findViewById(R.id.app_secert);

        userId.addTextChangedListener(textWatcher);
        channelId.addTextChangedListener(textWatcher);
        appId.addTextChangedListener(textWatcher);
        appSecert.addTextChangedListener(textWatcher);
    }

    private void initialPlayBackVideoView() {
        playbackLayout = findViewById(R.id.playback_layout);
        playbackVideoId = findViewById(R.id.playback_video_id);
        playbackAppId = findViewById(R.id.playback_app_id);
        playbackAppSecrect = findViewById(R.id.playback_app_secert);

        playbackVideoId.addTextChangedListener(textWatcher);
        playbackAppId.addTextChangedListener(textWatcher);
        playbackAppSecrect.addTextChangedListener(textWatcher);
    }

    private void intialLogoView() {
        loginLogo = findViewById(R.id.login_logo);
        loginLogoText = findViewById(R.id.login_logo_text);
        loginTv = findViewById(R.id.login);

        softLayout = findViewById(R.id.polyv_soft_listener_layout);
        softLayout.setOnKeyboardStateChangedListener(new PolyvSoftUtilLinearLayout.IOnKeyboardStateChangedListener() {
            @Override
            public void onKeyboardStateChanged(int state) {
                showTitleLogo(state != PolyvSoftUtilLinearLayout.KEYBOARD_STATE_SHOW);
            }
        });

        loginTv.setOnClickListener(this);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="设置测试数据">
    private void setTestData() {
        appId.setText("");
        appSecert.setText("");
        userId.setText("");
        channelId.setText("");

        playbackVideoId.setText("");
        playbackAppId.setText("");

        loginTv.setSelected(true);
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="onClick方法">
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.login:
                login();
                break;
            case R.id.live_group_layout:
                showLiveGroup();
                break;
            case R.id.playback_group_layout:
                showPlayBackGroup();
                break;
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="View显示控制">
    private void showTitleLogo(boolean showlog) {
        loginLogoText.setVisibility(!showlog ? View.VISIBLE : View.GONE);
        loginLogo.setVisibility(showlog ? View.VISIBLE : View.GONE);
    }

    private void showLiveGroup() {
        liveGroupLayout.setSelected(true);
        PlaybackGroupLayout.setSelected(false);

        liveLayout.setVisibility(View.VISIBLE);
        playbackLayout.setVisibility(View.GONE);

        loginTv.setSelected(//!TextUtils.isEmpty(userId.getText())//&& !TextUtils.isEmpty(appSecert.getText())
                (!TextUtils.isEmpty(channelId.getText())
                        && !TextUtils.isEmpty(appId.getText())));
    }

    private void showPlayBackGroup() {
        liveGroupLayout.setSelected(false);
        PlaybackGroupLayout.setSelected(true);

        liveLayout.setVisibility(View.GONE);
        playbackLayout.setVisibility(View.VISIBLE);

        loginTv.setSelected(!isEmpty(playbackAppId)
                && !isEmpty(playbackVideoId));
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="textWatcher监听">
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkLoginTvSelected();
        }
    };

    private void checkLoginTvSelected() {
        if (liveGroupLayout.isSelected()) {
            loginTv.setSelected(!isEmpty(userId) && !isEmpty(appSecert)&&
                    !isEmpty(channelId)
                            && !isEmpty(appId)
            );
        } else {
            loginTv.setSelected(!isEmpty(playbackVideoId)
                    && !isEmpty(playbackAppId)
            );//&& !isEmpty(playbackAppSecrect)
        }
    }

    private boolean isEmpty(TextView v) {
        return TextUtils.isEmpty(v.getText().toString());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="登录处理">
    private void login() {
        if (!loginTv.isSelected()) {
            return;
        }

        loginTv.setEnabled(false);
        progress.show();
        if (liveGroupLayout.isSelected()) {
            checkToken(userId.getText().toString().trim(),appSecert.getText().toString().trim(),
                    channelId.getText().toString().trim(), null, appId.getText().toString().trim());
        } else {
            checkToken(null,null,null,
                    playbackVideoId.getText().toString().trim(), playbackAppId.getText().toString().trim());
        }
    }

    private void checkToken(String userId,String appSecret,String channel, String vid, String appId) {
        //请求token接口
        getTokenDisposable = PolyvLoginManager.checkLoginToken(userId,appSecret,appId,
                channel, vid,
                new PolyvrResponseCallback<PolyvResponseBean>() {
                    @Override
                    public void onSuccess(PolyvResponseBean responseBean) {
                        PolyvLinkMicClient.getInstance().setAppIdSecret(appId, appSecert.getText().toString());
                        PolyvLiveSDKClient.getInstance().setAppIdSecret(appId, appSecert.getText().toString());
                        PolyvVodSDKClient.getInstance().initConfig(appId, appSecert.getText().toString());
                        if (liveGroupLayout.isSelected()) {
                            PolyvCloudClassHomeActivity.startActivityForLive(PolyvCloudClassLoginActivity.this,
                                    channelId.getText().toString().trim(), userId);
                        } else {
                            PolyvCloudClassHomeActivity.startActivityForPlayBack(PolyvCloudClassLoginActivity.this,
                                    playbackVideoId.getText().toString().trim());
                        }
                        progress.dismiss();
                    }

                    @Override
                    public void onFailure(PolyvResponseBean<PolyvResponseBean> responseBean) {
                        super.onFailure(responseBean);
                        ToastUtils.showLong(responseBean.getMessage());
                        loginTv.setEnabled(true);
                        progress.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);

                        PolyvCommonLog.exception(e);
                        loginTv.setEnabled(true);
                        progress.dismiss();
                        if (e instanceof HttpException) {
                            try {
                                ToastUtils.showLong(((HttpException) e).response().errorBody().string());
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            ToastUtils.showLong(e.getMessage());
                        }
                    }
                });
    }
    // </editor-fold>


}
