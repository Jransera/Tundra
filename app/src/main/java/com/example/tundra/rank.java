package com.example.tundra;

public class rank<L,R> {
    private L l;
    private R r;

    public rank(L l, R r){
        this.l = l;
        this.r = r;
    }

    public L getL() {
        return l;
    }

    public void setL(L l) {
        this.l = l;
    }

    public R getR() {
        return r;
    }

    public void setR(R r) {
        this.r = r;
    }
}
