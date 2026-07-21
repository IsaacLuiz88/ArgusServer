package com.argus.server.bdmodel;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(
    name = "sessions",
	indexes = {
	        // índice para buscas por aluno + prova
	        @Index(name = "idx_session_student_exam",
	               columnList = "student_id, exam_id"),

	        // índice para buscas por status
	        @Index(name = "idx_session_status",
	               columnList = "status"),

	        // índice útil para localizar sessões ativas rapidamente
	        @Index(name = "idx_session_student_exam_status",
	               columnList = "student_id, exam_id, status")
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

    @Column(nullable = false, unique = true)
    private String sessionUuid;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

 // ALTERADO:
    // idealmente deveria virar Enum futuramente.
    //
    // Exemplo:
    // @Enumerated(EnumType.STRING)
    // private SessionStatus status;
    //
    // Mantido String para evitar quebrar o restante do sistema agora.
    @Column(nullable = false)
    private String status; // ACTIVE, FINISHED

    private String ipAddress;

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

	public String getIpAddress() {return ipAddress;}
	public void setIpAddress(String ipAddress) {this.ipAddress = ipAddress;}
}
