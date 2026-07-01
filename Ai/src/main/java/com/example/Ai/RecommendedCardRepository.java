package com.example.Ai;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecommendedCardRepository extends JpaRepository<RecommendedCard, Long> {
    void deleteByUsername(String username);
    List<RecommendedCard> findByUsernameOrderByRecommendationOrderAsc(String username);
}