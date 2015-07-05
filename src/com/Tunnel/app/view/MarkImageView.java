package com.Tunnel.app.view;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import com.Tunnel.app.R;
import com.Tunnel.app.util.ImageProcessUtil;
import com.Tunnel.app.util.Orientation;

/**
 * Created by Yang Yupeng on 2014/7/25.
 */
public class MarkImageView extends ImageView {

    private static final int RADIUS = 50;
    private static final int FORESIGHT_RADIUS = 20;
    private static final int SIZE = 250;
    private static final float ZOOM_FACTOR = (float)SIZE / (RADIUS * 2);
    private static final long DELAY_TIME = 150;

    Handler handler = new Handler();

    Paint paint = new Paint();
    {
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
    }

    Paint paint2 = new Paint();
    {
        paint2.setColor(Color.WHITE);
        paint2.setStrokeWidth(3);
        paint2.setAntiAlias(true);
        paint2.setStyle(Paint.Style.STROKE);
    }

    private Point start = new Point();
    private Point end = new Point();

    private boolean bStartSet = false;
    private boolean bStartSetting = false;
    private boolean bEndSet = false;
    private boolean bEndSetting = false;

    private float wScale = 0.0f;
    private float hScale = 0.0f;

    private float slopeDegree = Float.MIN_VALUE;

    boolean viewMode = false;
    boolean bAdjustForViewMode = false;

    private Rect srcRect = new Rect();
    private Rect magnifierRect = new Rect(0, 0, SIZE, SIZE);
    private Point dstPoint = new Point();
    private PopupWindow popup;
    private Magnifier magnifier;

