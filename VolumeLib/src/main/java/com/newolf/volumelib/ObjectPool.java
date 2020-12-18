package com.newolf.volumelib;

import java.util.LinkedList;

/**
 * 回收对象复用 pool
 *
 * @author NeWolf
 * @since 2020-12-16
 */
public abstract class ObjectPool<T extends RecyclableObject> {
    private int mCurrentSize = 0;
    private LinkedList<T> mPoolList = new LinkedList<>();
    private final Object mSync = new Object();

    protected abstract T createNewObject();

    protected abstract int getClearCnt();

    /**
     * 获取对象
     * @return T
     */
    public T getObject() {
        T createNewObject;
        synchronized (mSync) {
            if (mCurrentSize > 0) {
                createNewObject = mPoolList.removeFirst();
                mCurrentSize--;
                createNewObject.onObtain();
            } else {
                createNewObject = createNewObject();
                createNewObject.onObtain();
            }
        }
        return createNewObject;
    }

    /**
     * 返回对象
     * @param obj T
     */
    public void returnObject(T obj) {
        synchronized (mSync) {
            if (mCurrentSize < getClearCnt()) {
                if (obj != null && !obj.isRecycled()) {
                    obj.recycle();
                    mPoolList.addFirst(obj);
                    mCurrentSize++;
                }
            }
        }
    }

    /**
     * 释放所有对象
     */
    public void release() {
        synchronized (mSync) {
            mCurrentSize = 0;
            mPoolList.clear();
        }
    }
}
