package com.argus.server.dto;

public record SessionDTO(
	    Long id,
	    String student,
	    String exam,
	    String session
	) {}