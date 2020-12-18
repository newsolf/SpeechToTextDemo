package com.newolf.volumelib;

/**
 * 可回收的对象
 *
 * @author NeWolf
 * @since 2020-12-16
 */
public abstract class RecyclableObject {
    private volatile boolean mIsRecycled;

    protected abstract void doRecycle();

    void recycle() {
        this.mIsRecycled = true;
        doRecycle();
    }

    boolean isRecycled() {
        return this.mIsRecycled;
    }

    void onObtain() {
        this.mIsRecycled = false;
    }
}
