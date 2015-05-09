package com.Tunnel.app.view;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by Yang Yupeng on 2014/7/24.
 */
public class CropImageView extends ImageView {

    CropRect rect = new CropRect();
    float wScale = 0.0f;
    float hScale = 0.0f;

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDrawingCacheEnabled(true);
    }

    public Rect getCroppingArea() {
        return new Rect((int) (rect.left * wScale), (int) (rect.top * hScale),
                (int) (rect.right * wScale), (int) (rect.bottom * hScale));
    }

    private void calculateScale()
    {
        Bitmap bmp = ((BitmapDrawable) getDrawable()).getBitmap();
        wScale = (float) bmp.getWidth() / getMeasuredWidth();
        hScale = (float) bmp.getHeight() / getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        calculateScale();
        rect.left = left + (right - left) / 5 * 2;
        rect.top = top + 200;
        rect.right = right - (right - left) / 5 * 2;
        rect.bottom = bottom - 200;
    }


    Paint paint = new Paint();
    {
        paint.setARGB(90, 8, 8, 8);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, rect.left, getHeight(), paint);
        canvas.drawRect(rect.left, 0, rect.right, rect.top, paint);
        canvas.drawRect(rect.right, 0, getWidth(), getHeight(), paint);
        canvas.drawRect(rect.left, rect.bottom, rect.right, getHeight(), paint);
        rect.draw(canvas);
    }

    boolean bPointDown = false;
    boolean bPointUp = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int action = event.getAction() & MotionEvent.ACTION_MASK;
        int x = (int)event.getX();
        int y = (int)event.getY();

        if (action == MotionEvent.ACTION_DOWN)
        {
//            Log.e("Tunnel", String.format("ACTION_DOWN (%d, %d) ", x, y) + rect.selectedType.toString());
            return rect.testSelect(x, y);
        }
        else if (action == MotionEvent.ACTION_POINTER_DOWN)
        {
            bPointDown = true;
//            Log.e("Tunnel", String.format("ACTION_POINTER_DOWN (%d, %d) ", x, y));
        }
        else if (action == MotionEvent.ACTION_POINTER_UP)
        {
            bPointDown = false;
            bPointUp = true;
//            Log.e("Tunnel", String.format("ACTION_POINTER_UP (%d, %d) ", x, y));
        }
        else if (action == MotionEvent.ACTION_MOVE)
        {
            if (bPointDown)
            {
                return false;
            }
            if (bPointUp)
            {
                bPointUp = false;
                rect.hitPoint.x = x;
                rect.hitPoint.y = y;
                return false;
            }
            rect.adjustArea(x, y);
//            Log.e("Tunnel", String.format("ACTION_MOVE Point(%d, %d) Rect(%d, %d, %d, %d)",
//                    x, y, rect.left, rect.top, rect.right,rect.bottom));
            rect.checkOutOfBound(getWidth(), getHeight());
            invalidate();
        }
        else if (action == MotionEvent.ACTION_UP)
        {
            //Log.e("Tunnel", "ACTION_UP");
            rect.clearSelect();
        }
        return true;
    }
}
