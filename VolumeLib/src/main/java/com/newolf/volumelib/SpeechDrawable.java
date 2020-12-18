package com.newolf.volumelib;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 语音动画
 *
 * @author NeWolf
 * @since 2020-12-16
 */
public class SpeechDrawable extends Drawable implements Runnable {
    private static final float DRAW_INTERVAL = 0.016f;
    private static final double DOUBLE_PARSE = 0.05000000074505806d;
    private static final double EXPONENT = 0.3330000042915344d;
    private static final int MIN_BUF_SIZE = 62;
    private static final int POW_INT = 18;
    private static final float MAX_TEMP_VOL = 0.6f;
    private static final int POW_FOUR = 4;
    private static final double POW_HALF = 0.5;
    private static final int INT_TEN = 10;
    private static final int INT_HUNDRED = 100;
    private static final double DOUBLE_TEMP_VOL = 8.0d;
    private static final float FLOAT_MIN_VOL = 0.1f;
    private static final float FLOAT_TOW = 2.0f;
    private static final float TEMP_F = 0.05f;
    private static final float FLOAT_RANDOM = 20.0f;
    private static final float FLOAT_POW = 0.8f;
    private static final int INT_MIN = 30;
    private static final float FLOAT_LINE = 0.3f;
    private static final float FLOAT_LINE_MIN = 0.135f;
    private static final int NORMAL_COLOR = Color.BLUE;
    private static Random sRandom;
    private List<LineData> mDatas;
    private double mDenominator;
    private RectF mDrawRect;
    private float[] mEh;
    private boolean mEnable = true;
    private FrameUnitPool mFrameUnitPool;
    private SparseArray<Double> mHeightCaches;
    private boolean mInited;
    private boolean mIsRunning;
    private int mLineCount;
    private int mLineWidth;
    private int[] mLoc;
    private int mLowMode;
    private int mMinHeight;
    private int mLineColor = NORMAL_COLOR;
    private Paint mPaint = new Paint();
    private SparseArray<Double> mRandomCaches;
    private int mStepWidth;
    private boolean[] mSv;
    private int mViewHeight;
    private int mViewWidth;


    public SpeechDrawable() {
        mPaint.setAntiAlias(true);
        mPaint.setColor(mLineColor);
        mDrawRect = new RectF();
        mRandomCaches = new SparseArray<>();
        mHeightCaches = new SparseArray<>();
    }

    /**
     * 设置声波线颜色
     *
     * @param color int
     */
    public void setLineColor(int color) {
        mLineColor = color;
        invalidateSelf();
    }

    /**
     * 设置声波线间隔
     *
     * @param stepWidth int
     */
    public void setStepWidth(int stepWidth) {
        mStepWidth = stepWidth;
    }

    /**
     * 设置声波线宽度
     *
     * @param lineWidth int
     */
    public void setLineWidth(int lineWidth) {
        mLineWidth = lineWidth;
    }

    /**
     * 设置最小高度
     *
     * @param minHeight int
     */
    public void setMinHeight(int minHeight) {
        mMinHeight = minHeight;
    }

    public int getLineWidth() {
        return mLineWidth;
    }


    public int getLineCount() {
        return mLineCount;
    }

    public int getMinHeight() {
        return mMinHeight;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.clipRect(getBounds());
        drawVolume(canvas);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        if (right - left != mViewWidth || bottom - top != mViewHeight) {
            mViewWidth = right - left;
            mViewHeight = bottom - top;
            mInited = false;
            init();
        }
    }


    private void init() {
        if (!mInited) {
            mLineCount = mViewWidth / (mLineWidth + mStepWidth);
            mFrameUnitPool = new FrameUnitPool(mLineCount + 1);
            mDenominator = Math.pow(mLineCount, POW_FOUR);
            int startLeft = (mViewWidth - (mLineWidth + mStepWidth) * mLineCount) / 2;
            mDatas = new ArrayList<>();
            for (int i = 0; i < mLineCount; i++) {
                LineData lineData = new LineData();
                lineData.Left = startLeft;
                lineData.centerHeight = mViewHeight / 2;
                lineData.width = mLineWidth;
                lineData.height = mMinHeight;
                lineData.timeList = new ArrayList<>();
                mDatas.add(lineData);
                startLeft = startLeft + mLineWidth + mStepWidth;
            }
            mSv = new boolean[mLineCount];
            int count = mLineCount;
            if (count < MIN_BUF_SIZE) {
                count = MIN_BUF_SIZE;
            }
            mEh = new float[count];
            mLoc = new int[count];
            mInited = true;

        }
    }

