package com.example.rolando.calendarproject;

/**
 * Created by rolando on 16/4/17.
 */

public class Utils {

    public static String[] populateMonth(int month) {
        String[] days_of_month;

        if (month==0 && month==2 && month==4 &&
                month==6 && month==7 && month==9 && month==11) {//31 day month
            return days_of_month = new String[]{
                    "1", "2", "3", "4", "5",
                    "6", "7", "8", "9", "10",
                    "11", "12", "13", "14", "15",
                    "16", "17", "18", "19", "20",
                    "21", "22", "23", "24", "25",
                    "26","27","28","29","30","31"};
        } else if (month==1) {//february
            return days_of_month = new String[]{
                    "1", "2", "3", "4", "5",
                    "6", "7", "8", "9", "10",
                    "11", "12", "13", "14", "15",
                    "16", "17", "18", "19", "20",
                    "21", "22", "23", "24", "25",
                    "26","27","28"};
        }else {//30 day month
            return days_of_month = new String[]{
                    "1", "2", "3", "4", "5",
                    "6", "7", "8", "9", "10",
                    "11", "12", "13", "14", "15",
                    "16", "17", "18", "19", "20",
                    "21", "22", "23", "24", "25",
                    "26","27","28","29","30"};
        }

    }

    public String getMonthName(int monthNumber){
        String month = "";
        switch (monthNumber){
            case 0:
                month = "January";
            break;
            case 1:
                month = "February";
            break;
            case 2:
                month = "March";
            break;
            case 3:
                month = "April";
            break;
            case 4:
                month = "May";
            break;
            case 5:
                month = "June";
            break;
            case 6:
                month = "July";
            break;
            case 7:
                month = "August";
            break;
            case 8:
                month = "September";
            break;
            case 9:
                month = "October";
            break;
            case 10:
                month = "November";
            break;
            case 11:
                month = "December";
            break;
            default:
                month = "Invalid Month";
                break;
        }
        return month;

    }
}
