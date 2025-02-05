package rmn.ETL.stream.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

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
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
