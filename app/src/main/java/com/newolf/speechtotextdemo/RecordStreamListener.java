package com.newolf.speechtotextdemo;


/**
 * 功能描述
 *
 * @author NeWolf
 * @since 2020-11-20
 */
public interface RecordStreamListener {
    void recordOfByte(byte[] audioData, int begin, int end);
}
