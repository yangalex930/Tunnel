package com.Tunnel.app.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yupeng.yyp
 * @create 14-12-20 14:53
 */
public class ImageProcessUtil {

    private static final float[][] x_mask = new float[][] {{0.125f, 0, -0.125f}, {0.25f, 0, -0.25f}, {0.125f, 0, -0.125f}};
    private static final float[][] y_mask = new float[][] {{0.125f, 0.25f, 0.125f}, {0, 0, 0}, {-0.125f, -0.25f, -0.125f}};

    //获取灰度矩阵，规一化
    private static float[][] getGrayMatrix(Bitmap bitmap, Rect srcRect) {
        int w = srcRect.width() + 1;
        int h = srcRect.height() + 1;
        float [][] grayMatrix = new float[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int color = bitmap.getPixel(srcRect.left + i, srcRect.top + j);
                grayMatrix[i][j] = Color.red(color) * 0.2989f + Color.green(color) * 0.5870f + Color.blue(color) * 0.1140f;
                grayMatrix[i][j] /= 255.f;
            }
        }
        return grayMatrix;
    }

    //图像滤波，返回滤波结果
    private static float[][] filterImage(float[][] srcMatrix, float[][] mask) {
        int w = srcMatrix[0].length;
        int h = srcMatrix.length;
        float[][] new_matrix = new float[h][w];

        //temp矩阵，长宽各加2
        float [][] temp = new float[h + 2][w + 2];

        for (int i = 1; i < h + 1; i++) {
            for (int j = 1; j < w + 1; j++) {
                temp[i][j] = srcMatrix[i - 1][j - 1];
            }
        }

        temp[0][0] = temp[1][1];
        temp[0][w + 1] = temp[1][w];
        temp[h + 1][0] = temp[h][1];
        temp[h + 1][w + 1] = temp[h][w];

        for (int i = 1; i < h + 1; i++) {
            temp[i][0] = temp[i][1];
            temp[i][w + 1] = temp[i][w];
        }

        for (int j = 1; j < w + 1; j++) {
            temp[0][j] = temp[1][j];
            temp[h + 1][j] = temp[h][j];
        }


        //对原矩阵每个元素做卷积
        for (int i = 1; i <= h; i++) {
            for (int j = 1; j <= w; j++) {
                new_matrix[i - 1][j - 1] = temp[i - 1][j - 1] * mask[0][0]
                        + temp[i - 1][j] * mask[0][1]
                        + temp[i - 1][j + 1] * mask[0][2]
                        + temp[i][j - 1] * mask[1][0]
                        + temp[i][j] * mask[1][1]
                        + temp[i][j + 1] * mask[1][2]
                        + temp[i + 1][j - 1] * mask[2][0]
                        + temp[i + 1][j] * mask[2][1]
                        + temp[i + 1][j + 1] * mask[2][2];
            }
        }
        return new_matrix;
    }

    //对原矩阵x、y方向分别滤波，得到滤波后结果
    private static float[][] getFilterMatrix(float [][] srcMatrix) {
        int w = srcMatrix[0].length;
        int h = srcMatrix.length;
        float[][] filter_matrix = new float[h][w];

        float [][] x_filter = filterImage(srcMatrix, x_mask);
        float [][] y_filter = filterImage(srcMatrix, y_mask);

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                filter_matrix[i][j] = x_filter[i][j] * x_filter[i][j] + y_filter[i][j] * y_filter[i][j];
            }
        }
        return filter_matrix;
    }

    //求阈值
    private static float getCutOff(float[][] filterMatrix) {
        int w = filterMatrix[0].length;
        int h = filterMatrix.length;
        float sum = 0.0f;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                sum += filterMatrix[i][j];
            }
        }
        return 4 * sum / (w * h);
    }

    //对于滤波结果，求出边界
    private static List<Point> findEdge(float [][] filter_matrix, float cutOff) {
        List<Point> points = new ArrayList<Point>();
        int w = filter_matrix[0].length;
        int h = filter_matrix.length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (filter_matrix[i][j] > cutOff) {
                    points.add(new Point(i, j));
                }
            }
        }
        return points;
    }

    static int minDistance = 10;

    //找出边界上离当前点最近的点
    private static Point findMinDistance(List<Point> points, Point point) {
        int min = Integer.MAX_VALUE;
        Point minPt = null;
        for (Point pt:points) {
            int xOffset = point.x - pt.x;
            int yOffset = point.y - pt.y;
            int distance_pow = xOffset * xOffset + yOffset * yOffset;
            if (min > distance_pow) {
                min = distance_pow;
                minPt = pt;
            }
        }
        if (min > minDistance * minDistance) {
            minPt = null;
        }

        return minPt;
    }

    public static Point adjustPoint(Bitmap bitmap, Rect srcRect, Point point) {
        long start = System.currentTimeMillis();
        point.x -= srcRect.left;
        point.y -= srcRect.top;
        float [][] grayMatrix = getGrayMatrix(bitmap, srcRect); //求灰度矩阵
        float [][] filterMatrix = getFilterMatrix(grayMatrix); //求滤波后的矩阵
        float cutOff = getCutOff(filterMatrix); //求域值
        List<Point> edgePointList = findEdge(filterMatrix, cutOff); //求所有的边界点
        //Log.d("TimeProfile", "List Points: " + edgePointList.toString());
        point = findMinDistance(edgePointList, point); //找出边界上离当前最近的点
        if (point != null) {
            point.x += srcRect.left;
            point.y += srcRect.top;
        }
        Log.d("TimeProfile", "adjustPoint cost time: " + (System.currentTimeMillis() - start));

        return point;
    }
}
