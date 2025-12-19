package com.argus.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private String action;
    private Integer code;
    private Integer x;
    private Integer y;
    private Long time; // timestamp enviado pelo cliente
    private String student;
    private String exam;
    private String level;
    private Long timestamp; // servidor

    // optionally store raw JSON
    @Column(columnDefinition = "text")
    private String raw;

    @Column(columnDefinition = "text")
    private String image;

	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}

	public String getType() {return type;}
	public void setType(String type) {this.type = type;}

	public String getAction() {return action;}
	public void setAction(String action) {this.action = action;}

	public Integer getCode() {return code;}
	public void setCode(Integer code) {this.code = code;}

	public Integer getX() {return x;}
	public void setX(Integer x) {this.x = x;}

	public Integer getY() {return y;}
	public void setY(Integer y) {this.y = y;}

	public Long getTime() {return time;}
	public void setTime(Long time) {this.time = time;}

	public String getStudent() {return student;}
	public void setStudent(String student) {this.student = student;}

	public String getExam() {return exam;}
	public void setExam(String exam) {this.exam = exam;}

	public String getLevel() {return level;}
	public void setLevel(String level) {this.level = level;}

	public Long getTimestamp() {return timestamp;}
	public void setTimestamp(Long timestamp) {this.timestamp = timestamp;}

	public String getRaw() {return raw;}
	public void setRaw(String raw) {this.raw = raw;}

	public String getImage() {return image;}
	public void setImage(String image) {this.image = image;}	
}
