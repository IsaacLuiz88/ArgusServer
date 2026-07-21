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

	private LocalDateTime startedAt;

	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}

	public String getCode() {return code;}
	public void setCode(String code) {this.code = code;}

	public LocalDateTime getStartedAt() {return startedAt;}
	public void setStartedAt(LocalDateTime startedAt) {this.startedAt = startedAt;}
}
