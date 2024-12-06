package org.ytcuber.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ytcuber.database.model.Lesson;
import org.ytcuber.database.model.Replacement;
import org.ytcuber.database.repository.LessonRepository;
import org.ytcuber.database.repository.ReplacementRepository;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*")
public class TeacherApiController {
    
    private final LessonRepository lessonRepository;
    private final ReplacementRepository replacementRepository;

    @Autowired
    public TeacherApiController(LessonRepository lessonRepository, ReplacementRepository replacementRepository) {
        this.lessonRepository = lessonRepository;
        this.replacementRepository = replacementRepository;
    }

    @GetMapping("/{teacherName}/lessons")
    public ResponseEntity<List<Lesson>> getTeacherLessons(
            @PathVariable String teacherName,
            @RequestParam(required = false) Integer odd
    ) {
        if (odd != null) {
            return ResponseEntity.ok(
                lessonRepository.findLessonsByTeacherAndOdd(teacherName, odd)
            );
        }
        return ResponseEntity.ok(lessonRepository.findLessonsByTeacher(teacherName));
    }

    @GetMapping("/{teacherName}/replacements")
    public ResponseEntity<List<Replacement>> getTeacherReplacements(
            @PathVariable String teacherName
    ) {
        return ResponseEntity.ok(replacementRepository.findReplacementsByTeacher(teacherName));
    }
}
