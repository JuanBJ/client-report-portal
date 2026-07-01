package com.clientreportportal.model;

public record ReportPeriod(Quarter quarter, int year) {

    public static final int MIN_YEAR = 2000;
    public static final int MAX_YEAR = 2100;

    public ReportPeriod {
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new IllegalArgumentException("Year must be between " + MIN_YEAR + " and " + MAX_YEAR);
        }
    }

    public static ReportPeriod of(int quarter, int year) {
        return new ReportPeriod(Quarter.fromValue(quarter), year);
    }

    public int quarterValue() {
        return quarter.value();
    }

    public String label() {
        return quarter.name() + " " + year;
    }
}
