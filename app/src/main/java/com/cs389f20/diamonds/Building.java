package com.cs389f20.diamonds;

public class Building implements java.io.Serializable
{
    public String name;
    public Property property;
    public int numOfDetectors, currentNumberOfPeople;
    //Location?

    public Building(String buildingName, Property _p, int detectors, int currentNum)
    {
        name = buildingName;
        property = _p;
        numOfDetectors = detectors;
        currentNumberOfPeople = currentNum;
    }

    public void setPeople(int newpeople)
    {
        currentNumberOfPeople = newpeople;
    }
}
