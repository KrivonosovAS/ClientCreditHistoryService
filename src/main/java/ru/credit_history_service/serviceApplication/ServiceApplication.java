package ru.credit_history_service.serviceApplication;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("ru.credit_history_service.config")
public class ServiceApplication
{
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}
