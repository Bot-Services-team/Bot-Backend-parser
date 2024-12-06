package org.ytcuber.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ytcuber.database.model.Replacement;

import java.util.List;

@Repository
public interface ReplacementRepository extends JpaRepository<Replacement, Long> {
    List<Replacement> findReplacementsByGroupTitle(String groupName);
    
    @Query("SELECT r FROM Replacement r WHERE r.teacher = :teacherName ORDER BY r.datOfWeek, r.ordinal")
    List<Replacement> findReplacementsByTeacherAndOdd(@Param("teacherName") String teacherName);
    
    @Query("SELECT r FROM Replacement r WHERE r.group.title = :groupName AND (r.subgroup = :subgroup OR r.subgroup = 0)")
    List<Replacement> findReplacementsByGroupTitleAndSubgroup(
        @Param("groupName") String groupName, 
        @Param("subgroup") Integer subgroup
    );

    @Query("SELECT r FROM Replacement r WHERE r.teacher Like %:teacherName%")
    List<Replacement> findReplacementsByTeacher(@Param("teacherName") String teacherName);
}