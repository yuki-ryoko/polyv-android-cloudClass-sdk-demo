package com.easefun.polyv.cloudclassdemo.watch.linkMic.widget;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

/**
 * @author df
 * @create 2018/11/30
 * @Describe 旋转view的抽象定义
 */
public interface IPolyvRotateBaseView {

    void topSubviewTo(final int top);

    void resetSoftTo();

    void resetFloatViewLand();

    void resetFloatViewPort();

    ViewGroup.MarginLayoutParams getLayoutParamsLayout();

    void setOriginTop(int originTop);

    void scrollToPosition(int pos, View parent);

    ViewTreeObserver getViewTreeObserver();

    void setLayoutParams(ViewGroup.LayoutParams params);

    void setVisibility(int visibility);

    ViewGroup getOwnView();

    void enableShow(boolean show);
}