    private double getRandomValue(int value) {
        if (mRandomCaches.indexOfKey(value) >= 0) {
            return mRandomCaches.get(value);
        }
        double pow = POW_INT * Math.pow(value, POW_FOUR);
        mRandomCaches.put(value, pow);
        return pow;
    }

    private double getHeightValue(int i) {
        if (mHeightCaches.indexOfKey(i) >= 0) {
            return mHeightCaches.get(i);
        }
        double pow = Math.pow(POW_HALF, i);
        mHeightCaches.put(i, pow);
        return pow;
    }

    /**
     * 重置为默认状态
     */
    public void reset() {
        init();
        if (!mEnable) {
            mEnable = true;
            mPaint.setColor(NORMAL_COLOR);
        }
        for (LineData lineData : mDatas) {
            lineData.timeList.clear();
            lineData.height = mMinHeight;
        }
    }

    /**
     * 设置声音参数
     *
     * @param volume int
     */
    public void setVolume(int volume) {
        float tempF;
        float tempF2;
        init();
        if (!mEnable) {
            mEnable = true;
            mPaint.setColor(mLineColor);
        }
        float tempVolume = (float) (volume / DOUBLE_TEMP_VOL);
        if (tempVolume > MAX_TEMP_VOL) {
            tempVolume = MAX_TEMP_VOL;
        }
        int halfLineCount = mLineCount / 2;
        if (tempVolume >= FLOAT_MIN_VOL) {
            tempF = FLOAT_TOW + tempVolume;
            mLowMode = 0;
        } else if (mLowMode <= 0 || mLowMode >= 3) {
            mLowMode = 0;
            mLowMode++;
            tempVolume = FLOAT_MIN_VOL;
            tempF = TEMP_F;
        } else {
            mLowMode++;
            return;
        }
        for (int i = 0; i < mLineCount; i++) {
            if (i < halfLineCount) {
                mSv[i] = randomBool((int) (tempVolume * ((getRandomValue(i) / mDenominator) + DOUBLE_PARSE) * INT_HUNDRED));

            } else {
                mSv[i] = randomBool((int) (tempVolume * ((getRandomValue(i - mLineCount) / mDenominator) + DOUBLE_PARSE) * INT_HUNDRED));
            }
        }
        int random2 = (int) (FLOAT_RANDOM * tempF) + (getsRandom() % 3) - 1;
        if (random2 <= 0) {
            random2 = 1;
        }
        float pow = FLOAT_POW * (float) Math.pow(tempF, EXPONENT) * mViewHeight;
        for (int i = 0; i < random2; i++) {
            if (((float) i) < halfLineCount) {
                mEh[i] = (float) (pow * ((getRandomValue(i) / mDenominator) + DOUBLE_PARSE) * INT_TEN);
            } else {
                mEh[i] = (float) (pow * ((getRandomValue(i - mLineCount) / mDenominator) + DOUBLE_PARSE) * INT_TEN);
            }
        }
        int tempNum = 0;
        for (int i = 0; i < mLineCount; i++) {
            if (mSv[i]) {
                mLoc[tempNum] = i;
                tempNum++;
            }
        }
        if (tempNum > 1) {
            int min = Math.min(INT_MIN, tempNum);
            for (int i = 0; i < min; i++) {
                int random3 = getsRandom() % tempNum;
                int random4 = getsRandom() % tempNum;
                if (random3 == random4) {
                    random4 = (random4 + 1) % tempNum;
                }
                int temp = mLoc[random3];
                mLoc[random3] = mLoc[random4];
                mLoc[random4] = temp;
            }
        }
        int min2 = Math.min(random2, tempNum);

        for (int i = 0; i < mLineCount; i++) {
            float minHeight = mMinHeight;
            for (int j = 0; j < min2; j++) {
                minHeight = (float) (minHeight + (mEh[j] * getHeightValue(Math.abs(mLoc[j] - i))));
            }
            if (minHeight > mViewHeight) {
                tempF2 = mViewHeight;
            } else {
                tempF2 = minHeight;
            }
            LineData lineData = mDatas.get(i);
            List<FrameUnit> list = lineData.timeList;
            if (Math.abs(tempF2 - lineData.lastHeight) >= 1) {
                float tempD = tempF2 < lineData.lastHeight ? FLOAT_LINE : FLOAT_LINE_MIN;
                FrameUnit frameUnit = mFrameUnitPool.getObject();
                frameUnit.center = tempF2 - lineData.lastHeight;
                frameUnit.duration = tempD;
                frameUnit.bound = 0.0f;
                frameUnit.time = 0.0f;
                list.add(frameUnit);
            }
            lineData.lastHeight = tempF2;
        }
    }

