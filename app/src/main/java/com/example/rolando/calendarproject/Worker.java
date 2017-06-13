package com.example.rolando.calendarproject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rolando on 15/4/17.
 */

public class Worker {

    private String name;
    private String number_id;
    private boolean isActivated;
    private boolean daysRequested;
    private List<String> workDays = new ArrayList<>();
    private List<Long> workInts = new ArrayList<>();
    private List<Long> holidays =  new ArrayList<>();
    private List<Long> requestedHolidays = new ArrayList<>();

    public List<Long> getRequestedHolidays() {
        return requestedHolidays;
    }

    public Worker(String name, String number_id, List<Long> workInts, List<Long> holidays) {
        this.name = name;
        this.number_id = number_id;
        this.workInts = workInts;
        this.holidays = holidays;
    }

    public Worker(String name, String number_id, boolean isActivated, List<Long> workInts, List<Long> holidays) {
        this.name = name;
        this.number_id = number_id;
        this.isActivated = isActivated;
        this.workInts = workInts;
        this.holidays = holidays;
    }

    public Worker(String name, String number_id, List<Long> workInts) {
        this.name = name;
        this.number_id = number_id;
        this.workInts = workInts;
    }

    public void setActivated(boolean activated) {
        isActivated = activated;
    }

    public void setDaysRequested(boolean daysRequested) {
        this.daysRequested = daysRequested;
    }

    public void setWorkDays(List<String> workDays) {
        this.workDays = workDays;
    }

    public void setWorkInts(List<Long> workInts) {
        this.workInts = workInts;
    }

    public void setHolidays(List<Long> holidays) {
        this.holidays = holidays;
    }

    public void setRequestedHolidays(List<Long> requestedHolidays) {
        this.requestedHolidays = requestedHolidays;
    }

    public Worker(String name, String number_id, List<Long> workInts, List<Long> holidays, List<Long> requestedHolidays) {
        this.name = name;
        this.number_id = number_id;
        this.workInts = workInts;
        this.holidays = holidays;
        this.requestedHolidays = requestedHolidays;
    }

    public boolean isDaysRequested() {
        return daysRequested;
    }

    public List<Long> getWorkInts() {

        return workInts;
    }

    public boolean isActivated() {
        return isActivated;
    }

    //needed for firebase
    public Worker() {
    }

    public List<String> getWorkDays() {

        return workDays;
    }

    public List<Long> getHolidays() {
        return holidays;
    }
/*public Worker(String name, String number_id, List<String> workDays) {

        this.name = name;
        this.number_id = number_id;
        this.workDays = workDays;
    }*/

    public Worker(String name, String number_id) {
        this.name = name;
        this.number_id = number_id;

    }

    public String getName() {
        return name;
    }

    public String getNumber_id() {
        return number_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber_id(String number_id) {
        this.number_id = number_id;
    }
}
