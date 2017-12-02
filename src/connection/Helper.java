package connection;

import java.time.LocalDateTime;

public class Helper {

    public Helper () {
    }

    public String getCurrentTime() {
        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        String output = month+"/"+day+"/"+year+" "+hour+":"+minute+":"+second+": ";
        return output;
    }
}
