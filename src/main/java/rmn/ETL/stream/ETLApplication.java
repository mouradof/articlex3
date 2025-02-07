package rmn.ETL.stream;

import org.springframework.boot.builder.SpringApplicationBuilder;
import rmn.ETL.stream.config.ConfigP0;
import rmn.ETL.stream.config.ConfigP1;
import rmn.ETL.stream.config.ConfigP2;
import rmn.ETL.stream.config.ConfigP3;

/**
 * Entry point for the ETL application.
 * <p>
 * The process type is determined by the "process" system property (default is "P0"). Depending
 * on the specified process, the corresponding Spring configuration is loaded.
 */
public class ETLApplication {
    public static void main(String[] args) {
        // Determine the process type from system properties (default to "P0")
        String process = System.getProperty("process", "P0");

        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        // Load the appropriate configuration based on the process type
        if ("P0".equalsIgnoreCase(process)) {
            builder.sources(ConfigP0.class);
        } else if ("P1".equalsIgnoreCase(process)) {
            builder.sources(ConfigP1.class);
        } else if ("P2".equalsIgnoreCase(process)) {
            builder.sources(ConfigP2.class);
        } else if ("P3".equalsIgnoreCase(process)) {
            builder.sources(ConfigP3.class);
        } else {
            throw new IllegalArgumentException("Unknown process: " + process);
        }

        // Run the application with the provided arguments
        builder.run(args);
    }
}
