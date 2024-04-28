package com.thesurvey.api.repository;

import com.thesurvey.api.domain.QuestionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionTestRepository extends JpaRepository<QuestionTest, Long> {
}
