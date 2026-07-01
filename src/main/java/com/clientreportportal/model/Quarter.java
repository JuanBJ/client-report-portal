package com.clientreportportal.model;

import java.util.Arrays;

public enum Quarter {
    Q1(1),
    Q2(2),
    Q3(3),
    Q4(4);

    private final int value;

    Quarter(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static Quarter fromValue(int value) {
        return Arrays.stream(values())
            .filter(q -> q.value == value)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Quarter must be one of: Q1, Q2, Q3, Q4"));
    }
}
