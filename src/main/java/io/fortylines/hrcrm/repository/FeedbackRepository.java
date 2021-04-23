package io.fortylines.hrcrm.repository;

import io.fortylines.hrcrm.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
