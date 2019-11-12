package com.completeinnovations.ert.restapi;

/**
 *
 */
public class Employee {
    String key;
    String value;


    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Employee: {key: " + key + ", value: " + value + "}";
    }
}