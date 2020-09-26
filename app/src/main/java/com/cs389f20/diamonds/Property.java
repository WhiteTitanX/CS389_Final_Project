package com.cs389f20.diamonds;

import java.util.ArrayList;
import java.util.List;

public class Property implements java.io.Serializable {
    public String name;
    public int numOfBuildings;
    private List<Building> buildings;
    //Location?

    public Property(String _name, int _num) {
        name = _name;
        numOfBuildings = _num;
        buildings = new ArrayList<>();
    }

    public Property(String _name, int _num, List<Building> b) {
        name = _name;
        numOfBuildings = _num;
        buildings = b;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public Building getBuilding(String name) {
        for (int i = 0; i < buildings.size(); i++)
            if (buildings.get(i).name.equalsIgnoreCase(name))
                return buildings.get(i);
        return null;
    }

    public void addBuildings(List<Building> bs) {
        buildings = bs;
    }

    public void addBuilding(Building b) {
        buildings.add(b);
    }

    public void removeBuilding(Building b) {
        buildings.remove(b);
    }

    public boolean searchFor(Building b) {
        return buildings.contains(b);
    }
}
