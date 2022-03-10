package com.example.tundra;

import java.io.Serializable;

public class userData implements Serializable {
    private int Rank;
    private long totalTime;
    private long avg;
    private long latest;
    private float succRate;
    private int numSessions;
    private int numTries;
    private int ID;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getRank() {
        return Rank;
    }

    public void setRank(int rank) {
        Rank = rank;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public long getAvg() {
        return avg;
    }

    public void setAvg(long avg) {
        this.avg = avg;
    }

    public long getLatest() {
        return latest;
    }

    public void setLatest(long latest) {
        this.latest = latest;
    }

    public float getSuccRate() {
        return succRate;
    }

    public void setSuccRate(float succRate) {
        this.succRate = succRate;
    }

    public int getNumSessions() {
        return numSessions;
    }

    public void setNumSessions(int numSessions) {
        this.numSessions = numSessions;
    }

    public int getNumTries() {
        return numTries;
    }

    public void setNumTries(int numTries) {
        this.numTries = numTries;
    }

    @Override
    public String toString() {
        return "userData{" +
                "Rank=" + Rank +
                ", totalTime=" + totalTime +
                ", avg=" + avg +
                ", latest=" + latest +
                ", succRate=" + succRate +
                ", numSessions=" + numSessions +
                ", numTries=" + numTries +
                ", ID=" +ID+
                '}';
    }

    public String publish(){
        return (Rank + "," + totalTime +
                "," + avg +
                "," + latest +
                "," + succRate +
                "," + numSessions +
                "," + numTries +
                "," + ID);

    }
}
