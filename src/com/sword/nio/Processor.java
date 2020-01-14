package com.sword.nio;

/**
 * Created by Max on 2017/9/29.
 */
public interface Processor {
    public boolean init();
    public boolean process(Object value);
    public void quit();
}
