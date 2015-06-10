package snippets.mongorule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

	public static void main(final String[] args) throws Exception {
		final SpringApplication springApplication = new SpringApplication(
				Application.class);
		springApplication.run(args);
	}

}