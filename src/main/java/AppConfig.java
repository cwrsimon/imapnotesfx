import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCombination;

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
	public Label account() {
		return new Label();
	}
	
	@Bean
	public MenuItem reloadMenuTask() {
		return new MenuItem("Reload");
		
	}

	@Bean
	public MenuItem exit() {
		final MenuItem exitItem = new MenuItem("Exit");
		exitItem.setAccelerator(KeyCombination.keyCombination("Shortcut+Q"));
		return exitItem;
	}

	@Bean
	public MenuItem update() {
		final MenuItem updateItem = new MenuItem("Save current Note");
		updateItem.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));
		return updateItem;
	}

	@Bean
	public MenuItem switchAccountMenuItem() {
		return new Menu("Switch Account ...");

	}

	@Bean
	public MenuItem preferences() {
		return  new Menu("Preferences");
	}
        
        @Bean
	public MenuItem find() {
            final MenuItem findMenuItem = new MenuItem("Finden ...");
            findMenuItem.setAccelerator(KeyCombination.keyCombination("Shortcut+F"));
            return findMenuItem;                        
	}
	
	@Bean
	public TabPane tp() {
		TabPane newObj = new TabPane();
		newObj.setMinWidth(500);
		newObj.setPrefWidth(500);
		return newObj; 
	}
	
}