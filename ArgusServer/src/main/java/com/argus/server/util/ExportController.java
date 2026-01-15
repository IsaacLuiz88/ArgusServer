package com.argus.server.util;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.argus.server.model.EventEntity;
import com.argus.server.repository.EventRepository;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final EventRepository repo;

    public ExportController(EventRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/csv")
    public ResponseEntity<String> exportCsv() {
        List<EventEntity> list = repo.findAll();
        return ResponseEntity.ok(CSVUtil.toCSV(list));
    }
}
