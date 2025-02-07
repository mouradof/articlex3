package rmn.ETL.stream.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring configuration enabling scheduling and component scanning
 * for the P2 process and related packages.
 */
@Configuration
@EnableScheduling
@ComponentScan(basePackages = {
        "rmn.ETL.stream.process.P2",
        "com.example.common_library.utils",
        "rmn.ETL.stream.config",
        "rmn.ETL.stream.entities",
        "rmn.ETL.stream.service"
})
public class ConfigP2 {
}
