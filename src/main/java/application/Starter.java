package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
//import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * start the application here
 * @author 10130
 *
 */
@SpringBootApplication()
@ComponentScan({"authentication,controller,relation,assembler,object,configuration,service"})
@EntityScan("object,relation")
@EnableMongoRepositories("repository")
public class Starter {
	public static void main(String... args) {
		SpringApplication.run(Starter.class, args);
	}
	
}
