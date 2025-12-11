package com.argus.server.model;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class Event {
    private String type;
    private String action;
    private int code;
    private int x;
    private int y;
    private long time;
    private String student;
    private String exam;
    private String level;      // "green", "yellow", "red"
    private long timestamp;    // para o servidor marcar a hora de recebimento
    private String image;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }

    public String getStudent() { return student; }
    public void setStudent(String student) { this.student = student; }

    public String getExam() { return exam; }
    public void setExam(String exam) { this.exam = exam; }

    public String getLevel() { return level; }
	public void setLevel(String level) { this.level = level; }

	public long getTimestamp() { return timestamp; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	
	public String getImage() {return image;}
	public void setImage(String image) {this.image = image;}
	
	@Override
    public String toString() {
        return String.format("[%s] %s (code=%d, x=%d, y=%d, time=%d, student=%s, exam=%s)", 
                			  type, action, code, x, y, time, student, exam);
    }
}
