package com.argus.server.controller;

import org.springframework.web.bind.annotation.*;

import com.argus.server.bdmodel.ExamEntity;
import com.argus.server.bdmodel.SessionEntity;
import com.argus.server.bdmodel.StudentEntity;
import com.argus.server.dto.SessionDTO;
import com.argus.server.mapper.SessionMapper;
import com.argus.server.service.ExamService;
import com.argus.server.service.SessionService;
import com.argus.server.service.StudentService;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/session")
public class SessionController {

	private final StudentService studentService;
    private final ExamService examService;
    private final SessionService sessionService;
    
    public SessionController(
            StudentService studentService,
            ExamService examService,
            SessionService sessionService
        ) {
            this.studentService = studentService;
            this.examService = examService;
            this.sessionService = sessionService;
        }

    @PostMapping("/start")
    public SessionDTO start(@RequestBody SessionDTO req) {
        StudentEntity student = studentService.findOrCreate(req.student());
        ExamEntity exam = examService.findOrCreate(req.exam());

        SessionEntity s = sessionService.findOrCreate(student, exam);
        return SessionMapper.toDTO(s);
    }

    @PostMapping("/end/{student}")
    public void end(@PathVariable String student) {
    	System.out.println("[SESSION] encerrada para " + student);
    	sessionService.endByStudent(student);
    }

    @GetMapping("/active")
    public SessionDTO getActiveSession() {
        SessionEntity s = sessionService
            .findFirstActive()
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Nenhuma sessão ativa"
            ));

        return SessionMapper.toDTO(s);
    }

//    @GetMapping("/active/{student}")
//    public SessionDTO getActiveByStudent(@PathVariable String student) {
//        SessionEntity s = sessionService
//            .findActiveByStudentName(student)
//            .orElseThrow(() -> new ResponseStatusException(
//                HttpStatus.NOT_FOUND,
//                "Aluno não possui sessão ativa"
//            ));
//
//        return SessionMapper.toDTO(s);
//    }
}
//    @GetMapping("/active")
//    public SessionEntity getActiveSession() {
//        return sessionService.getActiveSession()
//            .orElseThrow(() -> new ResponseStatusException(
//                HttpStatus.NOT_FOUND,
//                "Nenhuma sessão ativa"
//            ));
//    }
//}