package com.argus.server.bdmodel;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(
    name = "sessions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "exam_id"})
    }
)
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private StudentEntity student;

    @ManyToOne
    private ExamEntity exam;

    @Column(nullable = false)
    private String sessionUuid;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    private String status; // ACTIVE, FINISHED, TERMINATED

	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}

	public StudentEntity getStudent() {return student;}
	public void setStudent(StudentEntity student) {this.student = student;}

	public ExamEntity getExam() {return exam;}
	public void setExam(ExamEntity exam) {this.exam = exam;}

	public String getSessionUuid() {return sessionUuid;}
	public void setSessionUuid(String sessionUuid) {this.sessionUuid = sessionUuid;}

	public LocalDateTime getStartedAt() {return startedAt;}
	public void setStartedAt(LocalDateTime startedAt) {this.startedAt = startedAt;}

	public LocalDateTime getEndedAt() {return endedAt;}
	public void setEndedAt(LocalDateTime endedAt) {this.endedAt = endedAt;}

	public String getStatus() {return status;}
	public void setStatus(String status) {this.status = status;}
}