    public MarkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        magnifier = new Magnifier(context);
        magnifier.setBackgroundResource(R.drawable.magnifier_bg);
        popup = new PopupWindow(magnifier, SIZE, SIZE);
        popup.setAnimationStyle(android.R.style.Animation_Toast);
    }

    public void reset()
    {
        start.x = 0;
        start.y = 0;
        end.x = 0;
        end.y = 0;
        bStartSet = false;
        bStartSetting = false;
        bEndSet = false;
        bEndSetting = false;
        slopeDegree = Float.MIN_VALUE;
        invalidate();
    }

    public boolean isMarkFinished()
    {
        return bEndSet;
    }

    public void setMarkRect(Rect rect)
    {
        viewMode = true;
        start.x = rect.left;
        start.y = rect.top;
        end.x = rect.right;
        end.y = rect.bottom;
    }

    public Rect getMarkRect()
    {
        return new Rect((int)(start.x * wScale) , (int)(start.y * hScale),
                (int)(end.x * wScale), (int)(end.y * hScale));
    }

    public int getSlopeRate()
    {
        if (slopeDegree == Float.MIN_VALUE)
        {
            if (end.x == start.x)
            {
                slopeDegree = 90.0f;
            }
            else
            {
                float slope = ((end.y - start.y) * hScale) / ((end.x - start.x) * wScale);
                slopeDegree = (float)Math.toDegrees(Math.atan(slope));
            }
            slopeDegree = Orientation.adjustDegree(slopeDegree);
            slopeDegree = Orientation.correctDegreeDiff(Math.abs(slopeDegree));
        }

        float slope = Math.abs((float)Math.tan(Math.toRadians(slopeDegree)));
        return (int)slope;
    }

    public float getDegreeDiff()
    {
        if (slopeDegree == Float.MIN_VALUE)
        {
            if (end.x == start.x)
            {
                slopeDegree = 90.0f;
            }
            else
            {
                float slope = ((end.y - start.y) * hScale) / ((end.x - start.x) * wScale);
                slopeDegree = (float)Math.toDegrees(Math.atan(slope));
            }
            slopeDegree = Orientation.adjustDegree(slopeDegree);
        }
        return 90.f - Math.abs(slopeDegree);
    }

    private void calculateScaleFactor()
    {
        Bitmap bmp = ((BitmapDrawable)getDrawable()).getBitmap();
        wScale = (float)bmp.getWidth() / getWidth();
        hScale = (float)bmp.getHeight() / getHeight();
    }

    private void calSrcRect(int x, int y)
    {
        Bitmap bmp = ((BitmapDrawable)getDrawable()).getBitmap();
        x = (int)(x * wScale);
        y = (int)(y * hScale);
        if (x < RADIUS)
        {
            srcRect.left = 0;
            srcRect.right = 2 * RADIUS;
        }
        else if (x + RADIUS > bmp.getWidth())
        {
            srcRect.right = bmp.getWidth();
            srcRect.left = srcRect.right - 2 * RADIUS;
        }
        else
        {
            srcRect.left = x - RADIUS;
            srcRect.right = x + RADIUS;
        }
        if (y < RADIUS)
        {
            srcRect.top = 0;
            srcRect.bottom = 2 * RADIUS;
        }
        else if (y + RADIUS > bmp.getHeight())
        {
            srcRect.bottom = bmp.getHeight();
            srcRect.top = srcRect.bottom - 2 * RADIUS;
        }
        else
        {
            srcRect.top = y - RADIUS;
            srcRect.bottom = y + RADIUS;
        }
        dstPoint.x = (int)((x - srcRect.left) * ZOOM_FACTOR);
        dstPoint.y = (int)((y - srcRect.top) * ZOOM_FACTOR);
    }

    private void moveMagnifierRect()
    {
        if (magnifierRect.left == 0)
        {
            magnifierRect.right = getWidth();
            magnifierRect.left = magnifierRect.right - SIZE;
        }
        else
        {
            magnifierRect.left = 0;
            magnifierRect.right = SIZE;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        calculateScaleFactor();
        if (viewMode && !bAdjustForViewMode)
        {
            bAdjustForViewMode = true;
            start.x /= wScale;
            start.y /= hScale;
            end.x /= wScale;
            end.y /= hScale;
        }
    }

    Runnable showZoom = new Runnable() {
        public void run() {
            popup.showAtLocation(MarkImageView.this, Gravity.NO_GRAVITY, magnifierRect.left, magnifierRect.top);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (viewMode)
        {
            return false;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_MOVE)
        {
            int x = (int)event.getX();
            int y = (int)event.getY();
            if (!bStartSet)
            {
                start.x = x;
                start.y = y;
                bStartSetting = true;
            }
            else if (!bEndSet)
            {
                end.x = x;
                end.y = y;
                bEndSetting = true;
            }
            else
            {
                return false;
            }
            calSrcRect(x, y);
            boolean bMove = magnifierRect.contains(x, y);

            if (action == MotionEvent.ACTION_DOWN)
            {
                if (bMove)
                {
                    moveMagnifierRect();
                }
                removeCallbacks(showZoom);
                postDelayed(showZoom, DELAY_TIME);
            }
            else if (!popup.isShowing())
            {
                showZoom.run();
            }
            else
            {
                if (bMove)
                {
                    moveMagnifierRect();
                    popup.dismiss();
                    postDelayed(showZoom, DELAY_TIME);
                }
            }
            invalidate();
            magnifier.invalidate();
        }
        else if (action == MotionEvent.ACTION_UP)
        {
            final int x = (int)event.getX();
            final int y = (int)event.getY();
            if (!bStartSet)
            {
                bStartSet = true;
                bStartSetting = false;
                start.x = x;
                start.y = y;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Point point = ImageProcessUtil.adjustPoint(((BitmapDrawable) getDrawable()).getBitmap(), srcRect, new Point((int) (x * wScale), (int) (y * hScale)));
                            if (point != null) {
                                end.x = (int) (point.x / wScale);
                                end.y = (int) (point.y / hScale);
                                invalidate();
                            }
                        }catch (Exception e) {

                        }
                    }
                });
                if (listener != null)
                {
                    listener.onMarkStart();
                }
            }
            else if (!bEndSet)
            {
                bEndSetting = false;
                bEndSet = true;
                end.x = x;
                end.y = y;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Point point = ImageProcessUtil.adjustPoint(((BitmapDrawable) getDrawable()).getBitmap(), srcRect, new Point((int) (x * wScale), (int) (y * hScale)));
                            if (point != null) {
                                end.x = (int) (point.x / wScale);
                                end.y = (int) (point.y / hScale);
                                invalidate();
                            }
                        } catch (Exception e) {

                        }
                    }
                });
                if (listener != null)
                {
                    listener.onMarkFinish();
                }
            }
            removeCallbacks(showZoom);
            popup.dismiss();
            invalidate();
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewMode)
        {
            Path path = new Path();
            path.moveTo(start.x, start.y);
            path.lineTo(end.x, end.y);
            canvas.drawPath(path, paint);
            return;
        }
        if (bStartSetting)
        {
            drawForesight(canvas, start.x, start.y, FORESIGHT_RADIUS);
        }
        if (bEndSetting)
        {
            drawForesight(canvas, end.x, end.y, FORESIGHT_RADIUS);
        }
        if (bStartSet)
        {
            canvas.drawPoint(start.x, start.y, paint);
            if (bEndSetting || bEndSet)
            {
                Path path = new Path();
                path.moveTo(start.x, start.y);
                path.lineTo(end.x, end.y);
                canvas.drawPath(path, paint);
            }
        }
    }

    private void drawForesight(Canvas canvas, int x, int y, int radius)
    {
        Path path = new Path();
        path.moveTo(x - radius, y);
        path.lineTo(x + radius, y);
        path.moveTo(x, y - radius);
        path.lineTo(x, y + radius);
        canvas.drawPath(path, paint2);

        canvas.drawCircle(x, y, 2 * radius, paint2);
    }

    public static interface onMarkListener{
        void onMarkStart();
        void onMarkFinish();
    }

    public onMarkListener listener = null;

    public void setOnMarkListener(onMarkListener listener)
    {
        this.listener = listener;
    }

    class Magnifier extends View {

        private Rect displayRect = new Rect(4, 4, SIZE - 4, SIZE - 4);
        public Magnifier(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.save();
            // draw popup

            Bitmap bitmap = ((BitmapDrawable)MarkImageView.this.getDrawable()).getBitmap();
            canvas.drawBitmap(bitmap, srcRect, displayRect, null);
            canvas.restore();

            drawForesight(canvas, dstPoint.x, dstPoint.y, FORESIGHT_RADIUS);
        }
    }
}
