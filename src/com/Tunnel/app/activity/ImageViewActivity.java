package com.Tunnel.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.*;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import com.Tunnel.app.dialog.ConfirmDeleteDialog;
import com.Tunnel.app.util.*;
import com.Tunnel.app.R;

import java.io.File;
import java.util.*;

/**
 * Created by Yang Yupeng on 2014/7/30.
 */
public class ImageViewActivity extends Activity {

    DisplayMetrics dm;
    private ViewFlipper viewFlipper;
    GestureDetector detector = null;

    boolean bDelete = false;
    String scrImagePath;

    ArrayList<String> imageList = new ArrayList<String>();
    int imageIndex;
    BitmapCache bitmapCache = new BitmapCache();

    class BitmapCache extends LruCache<Integer, Bitmap>
    {
        public BitmapCache() {
            super(5);
        }

        @Override
        protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            if (!oldValue.isRecycled()) {
                oldValue.recycle();
            }
        }
    }

    class PreLoadTask extends AsyncTask<Integer, Void, Void> {

        boolean forward = true;
        public PreLoadTask(boolean forward) {
            this.forward = forward;
        }

        @Override
        protected Void doInBackground(Integer... params) {
            int index = params[0];
            if (forward) {
                if (index + 1 < imageList.size()) {
                    getBitmapByIndex(index + 1);
                }
                if (index + 2 < imageList.size()) {
                    getBitmapByIndex(index + 2);
                }

            }
            else {
                if (index > 0) {
                    getBitmapByIndex(index - 1);
                }
                if (index > 1) {
                    getBitmapByIndex(index - 2);
                }
            }
            return null;
        }
    }

    private Bitmap getBitmapByIndex(int index) {
        long t = System.currentTimeMillis();
        Bitmap bitmap = bitmapCache.get(index);
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = BitmapUtil.getImageThumbnail(imageList.get(index), dm.widthPixels, dm.heightPixels);
            bitmapCache.put(index, bitmap);
        }
        Log.d("TimeProfile", "Bitmap decode cost: " + (System.currentTimeMillis() - t));
        return bitmap;
    }

    private void deleteBitmapByIndex(int index) {
        Map<Integer, Bitmap> snapshot = bitmapCache.snapshot();
        bitmapCache = new BitmapCache();
        for (Map.Entry<Integer, Bitmap> entry:snapshot.entrySet()) {
            if (entry.getKey() == index) {
                Bitmap bitmap = entry.getValue();
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            else {
                bitmapCache.put(entry.getKey() - 1, entry.getValue());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_process);
        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);

        if (dm == null) {
            dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
        }

        detector = new GestureDetector(this, simpleOnGestureListener);

        imageIndex = getIntent().getIntExtra("ImageIndex", 0);
        String path = getIntent().getStringExtra("Path");
        File[] files = FileUtil.getFileList(path);
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".jpg")) {
                if (file.getName().contains("原图")) {
                    scrImagePath = file.getPath();
                }
                imageList.add(file.getPath());
            }
        }

        if (imageIndex % 2 != 0) {
            viewFlipper.showNext();
        }

        ((ImageView) viewFlipper.getChildAt(imageIndex % 2)).setImageBitmap(getBitmapByIndex(imageIndex));
        new PreLoadTask(true).execute(imageIndex);
    }


    private boolean isSrcImage() {
        return imageList.get(imageIndex).contains("原图");
    }

    @Override
    protected void onResume() {
        super.onResume();
//        GlobalSwitch.bOpenFromCapture = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bitmapCache.evictAll();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add(Menu.FIRST, 0, 0, R.string.delete);
        item.setTitle(R.string.delete);
        item.setIcon(R.drawable.delete);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ConfirmDeleteDialog.create(ImageViewActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bDelete = true;
                        String imagePath = imageList.get(imageIndex);
                        FileUtil.deleteFile(imagePath);
                        imageList.remove(imageIndex);
                        deleteBitmapByIndex(imageIndex);

                        if (imageIndex == imageList.size()) {
                            imageIndex = imageList.size() - 1;
                        }

                        if (imageList.size() == 0) {
                            onBackPressed();
                            return;
                        }

                        ((ImageView) viewFlipper.getCurrentView()).setImageBitmap(getBitmapByIndex(imageIndex));
                    }
                }, null).show();
                return false;
            }
        });

        if (isSrcImage()) {
            item = menu.add(Menu.FIRST, 0, 0, R.string.begin_check);
            item.setTitle(R.string.begin_check);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    GlobalSwitch.bOpenFromCapture = false;
                    Intent intent = new Intent(ImageViewActivity.this, ImageCropActivity.class);
                    Orientation.getOrientationExif(imageList.get(imageIndex));
                    intent.putExtra("ImagePath", imageList.get(imageIndex));
                    startActivity(intent);
                    return false;
                }
            });
        } else {
            item = menu.add(Menu.FIRST, 0, 0, R.string.view_check_result);
            item.setTitle(R.string.view_check_result);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    MeasureInfo mi = ExifUtil.getMeasureInfo(imageList.get(imageIndex));
                    if (mi == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ImageViewActivity.this);
                        builder.setMessage(R.string.cannot_find_check_result)
                                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                }).create().show();
                        return false;
                    }
                    Intent intent = new Intent(ImageViewActivity.this, ImageMarkActivity.class);
                    intent.putExtra("ImagePath", imageList.get(imageIndex));
                    intent.putExtra("MeasureInfo", mi);
                    startActivity(intent);
                    return false;
                }
            });
        }
        return true;
    }

    private void showPrevImg() {
        if (imageIndex > 0) {
            imageIndex--;

            ((ImageView) viewFlipper.getChildAt(imageIndex % 2)).setImageBitmap(getBitmapByIndex(imageIndex));
            new PreLoadTask(false).execute(imageIndex);

            viewFlipper.setInAnimation(AnimationUtil.getInstance().rightInAnimation);
            viewFlipper.setOutAnimation(AnimationUtil.getInstance().rightOutAnimation);

            viewFlipper.showPrevious();
            invalidateOptionsMenu();
        }
    }

    private void showNextImg() {
        if (imageIndex < imageList.size() - 1) {
            imageIndex++;
            ((ImageView) viewFlipper.getChildAt(imageIndex % 2)).setImageBitmap(getBitmapByIndex(imageIndex));
            new PreLoadTask(true).execute(imageIndex);

            viewFlipper.setInAnimation(AnimationUtil.getInstance().leftInAnimation);
            viewFlipper.setOutAnimation(AnimationUtil.getInstance().leftOutAnimation);
            viewFlipper.showNext();

            invalidateOptionsMenu();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("Need_Refresh", bDelete);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }

    GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            if (e2.getX() - e1.getX() > 50) {
                showPrevImg();
            } else if (e2.getX() - e1.getX() < -50) {
                showNextImg();
            }
            return true;
        }
    };
}
