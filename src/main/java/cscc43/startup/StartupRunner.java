package cscc43.startup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(-1)
@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("""

        
            Welcome to StockTrackr!

            Please use the login command to sign into your account.
            
            If you do not have an account, please use the register command to make one.
            
            You can also use "help" to all available commands at any time.
            """);
    }
}
