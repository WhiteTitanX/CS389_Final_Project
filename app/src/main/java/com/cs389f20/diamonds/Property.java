package com.cs389f20.diamonds;

import java.util.ArrayList;
import java.util.List;

public class Property implements java.io.Serializable {
    public String name;
    private List<Building> buildings;
    //Location?

    public Property(String _name) {
        name = _name;
        buildings = new ArrayList<>();
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

    public Building getBuildingFromID(String id) {
        for (int i = 0; i < buildings.size(); i++)
            if (buildings.get(i).id.equalsIgnoreCase(id))
                return buildings.get(i);
        return null;
    }

    public void addBuilding(Building b) {
        buildings.add(b);
    }

    public boolean updateBuilding(String id, int newCount, int[] past, String[] times) {
        try {
            if (newCount != -1)
                getBuildingFromID(id).setPeople(newCount);
            else if (past != null && times != null)
                getBuildingFromID(id).setPastPeople(past, times);
            else
                return false;
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean contains(String id) {
        for (int i = 0; i < buildings.size(); i++) {
            if (buildings.get(i).id.equalsIgnoreCase(id))
                return true;
        }
        return false;
    }
}
