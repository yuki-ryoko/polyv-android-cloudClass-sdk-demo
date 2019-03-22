package com.easefun.polyv.commonui.player;

import android.view.View;

import com.easefun.polyv.businesssdk.api.auxiliary.PolyvAuxiliaryVideoview;
import com.easefun.polyv.businesssdk.api.common.meidaControl.IPolyvMediaController;
import com.easefun.polyv.businesssdk.api.common.player.microplayer.PolyvCommonVideoView;
import com.easefun.polyv.commonui.player.ppt.PolyvPPTItem;

/**
 * @author df
 * @create 2018/8/10
 * @Describe
 */
public interface IPolyvVideoItem<T extends PolyvCommonVideoView, R extends IPolyvMediaController> {
    View getView();

    T getVideoView();

    PolyvAuxiliaryVideoview getSubVideoView();

    R getController();

    void resetUI();

    void bindPPTView(PolyvPPTItem polyvPPTItem);

    void destroy();
}
