import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;

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
	
	@Bean
	public TabPane tp() {
		TabPane newObj = new TabPane();
		newObj.setMinWidth(500);
		newObj.setPrefWidth(500);
		return newObj; 
	}
	
}