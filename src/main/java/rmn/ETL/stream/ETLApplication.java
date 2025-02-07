package rmn.ETL.stream;

import org.springframework.boot.builder.SpringApplicationBuilder;
import rmn.ETL.stream.config.ConfigP0;
import rmn.ETL.stream.config.ConfigP1;
import rmn.ETL.stream.config.ConfigP2;
import rmn.ETL.stream.config.ConfigP3;

public class ETLApplication {
    public static void main(String[] args) {
        String process = System.getProperty("process", "P0");

        SpringApplicationBuilder builder = new SpringApplicationBuilder();

        if ("P0".equalsIgnoreCase(process)) {
            builder.sources(ConfigP0.class);
        } else if ("P1".equalsIgnoreCase(process)) {
            builder.sources(ConfigP1.class);
        } else if ("P2".equalsIgnoreCase(process)) {
            builder.sources(ConfigP2.class);
        } else if ("P3".equalsIgnoreCase(process)) {
            builder.sources(ConfigP3.class);
        } else {
            throw new IllegalArgumentException("Process inconnu: " + process);
        }

        builder.run(args);
    }
}
