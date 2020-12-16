package com.newolf.speechtotextdemo.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.SparseIntArray;
import android.widget.ImageView;

/**
 * 功能描述
 *
 * @author NeWolf
 * @since 2020-12-16
 */
public abstract class AbsDrawable extends Drawable {
    public static final int INVALID_COLOR = 4178531;
    private float[] mColorFilterArray;
    protected Point mCropOffset;
    public boolean mCustomed = false;
    private int mDefaultAlpha = 255;
    protected ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_XY;

    public interface OnBitmapLoadedListener {
        void onBitmapLoaded(Bitmap bitmap);
    }



    public abstract void scale(float f);

    public abstract void setColorFilter(SparseIntArray sparseIntArray);

    public ColorMatrixColorFilter getColorFilter(int i) {
        ColorMatrix colorMatrix = new ColorMatrix();
        int red = Color.red(i);
        int green = Color.green(i);
        int blue = Color.blue(i);
        if (this.mColorFilterArray == null) {
            this.mColorFilterArray = new float[]{0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f};
        }
        this.mColorFilterArray[4] = (float) red;
        this.mColorFilterArray[9] = (float) green;
        this.mColorFilterArray[14] = (float) blue;
        colorMatrix.set(this.mColorFilterArray);
        return new ColorMatrixColorFilter(colorMatrix);
    }

    public void setScaleType(ImageView.ScaleType scaleType) {
        if (scaleType != null) {
            this.mScaleType = scaleType;
        }
    }

    public ImageView.ScaleType getScaleType() {
        return this.mScaleType;
    }

    public void setCropOffset(int i, int i2) {
        if (this.mCropOffset == null) {
            this.mCropOffset = new Point();
        }
        this.mCropOffset.x = i;
        this.mCropOffset.y = i2;
    }

    public void setDefaultAlpha(int i) {
        this.mDefaultAlpha = i;
    }

    public int getDefaultAlpha() {
        return this.mDefaultAlpha;
    }

    public Bitmap getBitmap() {
        return null;
    }
}
