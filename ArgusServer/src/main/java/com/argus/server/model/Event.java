package com.argus.server.model;

public class Event {
    private String type;
    private String action;
    private int code;
    private long time;
    private String student;
    private String exam;
    private long timestamp;    // para o servidor marcar a hora de recebimento
    private String image;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public long getTime() { return time; }
    public void setTime(long time) { this.time = time; }

    public String getStudent() { return student; }
    public void setStudent(String student) { this.student = student; }

    public String getExam() { return exam; }
    public void setExam(String exam) { this.exam = exam; }

	public long getTimestamp() { return timestamp; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
	
	public String getImage() {return image;}
	public void setImage(String image) {this.image = image;}

	@Override
    public String toString() {
		return String.format(
		        "[%s] %s (code=%d, time=%d, student=%s, exam=%s)",
		        type, action, code, time, student, exam);
    }
}
