package org.ytcuber.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ytcuber.database.model.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    @Query("SELECT l.location FROM Location l WHERE l.id = :locationId")
    String findNameById(@Param("locationId") Integer locationId);

    @Query("SELECT l.id FROM Location l ORDER BY l.id DESC LIMIT 1")
    Integer findLastId();
}