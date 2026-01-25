package com.argus.server.bdmodel;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "session_activity",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"session_id"})
    }
)
public class SessionActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private SessionEntity session;

    private String lastType;
    private String lastAction;
    private Long lastTimestamp;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String lastImage;

    private LocalDateTime updatedAt;

	public Long getId() {return id;}

	public void setId(Long id) {this.id = id;}

	public SessionEntity getSession() {return session;}

	public void setSession(SessionEntity session) {this.session = session;}

	public String getLastType() {return lastType;}

	public void setLastType(String lastType) {this.lastType = lastType;}

	public String getLastAction() {return lastAction;}

	public void setLastAction(String lastAction) {this.lastAction = lastAction;}

	public Long getLastTimestamp() {return lastTimestamp;}

	public void setLastTimestamp(Long lastTimestamp) {this.lastTimestamp = lastTimestamp;}

	public String getLastImage() {return lastImage;}

	public void setLastImage(String lastImage) {this.lastImage = lastImage;}

	public LocalDateTime getUpdatedAt() {return updatedAt;}

	public void setUpdatedAt(LocalDateTime updatedAt) {this.updatedAt = updatedAt;}
}
