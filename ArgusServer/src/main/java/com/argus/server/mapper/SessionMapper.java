package com.argus.server.mapper;


import com.argus.server.bdmodel.SessionEntity;
import com.argus.server.dto.SessionDTO;

public class SessionMapper {

    public static SessionDTO toDTO(SessionEntity s) {
        return new SessionDTO(
            s.getId(),
            s.getStudent().getName(),
            s.getExam().getCode(),
            s.getSessionUuid()
        );
    }
}