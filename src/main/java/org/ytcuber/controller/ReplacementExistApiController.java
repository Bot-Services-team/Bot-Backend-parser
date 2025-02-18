package org.ytcuber.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ytcuber.database.model.Replacement;
import org.ytcuber.database.repository.GroupRepository;
import org.ytcuber.database.repository.ReplacementRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/replacementexist")
@CrossOrigin(origins = "*")
public class ReplacementExistApiController {

    private final ReplacementRepository replacementRepository;

    @Autowired
    public ReplacementExistApiController(ReplacementRepository replacementRepository, GroupRepository groupRepository) {
        this.replacementRepository = replacementRepository;
    }

    private Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.parse(dateStr);
    }

    @GetMapping
    public ResponseEntity<Map<String, Boolean>> getAllReplacements(
            @RequestParam(required = true) String date) {
        try {
            Date parsedDate = parseDate(date);
            List<Replacement> replacements = replacementRepository.dateExist(parsedDate);

            Map<String, Boolean> response = new HashMap<String, Boolean>();
            response.put("status", !replacements.isEmpty());

            return ResponseEntity.ok(response);
        } catch (ParseException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}