package com.newolf.volumelib;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpeechDrawable extends Drawable implements Runnable {

    private static final String TAG = SpeechDrawable.class.getSimpleName();
    private static final double DOUBLE_PARSE = 0.05000000074505806d;
    static Random random;
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
        public List<FrameUnit> timeList;
        public int width;
        public int x;
        public int y;
    }

    public SpeechDrawable() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(mNormalColor);
        mDrawRect = new RectF();
        mRandomCaches = new SparseArray<>();
        mHeightCaches = new SparseArray<>();
    }


    public int getNormalColor() {
        return mNormalColor;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(getBounds());
        if (mMode == 1) {
            if (BuildConfig.DEBUG) {
                long currentTimeMillis = System.currentTimeMillis();
                drawVolume(canvas);
                Log.d(TAG, "draw volume: " + (System.currentTimeMillis() - currentTimeMillis) + " ms");
            } else {
                drawVolume(canvas);
            }
        } else if (mMode == 2) {
            if (BuildConfig.DEBUG) {
                long currentTimeMillis2 = System.currentTimeMillis();
                drawWait(canvas);
                Log.d(TAG, "draw wait: " + (System.currentTimeMillis() - currentTimeMillis2) + " ms");
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

    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setBounds(int i, int i2, int i3, int i4) {
        super.setBounds(i, i2, i3, i4);
        if (i3 - i != mViewWidth || i4 - i2 != mViewHeight) {
            mViewWidth = i3 - i;
            mViewHeight = i4 - i2;
            mInited = false;
            init();
        }
    }

    public SpeechDrawable setMode(int i) {
        mMode = i;
        if (i == 2) {
            mIndex = 0;
            mIncrease = true;
        }
        return this;
    }

    public void setStepWidth(int i) {
        mStepWidth = i;
    }

    public void setLineWidth(int i) {
        mLineWidth = (float) i;
    }

    public void setMinHeight(int i) {
        mMinHeight = i;
    }

    private void init() {
        if (!mInited) {
            mLineCount = mViewWidth / (((int) mLineWidth) + mStepWidth);
            mFrameUnitPool = new FrameUnitPool(mLineCount + 1);
            mDenominator = Math.pow(mLineCount, 4);
            int i = (mViewWidth - ((((int) mLineWidth) + mStepWidth) * mLineCount)) / 2;
            mDatas = new ArrayList<>();
            for (int i2 = 0; i2 < mLineCount; i2++) {
                LineData lineData = new LineData();
                lineData.x = i;
                lineData.y = mViewHeight / 2;
                lineData.width = (int) mLineWidth;
                lineData.height = mMinHeight;
                lineData.timeList = new ArrayList<>();
                mDatas.add(lineData);
                i = (int) (((float) i) + mLineWidth + ((float) mStepWidth));
            }
            mSv = new boolean[mLineCount];
            int i3 = mLineCount;
            if (i3 < 62) {
                i3 = 62;
            }
            mEh = new float[i3];
            mLoc = new int[i3];
            mInited = true;
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "init w: " + mLineWidth + " h: " + mMinHeight + " s: " + mStepWidth);
            }
        }
    }

    private double getRandomValue(int i) {
        if (mRandomCaches.indexOfKey(i) >= 0) {
            return mRandomCaches.get(i);
        }
        double pow = 18.0d * Math.pow((double) i, 4.0d);
        mRandomCaches.put(i, pow);
        return pow;
    }

    private double getHeightValue(int i) {
        if (mHeightCaches.indexOfKey(i) >= 0) {
            return mHeightCaches.get(i);
        }
        double pow = Math.pow(0.5d, (double) i);
        mHeightCaches.put(i, pow);
        return pow;
    }

    public void disable() {
        if (mEnable) {
            mEnable = false;
            init();
            mPaint.setColor(mDisableColor);
            for (int i = 0; i < mDatas.size(); i++) {
                mDatas.get(i).height = mMinHeight;
            }
        }
    }

    public void reset() {
        if (mMode == 1) {
            init();
            if (!mEnable) {
                mEnable = true;
                mPaint.setColor(mNormalColor);
            }
            for (LineData lineData : mDatas) {
                lineData.timeList.clear();
                lineData.height = mMinHeight;
            }
        }
    }

    public void setVolume(int i) {
        float f;
        float f2;
        if (mMode == 1) {
            init();
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "vol: " + i);
            }
            if (!mEnable) {
                mEnable = true;
                mPaint.setColor(mNormalColor);
            }
            float f3 = (float) (((double) i) / 8.0d);
            if (f3 > 0.6f) {
                f3 = 0.6f;
            }
            float f4 = (float) (mLineCount / 2);
            if (f3 >= 0.1f) {
                f = 2.0f + f3;
                mLowMode = 0;
            } else if (mLowMode <= 0 || mLowMode >= 3) {
                mLowMode = 0;
                mLowMode++;
                f3 = 0.1f;
                f = 0.05f;
            } else {
                mLowMode++;
                return;
            }
            for (int i2 = 0; i2 < mLineCount; i2++) {
                if (((float) i2) < f4) {
                    mSv[i2] = randomBool((int) (((double) f3) * ((getRandomValue(i2) / mDenominator) + DOUBLE_PARSE) * 100.0d));
                } else {
                    mSv[i2] = randomBool((int) (((double) f3) * ((getRandomValue(i2 - mLineCount) / mDenominator) + DOUBLE_PARSE) * 100.0d));
                }
            }
            int random2 = (((int) (20.0f * f)) + (getRandom() % 3)) - 1;
            if (random2 <= 0) {
                random2 = 1;
            }
            float pow = 0.8f * ((float) Math.pow((double) f, 0.3330000042915344d)) * ((float) mViewHeight);
            for (int i3 = 0; i3 < random2; i3++) {
                if (((float) i3) < f4) {
                    mEh[i3] = (float) (((double) pow) * ((getRandomValue(i3) / mDenominator) + DOUBLE_PARSE) * 10.0d);
                } else {
                    mEh[i3] = (float) (((double) pow) * ((getRandomValue(i3 - mLineCount) / mDenominator) + DOUBLE_PARSE) * 10.0d);
                }
            }
            int i4 = 0;
            for (int i5 = 0; i5 < mLineCount; i5++) {
                if (mSv[i5]) {
                    mLoc[i4] = i5;
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
                    int i7 = mLoc[random3];
                    mLoc[random3] = mLoc[random4];
                    mLoc[random4] = i7;
                }
            }
            int min2 = Math.min(random2, i4);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "factm: " + min2);
            }
            for (int i8 = 0; i8 < mLineCount; i8++) {
                float f5 = (float) mMinHeight;
                for (int i9 = 0; i9 < min2; i9++) {
                    f5 = (float) (((double) f5) + (((double) mEh[i9]) * getHeightValue(Math.abs(mLoc[i9] - i8))));
                }
                if (f5 > ((float) mViewHeight)) {
                    f2 = (float) mViewHeight;
                } else {
                    f2 = f5;
                }
                LineData lineData = mDatas.get(i8);
                List<FrameUnit> list = lineData.timeList;
                if (Math.abs(f2 - lineData.lastHeight) >= 1.0f) {
                    float f6 = f2 < lineData.lastHeight ? 0.3f : 0.135f;
                    FrameUnit frameUnit = (FrameUnit) mFrameUnitPool.getObject();
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
        for (int i = 0; i < mLineCount; i++) {
            LineData lineData = mDatas.get(i);
            List<FrameUnit> list = lineData.timeList;
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
                    mFrameUnitPool.returnObject(list.remove(i3));
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
                if (f5 < ((float) mMinHeight)) {
                    f5 = (float) mMinHeight;
                }
                lineData.height = (int) f5;
            } else if (list.size() == 0 && ((float) lineData.height) < ((float) mMinHeight)) {
                lineData.height = mMinHeight;
            }
            mDrawRect.left = (float) (lineData.x + bounds.left);
            mDrawRect.top = (float) ((lineData.y - (lineData.height / 2)) + bounds.top);
            mDrawRect.right = mDrawRect.left + mLineWidth;
            mDrawRect.bottom = mDrawRect.top + ((float) lineData.height);
            canvas.drawRect(mDrawRect, mPaint);
        }
    }

    public void drawWait(Canvas canvas) {
        Rect bounds = getBounds();
        if (mIncrease) {
            for (int i = 0; i < mLineCount; i++) {
                LineData lineData = mDatas.get(i);
                mDrawRect.left = (float) (lineData.x + bounds.left);
                mDrawRect.right = ((float) lineData.x) + mLineWidth + ((float) bounds.left);
                if (i == mIndex - 1) {
                    mDrawRect.top = (float) ((lineData.y - ((mMinHeight * 3) / 2)) + bounds.top);
                    mDrawRect.bottom = mDrawRect.top + ((float) (mMinHeight * 3));
                } else if (i == mIndex) {
                    mDrawRect.top = (float) ((lineData.y - ((mMinHeight * 5) / 2)) + bounds.top);
                    mDrawRect.bottom = mDrawRect.top + ((float) (mMinHeight * 5));
                } else {
                    mDrawRect.top = (float) ((lineData.y - (mMinHeight / 2)) + bounds.top);
                    mDrawRect.bottom = mDrawRect.top + ((float) mMinHeight);
                }
                canvas.drawRoundRect(mDrawRect, 5.0f, 5.0f, mPaint);
            }
            mIndex += 2;
            if (mIndex >= mLineCount) {
                mIndex = mLineCount - 1;
                mIncrease = false;
                return;
            }
            return;
        }
        for (int i2 = 0; i2 < mLineCount; i2++) {
            LineData lineData2 = mDatas.get(i2);
            mDrawRect.left = (float) (lineData2.x + bounds.left);
            mDrawRect.right = ((float) lineData2.x) + mLineWidth + ((float) bounds.left);
            if (i2 == mIndex + 1) {
                mDrawRect.top = (float) ((lineData2.y - ((mMinHeight * 3) / 2)) + bounds.top);
                mDrawRect.bottom = mDrawRect.top + ((float) (mMinHeight * 3));
            } else if (i2 == mIndex) {
                mDrawRect.top = (float) ((lineData2.y - ((mMinHeight * 5) / 2)) + bounds.top);
                mDrawRect.bottom = mDrawRect.top + ((float) (mMinHeight * 5));
            } else {
                mDrawRect.top = (float) ((lineData2.y - (mMinHeight / 2)) + bounds.top);
                mDrawRect.bottom = mDrawRect.top + ((float) mMinHeight);
            }
            canvas.drawRoundRect(mDrawRect, 5.0f, 5.0f, mPaint);
        }
        mIndex -= 2;
        if (mIndex < 0) {
            mIndex = 0;
            mIncrease = true;
        }
    }

    static boolean randomBool(int i) {
        if (i <= 0) {
            return false;
        }
        return i >= 100 || getRandom() % 1000 < i * 10;
    }

    static double quartInOut(double d, double d2, double d3, double d4) {
        return -d3 / 2 * (Math.cos((Math.PI * d) / d4) - 1) + d2;
    }

    public static int getRandom() {
        if (random == null) {
            random = new Random(System.currentTimeMillis());
        }
        return Math.abs(random.nextInt());
    }

    public void run() {
        unscheduleSelf(this);
        if (mIsRunning) {
            invalidateSelf();
            if (mMode == 1) {
                scheduleSelf(this, SystemClock.uptimeMillis() + 16);
            } else if (mMode == 2) {
                scheduleSelf(this, SystemClock.uptimeMillis() + 50);
            }
        }
    }

    public void start() {
        stop();
        mIsRunning = true;
        run();
    }

    public void stop() {
        mIsRunning = false;
        mIncrease = false;
        mIndex = 0;
        unscheduleSelf(this);
    }

    public static class FrameUnit extends RecyclableObject {
        public float b;
        public float c;
        public float d;
        public float t;

        @Override
        public void doRecycle() {
        }
    }

    public static class FrameUnitPool extends ObjectPool<FrameUnit> {
        private int mCacheSize;

        public FrameUnitPool(int i) {
            mCacheSize = i;
        }


        @Override
        public FrameUnit createNewObject() {
            return new FrameUnit();
        }

        @Override
        public int getClearCnt() {
            return mCacheSize;
        }
    }

    public int getLineWidth() {
        return (int) mLineWidth;
    }

    public List<LineData> getDatas() {
        return mDatas;
    }

    public int getLineCount() {
        return mLineCount;
    }

    public int getMinHeight() {
        return mMinHeight;
    }

    public FrameUnitPool getFrameUnitPool() {
        return mFrameUnitPool;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int i) {
        mIndex = i;
    }

    public void setIncrease(boolean z) {
        mIncrease = z;
    }

    public boolean isIncrease() {
        return mIncrease;
    }
}