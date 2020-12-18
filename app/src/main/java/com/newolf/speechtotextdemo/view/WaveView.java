/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2019. All rights reserved.
 */

package com.newolf.speechtotextdemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.newolf.speechtotextdemo.R;

import java.util.ArrayList;

/**
 * 语音录制页面声波动画view.
 *
 * @author NeWolf
 * @since 2020-11-27
 */
public class WaveView extends View {
    private ArrayList<Byte> datas = new ArrayList<>();

    private short max = 300;

    private float mWidth;

    private float mHeight;

    private float space = 2f;

    private Paint mWavePaint;

    private Paint baseLinePaint;

    private int mWaveColor = Color.GRAY;

    private int mBaseLineColor = Color.BLACK;

    private float waveStrokeWidth = 6f;

    private int invalidateTime = 1000 / 60;

    private long drawTime;

    private boolean isMaxConstant = false;

    public WaveView(Context context) {
        this(context, null);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.WaveView, defStyle, 0);
        mWaveColor = typedArray.getColor(R.styleable.WaveView_waveColor, mWaveColor);
        mBaseLineColor = typedArray.getColor(R.styleable.WaveView_baselineColor, mBaseLineColor);

        waveStrokeWidth = typedArray.getDimension(R.styleable.WaveView_waveStokeWidth, waveStrokeWidth);

        max = (short) typedArray.getInt(R.styleable.WaveView_maxValue, max);
        invalidateTime = typedArray.getInt(R.styleable.WaveView_invalidateTime, invalidateTime);

        space = typedArray.getDimension(R.styleable.WaveView_space, space);
        typedArray.recycle();
        initPainters();
    }

    private void initPainters() {
        mWavePaint = new Paint();
        mWavePaint.setColor(mWaveColor);
        mWavePaint.setStrokeWidth(waveStrokeWidth);
        mWavePaint.setAntiAlias(true);
        mWavePaint.setFilterBitmap(true);
        mWavePaint.setStrokeCap(Paint.Cap.ROUND);
        mWavePaint.setStyle(Paint.Style.FILL);

        baseLinePaint = new Paint();
        baseLinePaint.setColor(mBaseLineColor);
        baseLinePaint.setStrokeWidth(2f);
        baseLinePaint.setAntiAlias(true);
        baseLinePaint.setFilterBitmap(true);
        baseLinePaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 获取绘制最大数.
     *
     * @return short
     */
    public short getMax() {
        return max;
    }

    /**
     * 设置绘制最大数.
     *
     * @param max 最大数
     */

    public void setMax(short max) {
        this.max = max;
    }

    /**
     * 获取声波的间隔宽度.
     *
     * @return float
     */
    public float getSpace() {
        return space;
    }

    /**
     * 设置声波的间隔宽度.
     *
     * @param space float
     */
    public void setSpace(float space) {
        this.space = space;
    }

    /**
     * 获取声波的颜色.
     *
     * @return int 声波颜色
     */
    public int getWaveColor() {
        return mWaveColor;
    }

    /**
     * 设置声波的颜色.
     *
     * @param mWaveColor 声波颜色
     */

    public void setWaveColor(int mWaveColor) {
        this.mWaveColor = mWaveColor;
        invalidateNow();
    }

    /**
     * 获取中间基准线的颜色 int值.
     *
     * @return int
     */
    public int getBaseLineColor() {
        return mBaseLineColor;
    }

    /**
     * 设置中间基准线的颜色 int值.
     *
     * @param mBaseLineColor int
     */
    public void setBaseLineColor(int mBaseLineColor) {
        this.mBaseLineColor = mBaseLineColor;
        invalidateNow();
    }

    /**
     * 获取声波的宽度.
     *
     * @return float
     */
    public float getWaveStrokeWidth() {
        return waveStrokeWidth;
    }

    /**
     * 设置声波的宽度.
     *
     * @param waveStrokeWidth 声波条宽度
     */
    public void setWaveStrokeWidth(float waveStrokeWidth) {
        this.waveStrokeWidth = waveStrokeWidth;
        invalidateNow();
    }

    /**
     * 获取刷新间隔时间.
     *
     * @return int ms
     */
    public int getInvalidateTime() {
        return invalidateTime;
    }

    /**
     * 设置刷新间隔时间.
     *
     * @param invalidateTime int ms
     */
    public void setInvalidateTime(int invalidateTime) {
        this.invalidateTime = invalidateTime;
    }

    /**
     * 是否已达到数据最大包容值.
     *
     * @return boolean
     */
    public boolean isMaxConstant() {
        return isMaxConstant;
    }

    /**
     * 设置数据最大包容值.
     *
     * @param maxConstant 数据最大值
     */
    public void setMaxConstant(boolean maxConstant) {
        isMaxConstant = maxConstant;
    }

    /**
     * 如果改变相应配置 需要刷新相应的paint设置.
     */
    public void invalidateNow() {
        initPainters();
        invalidate();
    }

    /**
     * 添加数据.
     *
     * @param data 数据字节
     */
    public void addData(byte data) {
        if (data < 0) {
            data = (byte) -data;
        }
        if (data > max && !isMaxConstant) {
            max = data;
        }
        if (datas.size() > mWidth / space) {
            synchronized (this) {
                datas.remove(0);
                datas.add(data);
            }
        } else {
            datas.add(data);
        }
        if (System.currentTimeMillis() - drawTime > invalidateTime) {
            invalidate();
            drawTime = System.currentTimeMillis();
        }
    }

    /**
     * 清除数据.
     */
    public void clear() {
        datas.clear();
        invalidateNow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(0, mHeight / 2);
        drawWave(canvas);
    }

    @Override
    protected void onSizeChanged(int wth, int hth, int oldw, int oldh) {
        mWidth = wth;
        mHeight = hth;
    }

    private void drawWave(Canvas mCanvas) {
        for (int i = 0; i < datas.size(); i++) {
            float x = (i) * space;
            float y = (float) datas.get(i) / max * mHeight / 2;
            mCanvas.drawLine(x, -y, x, y, mWavePaint);
        }
    }

    private void drawBaseLine(Canvas mCanvas) {
        mCanvas.drawLine(0, 0, mWidth, 0, baseLinePaint);
    }
}
