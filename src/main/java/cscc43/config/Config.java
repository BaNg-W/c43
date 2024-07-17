package cscc43.config;


import org.jline.utils.AttributedString;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;

@Configuration
@ComponentScan("de.myapp.spring.shell")
public class Config implements PromptProvider {

    @Override
    public final AttributedString getPrompt() {
        return new AttributedString("StockTrackr >");
    }

}
