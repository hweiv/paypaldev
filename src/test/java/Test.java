import com.google.common.collect.ImmutableMap;
import com.jimi.common.constant.PaypalTransConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class Test {
    public static void main(String[] args) {
//        Calendar calendar = Calendar.getInstance(); // create a Calendar instance
//        Date date = calendar.getTime();
//        TimeZone timeZone = calendar.getTimeZone();
//        System.out.println("date:" + date);
//        System.out.println("timeZone:" + timeZone);
//        LocalDate now = LocalDate.now();
//        System.out.println("now:" + now);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
////        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // set the time zone to UTC
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // set the time zone to UTC
//        String utcTime = sdf.format(date);
//        System.out.println("utcTime:" + utcTime);
//        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
//        sdf1.setTimeZone(TimeZone.getTimeZone("GMT+8")); // set the time zone to Beijing
//        String beijingTime = sdf1.format(date);
//        System.out.println("beijing:" + beijingTime);
//        System.out.println(sdf.format(Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant())));
//        System.out.println(sdf1.format(Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant())));
//        LocalDateTime endOfDay = now.atTime(23, 59, 59);
//        System.out.println(sdf1.format(Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant())));
//        System.out.println(sdf.format(Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant())));
        String aa = "银行账户通知：\n账户姓名：张三\n账户尾号：9980";
        System.out.println(aa);


    }

    @org.junit.Test
    public void f1() {
        System.out.println(PaypalTransConstant.BIND_ACCOUNT_NAME.get("12"));
    }

}
