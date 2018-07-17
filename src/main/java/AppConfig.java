import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

@Configuration
@ComponentScan(basePackages = "de.wesim")
public class AppConfig {

	@Bean
	public ProgressBar p1() {
		return new ProgressBar();
	}

	@Bean
	public Label status() {
		return new Label();
	}
	
}