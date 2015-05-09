package com.Tunnel.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.Tunnel.app.R;

/**
 * Created by Yang Yupeng on 2014/8/10.
 */
public class PageControlView extends LinearLayout {

    private Context mContext;

    public PageControlView(Context ctx) {
        super(ctx);
        mContext = ctx;
    }


    public PageControlView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        mContext = ctx;
    }

    public void setIndication(int cnt, int index) {
        if (index < 0 || index > cnt)
            index = 0;
        removeAllViews();
        for (int i = 0; i < cnt; i++) {
            ImageView iv = new ImageView(mContext);
            iv.setImageResource(index == i ? R.drawable.page_indicator_focused
                    : R.drawable.page_indicator_unfocused);
            if (i != 0 || i != cnt - 1) {
                iv.setPadding(6, 0, 6, 0);
            }
            addView(iv);
        }
    }

}
