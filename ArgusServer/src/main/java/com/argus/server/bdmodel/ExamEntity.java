package com.argus.server.bdmodel;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "exams")
public class ExamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}

	public String getCode() {return code;}
	public void setCode(String code) {this.code = code;}

	public String getName() {return name;}
	public void setName(String name) {this.name = name;}

	public LocalDateTime getStartTime() {return startTime;}
	public void setStartTime(LocalDateTime startTime) {this.startTime = startTime;}

	public LocalDateTime getEndTime() {return endTime;}
	public void setEndTime(LocalDateTime endTime) {this.endTime = endTime;}
}
