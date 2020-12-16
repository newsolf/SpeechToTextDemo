package com.newolf.speechtotextdemo.view;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.newolf.speechtotextdemo.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VolumeDrawable extends AbsDrawable implements ISwitcher, Runnable {

    private static final String TAG = VolumeDrawable.class.getSimpleName();
    static Random random;
    private float mBaseWidth;
    private List<LineData> mDatas;
    private double mDenominator;
    private int mDisableColor = -2144366082;
    private RectF mDrawRect;
    private float[] mEh;
    private boolean mEnable = true;
    private FrameUnitPool mFrameUnitPool;
    private SparseArray<Double> mHeightCaches;
    private boolean mIncrease;
    private int mIndex;
    private boolean mInited;
    private boolean mIsRunning;
    private int mLineCount;
    private float mLineWidth;
    private int[] mLoc;
    private int mLowMode;
    private int mMinHeight;
    private int mMode = 1;
    private int mNormalColor = -13659650;
    private Paint mPaint = new Paint();
    private SparseArray<Double> mRandomCaches;
    private int mStepWidth;
    private boolean[] mSv;
    private int mViewHeight;
    private int mViewWidth;

    public static class LineData {
        public int height;
        public float lastHeight;
        public List<FrameUnit> timelist;
        public int width;
        public int x;
        public int y;
    }

    public VolumeDrawable() {
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(this.mNormalColor);
        this.mDrawRect = new RectF();
        this.mRandomCaches = new SparseArray<>();
        this.mHeightCaches = new SparseArray<>();
    }

    @Override // com.iflytek.inputmethod.common.view.widget.drawable.AbsDrawable
    public void scale(float f) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "scale: " + f);
        }
        if (Math.abs(this.mBaseWidth - this.mLineWidth) < 1.0E-5f) {
            this.mLineWidth *= f;
            this.mMinHeight = (int) (((float) this.mMinHeight) * f);
            this.mStepWidth = (int) (((float) this.mStepWidth) * f);
        }
    }





    @Override // com.iflytek.inputmethod.common.view.widget.drawable.AbsDrawable
    public void setColorFilter(SparseIntArray sparseIntArray) {
        if (sparseIntArray != null && sparseIntArray.indexOfKey(0) >= 0) {
            this.mNormalColor = sparseIntArray.get(0);
            this.mDisableColor = ColorUtils.changeColorAlpha(this.mNormalColor, 128);
            this.mPaint.setColor(this.mNormalColor);
        }
    }

    public int getNormalColor() {
        return this.mNormalColor;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(getBounds());
        if (this.mMode == 1) {
            if (BuildConfig.DEBUG) {
                long currentTimeMillis = System.currentTimeMillis();
                drawVolume(canvas);
//                Log.d(TAG, "draw volume: " + (System.currentTimeMillis() - currentTimeMillis) + " ms");
            } else {
                drawVolume(canvas);
            }
        } else if (this.mMode == 2) {
            if (BuildConfig.DEBUG) {
                long currentTimeMillis2 = System.currentTimeMillis();
                drawWait(canvas);
//                Log.d(TAG, "draw wait: " + (System.currentTimeMillis() - currentTimeMillis2) + " ms");
            } else {
                drawWait(canvas);
            }
        }
        canvas.restore();
    }

    public void setAlpha(int i) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    @SuppressLint("WrongConstant")
    public int getOpacity() {
        return 0;
    }

    public void setBounds(int i, int i2, int i3, int i4) {
        super.setBounds(i, i2, i3, i4);
        if (i3 - i != this.mViewWidth || i4 - i2 != this.mViewHeight) {
            this.mViewWidth = i3 - i;
            this.mViewHeight = i4 - i2;
            this.mInited = false;
            init();
        }
    }

    public VolumeDrawable setMode(int i) {
        this.mMode = i;
        if (i == 2) {
            this.mIndex = 0;
            this.mIncrease = true;
        }
        return this;
    }

    public void setStepWidth(int i) {
        this.mStepWidth = i;
    }

    public void setLineWidth(int i) {
        this.mBaseWidth = (float) i;
        this.mLineWidth = (float) i;
    }

    public void setMinHeight(int i) {
        this.mMinHeight = i;
    }

    private void init() {
        if (!this.mInited) {
            this.mLineCount = this.mViewWidth / (((int) this.mLineWidth) + this.mStepWidth);
            this.mFrameUnitPool = new FrameUnitPool(this.mLineCount + 1);
            this.mDenominator = Math.pow((double) this.mLineCount, 4.0d);
            int i = (this.mViewWidth - ((((int) this.mLineWidth) + this.mStepWidth) * this.mLineCount)) / 2;
            this.mDatas = new ArrayList();
            for (int i2 = 0; i2 < this.mLineCount; i2++) {
                LineData lineData = new LineData();
                lineData.x = i;
                lineData.y = this.mViewHeight / 2;
                lineData.width = (int) this.mLineWidth;
                lineData.height = this.mMinHeight;
                lineData.timelist = new ArrayList();
                this.mDatas.add(lineData);
                i = (int) (((float) i) + this.mLineWidth + ((float) this.mStepWidth));
            }
            this.mSv = new boolean[this.mLineCount];
            int i3 = this.mLineCount;
            if (i3 < 62) {
                i3 = 62;
            }
            this.mEh = new float[i3];
            this.mLoc = new int[i3];
            this.mInited = true;
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "init w: " + this.mLineWidth + " h: " + this.mMinHeight + " s: " + this.mStepWidth);
            }
        }
    }

    private double getRandomValue(int i) {
        if (this.mRandomCaches.indexOfKey(i) >= 0) {
            return this.mRandomCaches.get(i).doubleValue();
        }
        double pow = 18.0d * Math.pow((double) i, 4.0d);
        this.mRandomCaches.put(i, Double.valueOf(pow));
        return pow;
    }

    private double getHeightValue(int i) {
        if (this.mHeightCaches.indexOfKey(i) >= 0) {
            return this.mHeightCaches.get(i).doubleValue();
        }
        double pow = Math.pow(0.5d, (double) i);
        this.mHeightCaches.put(i, Double.valueOf(pow));
        return pow;
    }

    public void disable() {
        if (this.mEnable) {
            this.mEnable = false;
            init();
            this.mPaint.setColor(this.mDisableColor);
            for (int i = 0; i < this.mDatas.size(); i++) {
                this.mDatas.get(i).height = this.mMinHeight;
            }
        }
    }

    public void reset() {
        if (this.mMode == 1) {
            init();
            if (!this.mEnable) {
                this.mEnable = true;
                this.mPaint.setColor(this.mNormalColor);
            }
            for (LineData lineData : this.mDatas) {
                lineData.timelist.clear();
                lineData.height = this.mMinHeight;
            }
        }
    }

    public void setVolume(int i) {
        float f;
        float f2;
        if (this.mMode == 1) {
            init();
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "vol: " + i);
            }
            if (!this.mEnable) {
                this.mEnable = true;
                this.mPaint.setColor(this.mNormalColor);
            }
            float f3 = (float) (((double) i) / 8.0d);
            if (f3 > 0.6f) {
                f3 = 0.6f;
            }
            float f4 = (float) (this.mLineCount / 2);
            if (f3 >= 0.1f) {
                f = 2.0f + f3;
                this.mLowMode = 0;
            } else if (this.mLowMode <= 0 || this.mLowMode >= 3) {
                this.mLowMode = 0;
                this.mLowMode++;
                f3 = 0.1f;
                f = 0.05f;
            } else {
                this.mLowMode++;
                return;
            }
            for (int i2 = 0; i2 < this.mLineCount; i2++) {
                if (((float) i2) < f4) {
                    this.mSv[i2] = randomBool((int) (((double) f3) * ((getRandomValue(i2) / this.mDenominator) + 0.05000000074505806d) * 100.0d));
                } else {
                    this.mSv[i2] = randomBool((int) (((double) f3) * ((getRandomValue(i2 - this.mLineCount) / this.mDenominator) + 0.05000000074505806d) * 100.0d));
                }
            }
            int random2 = (((int) (20.0f * f)) + (getRandom() % 3)) - 1;
            if (random2 <= 0) {
                random2 = 1;
            }
            float pow = 0.8f * ((float) Math.pow((double) f, 0.3330000042915344d)) * ((float) this.mViewHeight);
            for (int i3 = 0; i3 < random2; i3++) {
                if (((float) i3) < f4) {
                    this.mEh[i3] = (float) (((double) pow) * ((getRandomValue(i3) / this.mDenominator) + 0.05000000074505806d) * 10.0d);
                } else {
                    this.mEh[i3] = (float) (((double) pow) * ((getRandomValue(i3 - this.mLineCount) / this.mDenominator) + 0.05000000074505806d) * 10.0d);
                }
            }
            int i4 = 0;
            for (int i5 = 0; i5 < this.mLineCount; i5++) {
                if (this.mSv[i5]) {
                    this.mLoc[i4] = i5;
                    i4++;
                }
            }
            if (i4 > 1) {
                int min = Math.min(30, i4);
                for (int i6 = 0; i6 < min; i6++) {
                    int random3 = getRandom() % i4;
                    int random4 = getRandom() % i4;
                    if (random3 == random4) {
                        random4 = (random4 + 1) % i4;
                    }
                    int i7 = this.mLoc[random3];
                    this.mLoc[random3] = this.mLoc[random4];
                    this.mLoc[random4] = i7;
                }
            }
            int min2 = Math.min(random2, i4);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "factm: " + min2);
            }
            for (int i8 = 0; i8 < this.mLineCount; i8++) {
                float f5 = (float) this.mMinHeight;
                for (int i9 = 0; i9 < min2; i9++) {
                    f5 = (float) (((double) f5) + (((double) this.mEh[i9]) * getHeightValue(Math.abs(this.mLoc[i9] - i8))));
                }
                if (f5 > ((float) this.mViewHeight)) {
                    f2 = (float) this.mViewHeight;
                } else {
                    f2 = f5;
                }
                LineData lineData = this.mDatas.get(i8);
                List<FrameUnit> list = lineData.timelist;
                if (Math.abs(f2 - lineData.lastHeight) >= 1.0f) {
                    float f6 = f2 < lineData.lastHeight ? 0.3f : 0.135f;
                    FrameUnit frameUnit = (FrameUnit) this.mFrameUnitPool.getObject();
                    frameUnit.c = f2 - lineData.lastHeight;
                    frameUnit.d = f6;
                    frameUnit.b = 0.0f;
                    frameUnit.t = 0.0f;
                    list.add(frameUnit);
                }
                lineData.lastHeight = f2;
            }
        }
    }

    public void drawVolume(Canvas canvas) {
        float f;
        Rect bounds = getBounds();
        for (int i = 0; i < this.mLineCount; i++) {
            LineData lineData = this.mDatas.get(i);
            List<FrameUnit> list = lineData.timelist;
            float f2 = 0.0f;
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 >= list.size()) {
                    break;
                }
                FrameUnit frameUnit = list.get(i3);
                frameUnit.t += 0.016f;
                if (frameUnit.t > frameUnit.d) {
                    float f3 = frameUnit.c - frameUnit.b;
                    this.mFrameUnitPool.returnObject(list.remove(i3));
                    i3--;
                    f = f3;
                } else {
                    float quartInOut = (float) quartInOut((double) frameUnit.t, 0.0d, (double) frameUnit.c, (double) frameUnit.d);
                    float f4 = frameUnit.b;
                    frameUnit.b = quartInOut;
                    f = quartInOut - f4;
                }
                f2 += f;
                i2 = i3 + 1;
            }
            if (f2 != 0.0f) {
                float f5 = ((float) lineData.height) + f2;
                if (f5 < ((float) this.mMinHeight)) {
                    f5 = (float) this.mMinHeight;
                }
                lineData.height = (int) f5;
            } else if (list.size() == 0 && ((float) lineData.height) < ((float) this.mMinHeight)) {
                lineData.height = this.mMinHeight;
            }
            this.mDrawRect.left = (float) (lineData.x + bounds.left);
            this.mDrawRect.top = (float) ((lineData.y - (lineData.height / 2)) + bounds.top);
            this.mDrawRect.right = this.mDrawRect.left + this.mLineWidth;
            this.mDrawRect.bottom = this.mDrawRect.top + ((float) lineData.height);
            canvas.drawRect(this.mDrawRect, this.mPaint);
        }
    }

    public void drawWait(Canvas canvas) {
        Rect bounds = getBounds();
        if (this.mIncrease) {
            for (int i = 0; i < this.mLineCount; i++) {
                LineData lineData = this.mDatas.get(i);
                this.mDrawRect.left = (float) (lineData.x + bounds.left);
                this.mDrawRect.right = ((float) lineData.x) + this.mLineWidth + ((float) bounds.left);
                if (i == this.mIndex - 1) {
                    this.mDrawRect.top = (float) ((lineData.y - ((this.mMinHeight * 3) / 2)) + bounds.top);
                    this.mDrawRect.bottom = this.mDrawRect.top + ((float) (this.mMinHeight * 3));
                } else if (i == this.mIndex) {
                    this.mDrawRect.top = (float) ((lineData.y - ((this.mMinHeight * 5) / 2)) + bounds.top);
                    this.mDrawRect.bottom = this.mDrawRect.top + ((float) (this.mMinHeight * 5));
                } else {
                    this.mDrawRect.top = (float) ((lineData.y - (this.mMinHeight / 2)) + bounds.top);
                    this.mDrawRect.bottom = this.mDrawRect.top + ((float) this.mMinHeight);
                }
                canvas.drawRoundRect(this.mDrawRect, 5.0f, 5.0f, this.mPaint);
            }
            this.mIndex += 2;
            if (this.mIndex >= this.mLineCount) {
                this.mIndex = this.mLineCount - 1;
                this.mIncrease = false;
                return;
            }
            return;
        }
        for (int i2 = 0; i2 < this.mLineCount; i2++) {
            LineData lineData2 = this.mDatas.get(i2);
            this.mDrawRect.left = (float) (lineData2.x + bounds.left);
            this.mDrawRect.right = ((float) lineData2.x) + this.mLineWidth + ((float) bounds.left);
            if (i2 == this.mIndex + 1) {
                this.mDrawRect.top = (float) ((lineData2.y - ((this.mMinHeight * 3) / 2)) + bounds.top);
                this.mDrawRect.bottom = this.mDrawRect.top + ((float) (this.mMinHeight * 3));
            } else if (i2 == this.mIndex) {
                this.mDrawRect.top = (float) ((lineData2.y - ((this.mMinHeight * 5) / 2)) + bounds.top);
                this.mDrawRect.bottom = this.mDrawRect.top + ((float) (this.mMinHeight * 5));
            } else {
                this.mDrawRect.top = (float) ((lineData2.y - (this.mMinHeight / 2)) + bounds.top);
                this.mDrawRect.bottom = this.mDrawRect.top + ((float) this.mMinHeight);
            }
            canvas.drawRoundRect(this.mDrawRect, 5.0f, 5.0f, this.mPaint);
        }
        this.mIndex -= 2;
        if (this.mIndex < 0) {
            this.mIndex = 0;
            this.mIncrease = true;
        }
    }

    static boolean randomBool(int i) {
        if (i <= 0) {
            return false;
        }
        return i >= 100 || getRandom() % 1000 < i * 10;
    }

    static double quartInOut(double d, double d2, double d3, double d4) {
        return (((-d3) / 2.0d) * (Math.cos((3.141592653589793d * d) / d4) - 1.0d)) + d2;
    }

    public static int getRandom() {
        if (random == null) {
            random = new Random(System.currentTimeMillis());
        }
        return Math.abs(random.nextInt());
    }

    public void run() {
        unscheduleSelf(this);
        if (this.mIsRunning) {
            invalidateSelf();
            if (this.mMode == 1) {
                scheduleSelf(this, SystemClock.uptimeMillis() + 16);
            } else if (this.mMode == 2) {
                scheduleSelf(this, SystemClock.uptimeMillis() + 50);
            }
        }
    }

    @Override // com.iflytek.inputmethod.common.view.widget.interfaces.ISwitcher
    public void start() {
        stop();
        this.mIsRunning = true;
        run();
    }

    @Override // com.iflytek.inputmethod.common.view.widget.interfaces.ISwitcher
    public void stop() {
        this.mIsRunning = false;
        this.mIncrease = false;
        this.mIndex = 0;
        unscheduleSelf(this);
    }

    public static class FrameUnit extends RecyclableObject {
        public float b;
        public float c;
        public float d;
        public float t;

        @Override // com.iflytek.inputmethod.common.objectpool.RecyclableObject
        public void doRecycle() {
        }
    }

    public static class FrameUnitPool extends ObjectPool<FrameUnit> {
        private int mCacheSize;

        public FrameUnitPool(int i) {
            this.mCacheSize = i;
        }

        /* access modifiers changed from: protected */
        @Override // com.iflytek.inputmethod.common.objectpool.ObjectPool
        public FrameUnit createNewObject() {
            return new FrameUnit();
        }

        @Override // com.iflytek.inputmethod.common.objectpool.ObjectPool
        public int getClearCnt() {
            return this.mCacheSize;
        }
    }

    public int getmLineWidth() {
        return (int) this.mLineWidth;
    }

    public List<LineData> getmDatas() {
        return this.mDatas;
    }

    public int getmLineCount() {
        return this.mLineCount;
    }

    public int getmMinHeight() {
        return this.mMinHeight;
    }

    public FrameUnitPool getmFrameUnitPool() {
        return this.mFrameUnitPool;
    }

    public int getmIndex() {
        return this.mIndex;
    }

    public void setmIndex(int i) {
        this.mIndex = i;
    }

    public void setmIncrease(boolean z) {
        this.mIncrease = z;
    }

    public boolean ismIncrease() {
        return this.mIncrease;
    }
}