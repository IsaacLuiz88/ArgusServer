package com.argus.server.bdmodel;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "events",
    indexes = {
        @Index(columnList = "session_id"),
        @Index(columnList = "receivedAt")
    }
)
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private SessionEntity session;

    private String type;
    private String action;
    private LocalDateTime receivedAt;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String raw;

    private String ip;

	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}

	public SessionEntity getSession() {return session;}
	public void setSession(SessionEntity session) {this.session = session;}

	public String getType() {return type;}
	public void setType(String type) {this.type = type;}

	public String getAction() {return action;}
	public void setAction(String action) {this.action = action;}

	public LocalDateTime getReceivedAt() {return receivedAt;}
	public void setReceivedAt(LocalDateTime receivedAt) {this.receivedAt = receivedAt;}

	public String getRaw() {return raw;}
	public void setRaw(String raw) {this.raw = raw;}

	public String getIp() {return ip;}
	public void setIp(String ip) {this.ip = ip;}
}
