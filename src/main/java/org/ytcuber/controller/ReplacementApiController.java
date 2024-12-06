package org.ytcuber.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ytcuber.database.model.Replacement;
import org.ytcuber.database.repository.GroupRepository;
import org.ytcuber.database.repository.ReplacementRepository;

import java.util.List;

@RestController
@RequestMapping("/api/replacement")
@CrossOrigin(origins = "*")
public class ReplacementApiController {

    private final ReplacementRepository replacementRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public ReplacementApiController(ReplacementRepository replacementRepository, GroupRepository groupRepository) {
        this.replacementRepository = replacementRepository;
        this.groupRepository = groupRepository;
    }

    @GetMapping
    public ResponseEntity<List<Replacement>> getAllReplacements(
            @RequestParam(required = false) Integer subgroup,
            @RequestParam(required = false) String groupName
    ) {
        if (subgroup != null && groupName != null) {
            return ResponseEntity.ok(
                replacementRepository.findReplacementsByGroupTitleAndSubgroup(groupName, subgroup)
            );
        }
        
        if (groupName != null) {
            return ResponseEntity.ok(replacementRepository.findReplacementsByGroupTitle(groupName));
        }
        
        return ResponseEntity.ok(replacementRepository.findAll());
    }
}