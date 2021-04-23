package io.fortylines.hrcrm.service;

import io.fortylines.hrcrm.entity.Feedback;

public interface FeedbackService {
    Feedback create(Feedback feedback);
    Feedback getById(Long id);
    void delete(Long id);
    Feedback update(Long id, Feedback feedback);
}
