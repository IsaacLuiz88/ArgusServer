package com.argus.server.repository;

import com.argus.server.bdmodel.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {

    @Query("""
        SELECT s FROM SessionEntity s
        WHERE s.student = :student
          AND s.exam = :exam
          AND s.status = 'ACTIVE'
    """)
    Optional<SessionEntity> findActiveSession(StudentEntity student, ExamEntity exam);

    @Query("""
    	    SELECT s FROM SessionEntity s
    	    WHERE s.student.name = :student
    	      AND s.status = 'ACTIVE'
    	""")
    	Optional<SessionEntity> findActiveByStudentName(String student);

}
