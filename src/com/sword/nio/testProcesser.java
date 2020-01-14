package com.sword.nio;

/**
 * Created by Max on 2017/10/16.
 */
public class testProcesser implements Processor {
    @Override
    public boolean init() {
        return true;
    }

    @Override
    public boolean process(Object value){
        System.out.println("mytest queue msg : "+value.toString());
        return true;
    }

    @Override
    public void quit() {
    }
}
