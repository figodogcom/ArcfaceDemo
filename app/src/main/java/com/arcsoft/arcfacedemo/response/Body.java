package com.arcsoft.arcfacedemo.response;

public class Body<T> {
    public int code;
    public String msg;
    public String time;

    @Override
    public String toString() {
        return "Body{" +
                "data=" + data +
                '}';
    }

    public T data;


}
