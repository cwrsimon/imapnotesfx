import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import de.wesim.imapnotes.Consts;
import javafx.scene.control.Label;
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
		return new MenuItem();
		
	}

	@Bean
	public MenuItem exit() {
		final MenuItem exitItem = new MenuItem();
		exitItem.setAccelerator(KeyCombination.keyCombination(Consts.SHORTCUT_QUIT));
		return exitItem;
	}

	@Bean
	public MenuItem update() {
		// content will be configured in MainView
		final MenuItem updateItem = new MenuItem();
		updateItem.setAccelerator(KeyCombination.keyCombination(Consts.SHORTCUT_SAVE));
		return updateItem;
	}

	@Bean
	public MenuItem newNote() {
		// content will be configured in MainView
		return new MenuItem();
	}
	
	@Bean
	public MenuItem newFolder() {
		// content will be configured in MainView
		return new MenuItem();
	}
	
	@Bean
	public MenuItem switchAccountMenuItem() {
		// content will be configured in MainView
		return new MenuItem();

	}

	@Bean
	public MenuItem preferences() {
		// content will be configured in MainView
		return new MenuItem();
	}
	
	@Bean
	public MenuItem about() {
		// content will be configured in MainView
		return new MenuItem();
	}
        
    @Bean
	public MenuItem find() {
		// content will be configured in MainView
        final MenuItem findMenuItem = new MenuItem();
        findMenuItem.setAccelerator(KeyCombination.keyCombination(Consts.SHORTCUT_FIND));
        return findMenuItem;                        
	}
	
	@Bean
	public TabPane tp() {
		TabPane newObj = new TabPane();
		// TODO
		newObj.setMinWidth(500);
		newObj.setPrefWidth(500);
		return newObj; 
	}
	
}