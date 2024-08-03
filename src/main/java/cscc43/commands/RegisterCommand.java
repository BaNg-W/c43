package cscc43.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import cscc43.appUser.AppUser;
import cscc43.appUser.AppUserRepo;

@ShellComponent
public class RegisterCommand {
    private final AppUserRepo appUserRepo;

    public RegisterCommand(AppUserRepo appUserRepo) {
        this.appUserRepo = appUserRepo;
    }

    @ShellMethod(key = "register", value = "Register an account for a new user. >register <username> <email> <password>")
    public String register(@ShellOption String username, @ShellOption String email, @ShellOption String password) {
        
        AppUser appUser = new AppUser(username, email, password);
        if (appUserRepo.findByUsername(username) != null) {
            return "Error: Username already exists. Please choose a different username.";
        } else {
            appUserRepo.save(appUser);
            return "User registered successfully.";
        }   
    }
}
