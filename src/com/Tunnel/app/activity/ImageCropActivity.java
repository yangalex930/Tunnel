package com.Tunnel.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;
import com.Tunnel.app.R;
import com.Tunnel.app.util.BitmapUtil;
import com.Tunnel.app.util.GlobalSwitch;
import com.Tunnel.app.view.CropImageView;

/**
 * Created by Yang Yupeng on 2014/7/24.
 */
public class ImageCropActivity extends Activity {

    private CropImageView imageView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_cropping);
        imageView = (CropImageView)findViewById(R.id.imageView);

        long t = System.currentTimeMillis();
        final String imagePath = getIntent().getStringExtra("ImagePath");
        final Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        imageView.setImageBitmap(bitmap);
        Log.d("TimeProfile", "ImageCropActivity onCreate cost time: " + (System.currentTimeMillis() - t));

        ImageButton cancel = (ImageButton)findViewById(R.id.cancel);
        cancel.setVisibility(View.VISIBLE);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.textView1).setVisibility(View.VISIBLE);

        ImageButton ok = (ImageButton)findViewById(R.id.ok);
        ok.setVisibility(View.VISIBLE);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Rect rect = new Rect(imageView.getCroppingArea());

                if (!GlobalSwitch.bCorrectionMode) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap cacheBitmap = BitmapUtil.drawCacheBitmap(bitmap, rect);
                            BitmapUtil.saveTempBitmapCache(cacheBitmap);
                            cacheBitmap.recycle();
                        }
                    }).start();
                }

                Intent intent = new Intent(ImageCropActivity.this, ImageMarkActivity.class);
                intent.putExtra("ImagePath", imagePath);
                intent.putExtra("Rect", rect);
                startActivity(intent);
            }
        });
        findViewById(R.id.textView3).setVisibility(View.VISIBLE);

        Toast.makeText(this, R.string.notify_select_goal, Toast.LENGTH_LONG).show();
    }
}
