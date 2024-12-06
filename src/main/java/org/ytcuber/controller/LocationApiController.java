package org.ytcuber.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ytcuber.database.model.Location;
import org.ytcuber.database.model.Lesson;
import org.ytcuber.database.model.Replacement;
import org.ytcuber.database.repository.LocationRepository;
import org.ytcuber.database.repository.LessonRepository;
import org.ytcuber.database.repository.ReplacementRepository;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationApiController {
    
    private final LocationRepository locationRepository;
    private final LessonRepository lessonRepository;
    private final ReplacementRepository replacementRepository;
    
    @Autowired
    public LocationApiController(LocationRepository locationRepository, LessonRepository lessonRepository, ReplacementRepository replacementRepository) {
        this.locationRepository = locationRepository;
        this.lessonRepository = lessonRepository;
        this.replacementRepository = replacementRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        return ResponseEntity.ok(locationRepository.findAll());
    }
    
    @GetMapping("/{name}/lessons")
    public ResponseEntity<List<Lesson>> getLocationNameByName(
            @PathVariable String name,
            @RequestParam(required = true) Integer odd
    ) {
        if (odd != null) {
            return ResponseEntity.ok(lessonRepository.findLessonsByLocationAndOdd(name, odd));
        }   
        return ResponseEntity.ok(lessonRepository.findLessonsByLocation(name));
    }

    @GetMapping("/{name}/replacements")
    public ResponseEntity<List<Replacement>> getLocationNameByNameReplacements(
            @PathVariable String name
    ) {
        return ResponseEntity.ok(replacementRepository.findReplacementsByLocation(name));
    }
}
