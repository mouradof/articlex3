package rmn.ETL.stream.config;

import com.example.common_library.utils.TopicNames;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rmn.ETL.stream.entities.ARTICLEX3;

@Configuration
public class TopicNamesConfig {

    @Bean
    public TopicNames<ARTICLEX3> topicNamesForArticleX3() {
        return new TopicNames<>(ARTICLEX3.class);
    }
}
