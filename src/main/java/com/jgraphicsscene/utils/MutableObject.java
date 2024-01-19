package com.jgraphicsscene.utils;

public class MutableObject<T> {
    public T v;

    public MutableObject(T v) {
        this.v = v;
    }

    public T getV() {
        return v;
    }

    public void setV(T v) {
        this.v = v;
    }

    public void exchange(MutableObject<T> obj) {
        T temp = this.v;
        this.v = obj.v;
        obj.v = temp;
    }
}
