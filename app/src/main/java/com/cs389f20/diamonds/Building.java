package com.cs389f20.diamonds;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Building implements java.io.Serializable {
    public String name;
    public Property property;
    public int numOfDetectors, currentNumberOfPeople, maxOccupancy, notificationID;
    private PastCount[] pastNumberOfPeople;

    public OccupancyAlertManager.NotificationType notificationType;


    public Building(String buildingName, Property _p, int detectors, int currentNum, int maximum, int notID, int[] past, String[] timestamps) {
        name = buildingName;
        property = _p;
        numOfDetectors = detectors;
        currentNumberOfPeople = currentNum;
        maxOccupancy = maximum;
        notificationID = notID;
        if (past != null && timestamps != null)
            setPastPeople(past, timestamps);
    }

    public void setPeople(int p) {
        currentNumberOfPeople = p;
    }

    public void setPastPeople(int[] past, String[] times) {
        if (past == null || times == null) {
            Log.e(Building.class.getSimpleName(), "ERROR: setPastPeople(int[], String[]) has null parameters");
            return;
        }
        if (past.length != times.length) {
            Log.e(Building.class.getSimpleName(), "ERROR: past count and timestamps aren't in sync!");
            return;
        }
        List<Integer> people = new ArrayList<>();
        List<Date> time = new ArrayList<>();
        for (int i = 0; i < times.length; i++) {
            if (past[i] == -1 || times[i] == null)
                continue;
            people.add(past[i]);
            time.add(convertTimestampToDate(times[i]));
        }
        pastNumberOfPeople = new PastCount[people.size()];
        for (int i = 0; i < people.size(); i++) {
            pastNumberOfPeople[i] = new PastCount(time.get(i), people.get(i));
        }
        sortPast();
    }

    public PastCount[] getPastArray(int minutes) {
        return getPastPeople(minutes / 15); //15m: 1 30m: 2 1h: 4
    }

    private PastCount[] getPastPeople(int interval) {
        if (pastNumberOfPeople == null)
            return null;
        int n = (int) Math.ceil((float) pastNumberOfPeople.length / interval), index;
        List<PastCount> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            index = interval * i;
            if (index < 0)
                index = 0;
            else if (index >= pastNumberOfPeople.length)
                index = pastNumberOfPeople.length - 1;
            result.add(pastNumberOfPeople[index]);
        }
        return result.toArray(new PastCount[0]);
    }

    private void sortPast() {
        PastCount a;
        for (int i = 0; i < pastNumberOfPeople.length; i++)
            for (int j = i + 1; j < pastNumberOfPeople.length; j++)
                if (pastNumberOfPeople[i].getDate().after(pastNumberOfPeople[j].getDate())) {
                    a = pastNumberOfPeople[i];
                    pastNumberOfPeople[i] = pastNumberOfPeople[j];
                    pastNumberOfPeople[j] = a;
                }
    }

    private Date convertTimestampToDate(String time) { //2020-11-02 21:18:10 <- Mon Nov 02 2020 21:18:10 GMT+0000 (Coordinated Universal Time)
        String day, month, year, num, result;
        month = time.substring(time.indexOf(" ") + 1);
        day = month.substring(month.indexOf(" ") + 1);
        year = day.substring(day.indexOf(" ") + 1);
        num = year.substring(year.indexOf(" ") + 1);
        num = num.substring(0, num.indexOf(" "));
        year = year.substring(0, year.indexOf(" "));
        day = day.substring(0, day.indexOf(" "));
        month = month.substring(0, month.indexOf(" "));

        result = month + " " + day + " " + year + " " + num;
        Date date;
        try {
            SimpleDateFormat format = new SimpleDateFormat("MMM dd yyyy HH:mm:ss", Locale.ENGLISH);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(result);
        } catch (ParseException e) {
            Log.e(Building.class.getSimpleName(), "Parse ERROR! " + e.getMessage());
            return null;
        }
        return date;
    }
}
