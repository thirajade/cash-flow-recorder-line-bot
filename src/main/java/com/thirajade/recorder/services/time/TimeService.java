package com.thirajade.recorder.services.time;

import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class TimeService {

    public Date convertDateWithFormat(String date, String format) {
        String transactionDateInString = date;
        try {
            Date dateWithoutYear = new SimpleDateFormat(format).parse(transactionDateInString);

            //add fix year and timezone
            SimpleDateFormat formatWithFixYear = new SimpleDateFormat("dd/MM/" + Calendar.getInstance().get(Calendar.YEAR) + " HH:mm:ss");
            String dateWithYearInString = formatWithFixYear.format(dateWithoutYear);

            //parse to Date type
            SimpleDateFormat formatWithRealYear = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            return formatWithRealYear.parse(dateWithYearInString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
