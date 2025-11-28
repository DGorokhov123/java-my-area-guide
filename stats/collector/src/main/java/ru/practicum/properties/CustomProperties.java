package ru.practicum.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("my-area-guide")
public class CustomProperties {

    private final Kafka kafka = new Kafka();

    @Getter
    @Setter
    public static class Kafka {
        private String userActionTopic = "user-actions";
        private String eventsSimilarityTopic = "events-similarity";
    }

}
