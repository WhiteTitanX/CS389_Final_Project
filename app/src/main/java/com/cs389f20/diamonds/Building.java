package com.cs389f20.diamonds;

public class Building implements java.io.Serializable {
    public String name;
    public Property property;
    public int numOfDetectors, currentNumberOfPeople;
    public int[] pastNumberOfPeople;
    public String[] timestamps; //parallel array with pastNumberOfPeople
    //Location?

    public Building(String buildingName, Property _p, int detectors, int currentNum, int[] past, String[] _timestamps) {
        name = buildingName;
        property = _p;
        numOfDetectors = detectors;
        currentNumberOfPeople = currentNum;
        pastNumberOfPeople = past;
        timestamps = _timestamps;
    }

    public void setPeople(int newpeople) {
        currentNumberOfPeople = newpeople;
    }

    public void setPastPeople(int[] past, String[] times) {
        pastNumberOfPeople = past;
        timestamps = times;
    }
}
