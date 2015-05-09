package com.Tunnel.app.view;

import android.graphics.*;

/**
 * Created by Yang Yupeng on 2014/7/26.
 */
public class CropRect
{
    private static int threshold = 20;
    public int left, top, right, bottom;
    public Point hitPoint = new Point();

    Paint paint = new Paint();
    {
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        PathEffect effects = new DashPathEffect(new float[]{5,5,5,5},1);
        paint.setPathEffect(effects);
    }

    public static final int ST_NONE   = 0;
    public static final int ST_LEFT   = 1;
    public static final int ST_TOP    = 2;
    public static final int ST_RIGHT  = 4;
    public static final int ST_BOTTOM = 8;
    public static final int ST_LEFT_TOP = ST_LEFT + ST_TOP;
    public static final int ST_RIGHT_TOP = ST_RIGHT + ST_TOP;
    public static final int ST_LEFT_BOTTOM = ST_LEFT + ST_BOTTOM;
    public static final int ST_RIGHT_BOTTOM = ST_RIGHT + ST_BOTTOM;
    public static final int ST_WHOLE_AREA = 16;


    int selectedType = ST_NONE;

    public void clearSelect()
    {
        selectedType = ST_NONE;
    }

    public boolean testSelect(int x, int y)
    {
        if (hitPoint(x, y, left, top))
        {
            selectedType = ST_LEFT_TOP;
        }
        else if (hitPoint(x, y, left, bottom))
        {
            selectedType = ST_LEFT_BOTTOM;
        }
        else if (hitPoint(x, y, right, top))
        {
            selectedType = ST_RIGHT_TOP;
        }
        else if (hitPoint(x, y, right, bottom))
        {
            selectedType = ST_RIGHT_BOTTOM;
        }
        else if (hitEdge(x, left, y, top, bottom))
        {
            selectedType = ST_LEFT;
        }
        else if (hitEdge(x, right, y, top, bottom))
        {
            selectedType = ST_RIGHT;
        }
        else if (hitEdge(y, top, x, left, right))
        {
            selectedType = ST_TOP;
        }
        else if (hitEdge(y, bottom, x, left, right))
        {
            selectedType = ST_BOTTOM;
        }
        else if (hitArea(x, y))
        {
            selectedType = ST_WHOLE_AREA;
        }
        hitPoint.x = x;
        hitPoint.y = y;
        return selectedType != ST_NONE;
    }

    private boolean hitPoint(int x1, int y1, int x2, int y2)
    {
        return Math.abs(x1 - x2) < threshold && Math.abs(y1 - y2) < threshold;
    }

    private boolean hitEdge(int a, int edge, int b, int start, int end)
    {
        return Math.abs(a - edge) < threshold && b > start && b < end;
    }

    private boolean hitArea(int x, int y)
    {
        return x >= (left + threshold)
                && x <= (right - threshold)
                && y >= (top + threshold)
                && y <= (bottom - threshold);
    }

    public void adjustArea(int x, int y)
    {
        int dx = x - hitPoint.x;
        int dy = y - hitPoint.y;
        if ((selectedType & ST_LEFT) != 0)
        {
            left += dx;
            if (right - left < 20)
            {
                left = right - 20;
            }
        }

        if ((selectedType & ST_RIGHT) != 0)
        {
            right += dx;
            if (right - left < 20)
            {
                right = left + 20;
            }
        }

        if ((selectedType & ST_TOP) != 0)
        {
            top += dy;
            if (bottom - top < 20)
            {
                top = bottom - 20;
            }
        }

        if ((selectedType & ST_BOTTOM) != 0)
        {
            bottom += dy;
            if (bottom - top < 20)
            {
                bottom = top + 20;
            }
        }

        if (selectedType == ST_WHOLE_AREA)
        {
            left += dx;
            right += dx;
            top += dy;
            bottom += dy;
        }

        hitPoint.x = x;
        hitPoint.y = y;
    }

    public void checkOutOfBound(int width, int height)
    {
        int w = right - left;
        int h = bottom - top;
        if (left < 0)
        {
            if (selectedType == ST_WHOLE_AREA)
            {
                right = w;
            }
            left = 0;
        }
        if (right > width)
        {
            if (selectedType == ST_WHOLE_AREA)
            {
                left = width - w;
            }
            right = width;
        }
        if (top < 0)
        {
            if (selectedType == ST_WHOLE_AREA)
            {
                bottom = h;
            }
            top = 0;
        }
        if (bottom > height)
        {
            if (selectedType == ST_WHOLE_AREA)
            {
                top = height - h;
            }
            bottom = height;
        }
    }

    public void draw(Canvas canvas)
    {
        canvas.drawRect(left, top, right, bottom, paint);
    }
}
