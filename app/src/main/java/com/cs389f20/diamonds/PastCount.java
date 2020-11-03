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
}
