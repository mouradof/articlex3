package rmn.ETL.stream.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring configuration for scheduling tasks and component scanning.
 * This configuration enables scheduling and scans the specified packages for Spring-managed components.
 * It also provides a Jackson ObjectMapper bean for JSON processing.
 */
@Configuration
@EnableScheduling
@ComponentScan(basePackages = {
        "rmn.ETL.stream.process.P1",
        "com.example.common_library.utils",
        "rmn.ETL.stream.config",
        "rmn.ETL.stream.entities",
        "rmn.ETL.stream.service"
})
public class ConfigP1 {

    /**
     * Configures a Jackson ObjectMapper bean.
     * The ObjectMapper is used throughout the application for JSON serialization and deserialization.
     *
     * @return a new instance of ObjectMapper.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
