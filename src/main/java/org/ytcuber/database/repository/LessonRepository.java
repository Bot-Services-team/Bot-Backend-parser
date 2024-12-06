package org.ytcuber.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ytcuber.database.model.Lesson;

import java.io.Console;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
//    List<Lesson> findBySubgroup(Integer subgroup);
    // Возврашение всех пар
    @Query("select l from Lesson l where (l.subgroup = :subgroup or l.subgroup = 0) order by l.odd, l.dayOfWeek, l.ordinal")
    List<Lesson> getLessons(Integer subgroup);

    @Query("SELECT l FROM Lesson l " +
            "WHERE (l.subgroup = :subgroup OR l.subgroup = 0) AND l.group.id = :groupId AND l.odd = :odd " +
            "ORDER BY l.dayOfWeek, l.ordinal")
    List<Lesson> findLessonsByGroupIdAndSubgroupAndOdd(@Param("groupId") Integer groupId,
                                                          @Param("subgroup") Integer subgroup,
                                                          @Param("odd") Integer odd);
    @Query("SELECT l FROM Lesson l WHERE l.teacher LIKE %:teacherName% AND l.odd = :odd\n" +
            "                       ORDER BY l.dayOfWeek, l.ordinal")
    List<Lesson> findLessonsByTeacherAndOdd(@Param("teacherName") String teacherName, @Param("odd") Integer odd);

    @Query("SELECT l FROM Lesson l WHERE l.teacher LIKE %:teacherName%")
    List<Lesson> findLessonsByTeacher(@Param("teacherName") String teacherName);

    @Query("SELECT l FROM Lesson l WHERE l.location = :locationName AND l.odd = :odd ORDER BY l.dayOfWeek, l.ordinal")
    List<Lesson> findLessonsByLocationAndOdd(@Param("locationName") String locationName, @Param("odd") Integer odd);

    @Query("SELECT l FROM Lesson l WHERE l.location = :locationName ORDER BY l.dayOfWeek, l.ordinal")
    List<Lesson> findLessonsByLocation(@Param("locationName") String locationName);
}
