package com.alya.aplikasilansia.ui.reminder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FilteredReminder {
    private int imgReminder;

    private String titleDayReminder;

    private String titleReminder;
    private String dayReminder;
    private String timeReminder;
    private String descReminder;


    public FilteredReminder(String titleReminder, String timeReminder, String descReminder, int imgReminder) {
        this.titleReminder = titleReminder;
        this.timeReminder = timeReminder;
        this.descReminder = descReminder;
        this.imgReminder = imgReminder;
//        this.titleDayReminder = calculateTitleDay(timeReminder); // Calculate titleDayReminder based on timeReminder
    }
    public String getTitleDayReminder() {
        return titleDayReminder;
    }
    public int getImgReminder() {
        return imgReminder;
    }

    public void setImgReminder(int imgReminder) {
        this.imgReminder = imgReminder;
    }

    public String getTitleReminder() {
        return titleReminder;
    }

    public String getTimeReminder() {
        return timeReminder;
    }


    public String getDescReminder() {
        return descReminder;
    }

    // Method to calculate titleDayReminder based on timeReminder (date)
    private String calculateTitleDay(String timeReminder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date reminderDate = sdf.parse(timeReminder);

            Calendar today = Calendar.getInstance();
            Calendar reminderCalendar = Calendar.getInstance();
            reminderCalendar.setTime(reminderDate);

            if (isSameDay(today, reminderCalendar)) {
                return "Hari Ini";
            } else if (isTomorrow(today, reminderCalendar)) {
                return "Besok";
            } else {
                Calendar tomorrow = (Calendar) today.clone();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);
                if (reminderCalendar.after(tomorrow)) {
                    return "Yang Akan Datang";
                } else {
                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
                    return dayFormat.format(reminderDate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Label Default"; // Handle parsing exception or return default label
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isTomorrow(Calendar cal1, Calendar cal2) {
        Calendar tomorrow = (Calendar) cal1.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        return tomorrow.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
