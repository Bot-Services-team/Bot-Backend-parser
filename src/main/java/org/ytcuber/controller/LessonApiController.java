package org.ytcuber.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ytcuber.database.model.Lesson;
import org.ytcuber.database.repository.GroupRepository;
import org.ytcuber.database.repository.LessonRepository;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "*")
public class LessonApiController {

    private final LessonRepository lessonRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public LessonApiController(LessonRepository lessonRepository, GroupRepository groupRepository) {
        this.lessonRepository = lessonRepository;
        this.groupRepository = groupRepository;
    }

    @GetMapping
    public ResponseEntity<List<Lesson>> getAllLessons(
            @RequestParam(required = false) Integer subgroup,
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) Integer odd
    ) {
        if (subgroup != null && groupName != null && odd != null) {
            return ResponseEntity.ok(
                lessonRepository.findLessonsByGroupIdAndSubgroupAndOdd(groupRepository.findByName(groupName), subgroup, odd)
            );
        }
        
        if (subgroup != null) {
            return ResponseEntity.ok(lessonRepository.getLessons(subgroup));
        }
        
        return ResponseEntity.ok(lessonRepository.findAll());
    }
}