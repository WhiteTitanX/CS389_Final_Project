package com.cs389f20.diamonds;

import java.util.Date;

public class PastCount implements java.io.Serializable {
    private Date date;
    private int people;

    public PastCount(Date d, int p) {
        date = d;
        people = p;
    }

    public int getPeople() {
        return people;
    }

    public Date getDate() {
        return date;
    }

    public String getStringDate() {
        String time = date.toString().substring(date.toString().indexOf(":") - 2, date.toString().lastIndexOf(":"));
        int hour = Integer.parseInt(time.substring(0, 2));
        if (hour < 12) {
            if (hour == 0)
                hour = 12;
            if (hour < 10)
                time = " " + hour + time.substring(2) + " AM";
            else
                time = hour + time.substring(2) + " AM";
        } else {
            if (hour != 12)
                hour = hour - 12;
            if (hour < 10)
                time = " " + hour + time.substring(2) + " PM";
            else
                time = hour + time.substring(2) + " PM";
        }
        return date.toString().substring(0, date.toString().indexOf(":") - 2) + time;
    }
}
