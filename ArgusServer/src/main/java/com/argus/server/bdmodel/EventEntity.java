package com.argus.server.bdmodel;

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
    private Integer code;

    private Long eventTime;
    private Long receivedAt;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String raw;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String image;

	public Long getId() {return id;}
	public void setId(Long id) {this.id = id;}

	public SessionEntity getSession() {return session;}
	public void setSession(SessionEntity session) {this.session = session;}

	public String getType() {return type;}
	public void setType(String type) {this.type = type;}

	public String getAction() {return action;}
	public void setAction(String action) {this.action = action;}

	public Integer getCode() {return code;}
	public void setCode(Integer code) {this.code = code;}

	public Long getEventTime() {return eventTime;}
	public void setEventTime(Long eventTime) {this.eventTime = eventTime;}

	public Long getReceivedAt() {return receivedAt;}
	public void setReceivedAt(Long receivedAt) {this.receivedAt = receivedAt;}

	public String getRaw() {return raw;}
	public void setRaw(String raw) {this.raw = raw;}

	public String getImage() {return image;}
	public void setImage(String image) {this.image = image;}
}
