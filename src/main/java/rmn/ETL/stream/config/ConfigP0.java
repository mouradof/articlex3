package rmn.ETL.stream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Spring configuration for scheduling tasks and component scanning.
 * This configuration enables scheduling and scans the specified packages
 * for Spring-managed components.
 */
@Configuration
@EnableScheduling
@ComponentScan(basePackages = {
        "rmn.ETL.stream.process.P0",
        "com.example.common_library.utils",
        "rmn.ETL.stream.config",
        "rmn.ETL.stream.entities",
        "rmn.ETL.stream.service"
})
public class ConfigP0 {

    /**
     * Configures a ThreadPoolTaskScheduler bean.
     * The scheduler is set with a fixed pool size of 1, a custom thread name prefix,
     * and non-daemon threads. This scheduler is used to execute tasks annotated with
     * {@code @Scheduled}.
     *
     * @return a configured ThreadPoolTaskScheduler instance.
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setDaemon(false);
        return scheduler;
    }
}
