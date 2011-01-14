package isabel.component.conference.util;

import java.util.Comparator;
import java.util.Date;

public class DateCompare implements Comparator<Date> {
    public int compare(Date one, Date two){
    return one.compareTo(two);
    }
}
