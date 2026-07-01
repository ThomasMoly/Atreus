package com.example.Ai;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AiRepo extends JpaRepository<StatementData, Long> {
    List<StatementData> findByUsername(String username);
}