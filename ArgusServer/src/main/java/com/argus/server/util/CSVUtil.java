package com.argus.server.util;

import java.util.List;
import com.argus.server.model.EventEntity;

public class CSVUtil {

    public static String toCSV(List<EventEntity> events) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,type,action,code,x,y,time,student,exam,level,timestamp\n");

        for (EventEntity e : events) {
            sb.append(String.format(
                "%d,%s,%s,%d,%d,%d,%d,%s,%s,%s,%d\n",
                e.getId(), e.getType(), e.getAction(),
                e.getCode(), e.getX(), e.getY(), e.getTime(),
                e.getStudent(), e.getExam(),
                e.getLevel(), e.getTimestamp()
            ));
        }
        return sb.toString();
    }
}
