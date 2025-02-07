package com.example.common_library.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TopicNames<T> {

    private final Class<T> targetEntityClass;

    @Value("${kafka.topics.staging:article_staging}")
    private String stagingTopic;

    @Value("${kafka.topics.validated:article_validated}")
    private String validatedTopic;

    @Value("${kafka.topics.rejected:article_rejected}")
    private String rejectedTopic;

    @Value("${kafka.topics.transformed:article_bext_transformed}")
    private String transformedTopic;  // 🔥 Ajout du topic cible explicite

    public TopicNames() {
        this.targetEntityClass = null;
    }

    public TopicNames(Class<T> targetEntityClass) {
        this.targetEntityClass = targetEntityClass;
    }

    public String getStagingTopicName() {
        return stagingTopic;
    }

    public String getValidatedTopicName() {
        return validatedTopic;
    }

    public String getRejectedTopicName() {
        return rejectedTopic;
    }

    // ✅ Fix : Retourner explicitement `article_bext_transformed`
    public String getTargetTopicName() {
        return transformedTopic;
    }
}
