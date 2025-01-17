package com.easefun.polyv.commonui.player.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.easefun.polyv.commonui.R;


public class PolyvLightTipsView extends FrameLayout {
    //lightView
    private View view;
    private TextView tv_percent;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == View.GONE)
                setVisibility(View.GONE);
        }
    };

    public PolyvLightTipsView(Context context) {
        this(context, null);
    }

    public PolyvLightTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvLightTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.view = LayoutInflater.from(context).inflate(R.layout.polyv_tips_view_light, this);
        initView();
    }

    private void initView() {
        hide();
        tv_percent = view.findViewById(R.id.tv_percent);
    }

    public void hide() {
        setVisibility(View.GONE);
    }

    public void setLightPercent(int brightness, boolean slideEnd) {
        handler.removeMessages(View.GONE);
        if (slideEnd) {
            handler.sendEmptyMessageDelayed(View.GONE, 300);
        } else {
            setVisibility(View.VISIBLE);
            tv_percent.setText(brightness + "%");
        }
    }
}
