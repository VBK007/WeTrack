package nr.king.familytracker;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import nr.king.familytracker.model.http.SpringExtension;
import nr.king.familytracker.service.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableWebMvc
@Configuration
@EnableScheduling
public class DemoApplication implements WebMvcConfigurer {
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private Interceptor interceptor;

	@Autowired
	private SpringExtension springExtension;
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor)
				.addPathPatterns("/we_track/**")
				.excludePathPatterns("/we_track/**/create-deviceUser");
	}

	@Bean
	@ConditionalOnProperty(name = "mode", prefix = "test", havingValue = "false", matchIfMissing = true)
	public ActorSystem actorSystem() {
		ActorSystem system = ActorSystem.create("AkkaTaskProcessing", akkaConfiguration());
		springExtension.initialize(applicationContext);
		return system;
	}

	@Bean
	public Config akkaConfiguration() {
		return ConfigFactory.load();
	}
}
