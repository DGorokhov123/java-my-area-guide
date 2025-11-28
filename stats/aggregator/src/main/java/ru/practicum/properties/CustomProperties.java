package ru.practicum.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties("my-area-guide")
@Component
public class CustomProperties {

    private final Kafka kafka = new Kafka();
    private final Aggregator aggregator = new Aggregator();

    @Getter
    @Setter
    public static class Kafka {
        private String userActionTopic = "user-actions";
        private String eventsSimilarityTopic = "events-similarity";
    }

    @Getter
    @Setter
    public static class Aggregator {
        private final Weights weights = new Weights();
        private String minimumSumAlgorithm = "optimized";
    }

    @Getter
    @Setter
    public static class Weights {
        private String like = "0.9";
        private String register = "0.7";
        private String view = "0.3";

        public BigDecimal ofUserAction(UserActionAvro userActionAvro) {
            return switch (userActionAvro.getActionType()) {
                case LIKE -> new BigDecimal(like);
                case REGISTER -> new BigDecimal(register);
                default -> new BigDecimal(view);
            };
        }
    }

}
