package com.coffee.repository;


import com.coffee.entity.InventorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, Long> {


    Optional<InventorySnapshot> findByProductIdAndSnapshotDate(Integer productId, LocalDate snapshotDate);


    List<InventorySnapshot> findAllBySnapshotDate(LocalDate snapshotDate);


    @Query("SELECT s FROM InventorySnapshot s WHERE s.snapshotDate = " +
            "(SELECT MAX(s2.snapshotDate) FROM InventorySnapshot s2 WHERE s2.snapshotDate <= :date)")
    List<InventorySnapshot> findLatestSnapshotBeforeDate(LocalDate date);


    InventorySnapshot findTopByProductIdOrderBySnapshotDateDescCreatedAtDesc(Integer productId);
}

