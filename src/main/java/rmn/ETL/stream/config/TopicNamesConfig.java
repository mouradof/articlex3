package rmn.ETL.stream.config;

import com.example.common_library.utils.TopicNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rmn.ETL.stream.entities.ARTICLEX3;

/**
 * Spring configuration for registering TopicNames bean.
 * This configuration creates a bean for TopicNames specifically for ARTICLEX3 entities,
 * which can be used to manage topic names for messaging or event handling.
 */
@Configuration
public class TopicNamesConfig {

    /**
     * Creates a TopicNames bean for ARTICLEX3.
     *
     * @return a new TopicNames instance parameterized with ARTICLEX3.
     */
    @Bean
    public TopicNames<ARTICLEX3> topicNamesForArticleX3() {
        return new TopicNames<>(ARTICLEX3.class);
    }
}
