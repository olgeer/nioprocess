package com.sword.nio;

/**
 * Created by Max on 2017/10/25.
 */
public class Config {
    private String commandQueue = "CMD_QUEUE";
    private boolean writeToFile = false;     //缓存是否写入文件
    private int writeInterval = 360;        //写文件间隔，单位：秒
    private String cacheDataPath = "/usr/local/itsm/cache/nio.dat";   //缓存文件存放路径
    private int interval = 5;            //检查间隔，单位：秒
    private int sleepInterval = 50;     //每处理休眠时间间隔，单位：毫秒

    public String getCommandQueue() {
        return commandQueue;
    }

    public void setCommandQueue(String commandQueue) {
        this.commandQueue = commandQueue;
    }

    public boolean isWriteToFile() {
        return writeToFile;
    }

    public void setWriteToFile(boolean writeToFile) {
        this.writeToFile = writeToFile;
    }

    public int getWriteInterval() {
        return writeInterval;
    }

    public void setWriteInterval(int writeInterval) {
        this.writeInterval = writeInterval;
    }

    public String getCacheDataPath() {
        return cacheDataPath;
    }

    public void setCacheDataPath(String cacheDataPath) {
        this.cacheDataPath = cacheDataPath;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getSleepInterval() {
        return sleepInterval;
    }

    public void setSleepInterval(int sleepInterval) {
        this.sleepInterval = sleepInterval;
    }
}
