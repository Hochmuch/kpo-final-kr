package org.example.repository;

import org.example.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    
    @Query("SELECT e FROM OutboxEvent e WHERE e.processed = false ORDER BY e.id ASC")
    List<OutboxEvent> findUnprocessed();
} 