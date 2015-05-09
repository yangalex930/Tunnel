package com.Tunnel.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import com.Tunnel.app.R;
import com.Tunnel.app.TunnelApplication;
import com.Tunnel.app.util.AnimationUtil;
import com.Tunnel.app.util.FileUtil;
import com.Tunnel.app.util.TunnelUtil;
import com.Tunnel.app.view.PageControlView;

import java.io.*;

/**
 * Created by Yang Yupeng on 2014/8/6.
 */
public class GuideActivity extends Activity implements GestureDetector.OnGestureListener, Animation.AnimationListener {

    private ViewFlipper viewFlipper;
    private PageControlView pageControlView;
    private Button btnEntry;
    private GestureDetector detector;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences sp = getSharedPreferences("Pref", MODE_PRIVATE);
        boolean bHasLoggedIn = sp.getBoolean("hasLoggedIn", false);
        if (bHasLoggedIn) {
            Intent intent = new Intent(GuideActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            setContentView(R.layout.user_guide);
            viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
            viewFlipper.addView(getImageView(R.drawable.guide1));
            viewFlipper.addView(getImageView(R.drawable.guide2));
            viewFlipper.addView(getImageView(R.drawable.guide3));
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View view = layoutInflater.inflate(R.layout.guide4, null);
            viewFlipper.addView(view);

            detector = new GestureDetector(this);
            btnEntry = (Button)view.findViewById(R.id.enterBtn);
            btnEntry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("hasLoggedIn", true);
                    editor.apply();
                    finish();
                    Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            pageControlView = (PageControlView)findViewById(R.id.pageControlView);
            pageControlView.setIndication(4, 0);

            AnimationUtil.getInstance().leftInAnimation.setAnimationListener(this);
            AnimationUtil.getInstance().leftOutAnimation.setAnimationListener(this);
            AnimationUtil.getInstance().rightInAnimation.setAnimationListener(this);
            AnimationUtil.getInstance().rightOutAnimation.setAnimationListener(this);
        }
    }

    private ImageView getImageView(int id){
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageResource(id);
        return imageView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.detector.onTouchEvent(event);
    }

    private int index = 0;
    private void showNext()
    {
        if (index < 3) {
            index++;
            viewFlipper.setInAnimation(AnimationUtil.getInstance().leftInAnimation);
            viewFlipper.setOutAnimation(AnimationUtil.getInstance().leftOutAnimation);
            viewFlipper.showNext();
        }
    }
    private void showPrevious()
    {
        if (index > 0) {
            index--;
            viewFlipper.setInAnimation(AnimationUtil.getInstance().rightInAnimation);
            viewFlipper.setOutAnimation(AnimationUtil.getInstance().rightOutAnimation);
            viewFlipper.showPrevious();
        }
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v2) {
        if (e1.getX() - e2.getX() > 120) {
            showNext();
            return true;
        } else if (e1.getX() - e2.getY() < -120) {
            showPrevious();
            return true;
        }
        return false;
    }


    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        pageControlView.setIndication(4, index);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
