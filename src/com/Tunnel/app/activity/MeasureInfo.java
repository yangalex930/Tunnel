package com.Tunnel.app.activity;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Yang Yupeng on 2014/8/3.
 */
public class MeasureInfo implements Parcelable {
    public Rect cropRect = new Rect();
    public Rect markRect = new Rect();
    public int slope;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(cropRect, flags);
        dest.writeParcelable(markRect, flags);
        dest.writeInt(slope);
    }

    public static final Parcelable.Creator<MeasureInfo> CREATOR = new Parcelable.Creator<MeasureInfo>() {

        @Override
        public MeasureInfo createFromParcel(Parcel source) {
            MeasureInfo mi = new MeasureInfo();
            mi.cropRect = source.readParcelable(Rect.class.getClassLoader());
            mi.markRect = source.readParcelable(Rect.class.getClassLoader());
            mi.slope = source.readInt();
            return mi;
        }

        @Override
        public MeasureInfo[] newArray(int size) {
            return new MeasureInfo[0];
        }
    };
}
