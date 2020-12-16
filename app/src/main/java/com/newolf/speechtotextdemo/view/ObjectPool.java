package com.newolf.speechtotextdemo.view;

import java.util.LinkedList;

/**
 * 功能描述
 *
 * @author NeWolf
 * @since 2020-12-16
 */
public abstract class ObjectPool<O extends RecyclableObject> {
    private int mCurrentSize = 0;
    private LinkedList<O> mPoolList = new LinkedList<>();
    private Object mSync = new Object();

    protected abstract O createNewObject();

    protected abstract int getClearCnt();

    public O getObject() {
        O createNewObject;
        synchronized (this.mSync) {
            if (this.mCurrentSize > 0) {
                createNewObject = this.mPoolList.removeFirst();
                this.mCurrentSize--;
                createNewObject.onObtain();
            } else {
                createNewObject = createNewObject();
                createNewObject.onObtain();
            }
        }
        return createNewObject;
    }

    public void returnObject(O o) {
        synchronized (this.mSync) {
            if (this.mCurrentSize < getClearCnt()) {
                if (o != null && !o.isRecycled()) {
                    o.recycle();
                    this.mPoolList.addFirst(o);
                    this.mCurrentSize++;
                }
            }
        }
    }

    public void release() {
        synchronized (this.mSync) {
            this.mCurrentSize = 0;
            this.mPoolList.clear();
        }
    }
}
