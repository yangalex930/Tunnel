package com.Tunnel.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;
import com.Tunnel.app.R;
import com.Tunnel.app.util.BitmapUtil;
import com.Tunnel.app.util.ExifUtil;
import com.Tunnel.app.util.GlobalSwitch;
import com.Tunnel.app.util.Orientation;
import com.Tunnel.app.view.MarkImageView;

/**
 * Created by Yang Yupeng on 2014/7/24.
 */
public class ImageMarkActivity extends Activity{
    private Bitmap bmpCropping = null;
    private MarkImageView imageView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.image_mark);

        imageView = (MarkImageView)findViewById(R.id.imageView);
        Bitmap bitmap = null;

        final String imagePath = getIntent().getStringExtra("ImagePath");
        bitmap = BitmapFactory.decodeFile(imagePath);

        //正常检测
        final Rect rect = getIntent().getParcelableExtra("Rect");
        if (rect != null) {
            bmpCropping = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
            Toast.makeText(this, R.string.notify_select_start, Toast.LENGTH_SHORT).show();
        }

        //查看检测结果
        final MeasureInfo mi = getIntent().getParcelableExtra("MeasureInfo");
        if (mi != null)
        {
            bmpCropping = Bitmap.createBitmap(bitmap, mi.cropRect.left, mi.cropRect.top, mi.cropRect.width(), mi.cropRect.height());
            imageView.setMarkRect(mi.markRect);
            Toast.makeText(ImageMarkActivity.this, String.format(getString(R.string.notify_slope), mi.slope), Toast.LENGTH_SHORT).show();
        }

        bitmap.recycle();
        imageView.setImageBitmap(bmpCropping);

        imageView.setOnMarkListener(new MarkImageView.onMarkListener() {

            @Override
            public void onMarkStart() {
                Toast.makeText(ImageMarkActivity.this, R.string.notify_select_end, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMarkFinish() {
                if (!GlobalSwitch.bCorrectionMode) {
                    Toast.makeText(ImageMarkActivity.this, String.format(getString(R.string.notify_slope), (int) imageView.getSlopeRate()), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(ImageMarkActivity.this, String.format(getString(R.string.notify_degree_diff), imageView.getDegreeDiff()), Toast.LENGTH_LONG).show();
                }
            }
        });

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
        ok.setEnabled(mi == null);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!imageView.isMarkFinished())
                {
                    return;
                }

                String msg;
                if (!GlobalSwitch.bCorrectionMode) {
                    msg = String.format(getString(R.string.save_result), imageView.getSlopeRate());
                }
                else {
                    msg = String.format(getString(R.string.save_correct_result), imageView.getDegreeDiff());
                }
                new AlertDialog.Builder(ImageMarkActivity.this)
                        .setTitle(R.string.notification)
                        .setMessage(msg)
                        .setNegativeButton(getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!GlobalSwitch.bCorrectionMode) {
                                            String cacheImage = BitmapUtil.renameBitmapCache(imagePath);
                                            if (cacheImage != null) {
                                                ExifUtil.saveMeasureInfo(cacheImage, rect, imageView.getMarkRect(), imageView.getSlopeRate());
                                            } else {
                                                Toast.makeText(ImageMarkActivity.this, R.string.save_failure, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        else
                                        {
                                            Orientation.putCorrection(imageView.getDegreeDiff());
                                        }

                                        Class<? extends Activity> cls;
                                        if (GlobalSwitch.bCorrectionMode)
                                        {
                                            GlobalSwitch.bCorrectionMode = false;
                                            cls = MainActivity.class;
                                        }
                                        else if (GlobalSwitch.bOpenFromCapture)
                                        {
                                            cls = ImageCaptureActivity.class;
                                        }
                                        else
                                        {
                                            GlobalSwitch.bOpenFromCapture = true;
                                            cls = ImageGridActivity.class;
                                        }

                                        Intent intent = new Intent(ImageMarkActivity.this, cls);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);

                                    }
                                })
                        .setPositiveButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                        .create().show();
            }
        });
        findViewById(R.id.textView3).setVisibility(View.VISIBLE);

        ImageButton clear = (ImageButton)findViewById(R.id.mid);
        clear.setVisibility(View.VISIBLE);
        clear.setEnabled(mi == null);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.reset();
            }
        });
        findViewById(R.id.textView2).setVisibility(View.VISIBLE);
    }
}