    public void drawVolume(Canvas canvas) {
        float tempLeft;
        Rect bounds = getBounds();
        for (int i = 0; i < mLineCount; i++) {
            LineData lineData = mDatas.get(i);
            List<FrameUnit> list = lineData.timeList;
            float quart = 0.0f;
            int tempInt = 0;
            while (true) {
                int sizeInt = tempInt;
                if (sizeInt >= list.size()) {
                    break;
                }
                FrameUnit frameUnit = list.get(sizeInt);
                frameUnit.time += DRAW_INTERVAL;
                if (frameUnit.time > frameUnit.duration) {
                    float tempFrame = frameUnit.center - frameUnit.bound;
                    mFrameUnitPool.returnObject(list.remove(sizeInt));
                    sizeInt--;
                    tempLeft = tempFrame;
                } else {
                    float quartInOut = (float) quartInOut(frameUnit.time, 0.0d, frameUnit.center, frameUnit.duration);
                    float bound = frameUnit.bound;
                    frameUnit.bound = quartInOut;
                    tempLeft = quartInOut - bound;
                }
                quart += tempLeft;
                tempInt = sizeInt + 1;
            }
            if (quart != 0.0f) {
                float height = ((float) lineData.height) + quart;
                if (height < ((float) mMinHeight)) {
                    height = (float) mMinHeight;
                }
                lineData.height = (int) height;
            } else if (list.size() == 0 && ((float) lineData.height) < ((float) mMinHeight)) {
                lineData.height = mMinHeight;
            }
            mDrawRect.left = (float) (lineData.Left + bounds.left);
            mDrawRect.top = (float) ((lineData.centerHeight - (lineData.height / 2)) + bounds.top);
            mDrawRect.right = mDrawRect.left + mLineWidth;
            mDrawRect.bottom = mDrawRect.top + lineData.height;
            canvas.drawRect(mDrawRect, mPaint);
        }
    }


    static boolean randomBool(int value) {
        if (value <= 0) {
            return false;
        }
        return value >= INT_HUNDRED || getsRandom() % 1000 < value * INT_TEN;
    }

    static double quartInOut(double doubleA, double doubleB, double doubleC, double doubleD) {
        return -doubleC / 2 * (Math.cos((Math.PI * doubleA) / doubleD) - 1) + doubleB;
    }

    private static int getsRandom() {
        if (sRandom == null) {
            sRandom = new Random(System.currentTimeMillis());
        }
        return Math.abs(sRandom.nextInt());
    }

    @Override
    public void run() {
        unscheduleSelf(this);
        if (mIsRunning) {
            invalidateSelf();
            scheduleSelf(this, SystemClock.uptimeMillis() + 16);
        }
    }

    /**
     *
     * 开始绘制
     */
    public void start() {
        stop();
        mIsRunning = true;
        run();
    }

    /**
     * 停止绘制
     */
    public void stop() {
        mIsRunning = false;
        unscheduleSelf(this);
    }

    private static class FrameUnit extends RecyclableObject {
        public float bound;
        public float center;
        public float duration;
        public float time;

        @Override
        public void doRecycle() {
        }
    }

    private static class FrameUnitPool extends ObjectPool<FrameUnit> {
        private int mCacheSize;

        public FrameUnitPool(int cacheSize) {
            mCacheSize = cacheSize;
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


    private static class LineData {
        public int height;
        public float lastHeight;
        public List<FrameUnit> timeList;
        public int width;
        public int Left;
        public int centerHeight;
    }
}