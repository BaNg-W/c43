package cscc43.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class RegisterCommand {

    @ShellMethod(key = "register", value = "Register an account for a new user.")
    public String register() {
        
    }
}
