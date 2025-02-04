package rmn.ETL.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan (basePackages = {"rmn.ETL.stream.process.P0", "com.example.common_library.utils",
        "rmn.ETL.stream.config", "rmn.ETL.stream.entities"})
public class ETLApplication {
    public static void main(String[] args) {
        SpringApplication.run(ETLApplication.class, args);
    }
}
