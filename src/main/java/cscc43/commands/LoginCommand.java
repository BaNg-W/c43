package cscc43.commands;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import cscc43.appUser.AppUser;
import cscc43.appUser.AppUserRepo;
import cscc43.appUser.CurrentUser;

@ShellComponent
public class LoginCommand {
    private final AppUserRepo appUserRepo;
    private final CurrentUser currentUser;

    public LoginCommand(AppUserRepo appUserRepo, CurrentUser currentUser) {
        this.appUserRepo = appUserRepo;
        this.currentUser = currentUser;
    }

    @ShellMethod(key = "login", value = "Login to a registered account.")
    public String login(@ShellOption String username, @ShellOption String password) {
        AppUser user = appUserRepo.findByUsername(username);
        if (user != null) {
            if (!user.getPassword().equals(password)) {
                return "The password was incorrect.";
            } else if (currentUser.getCurrentUser() != null) {
                return "A user is already logged in, please log out first.";
            } else {
                currentUser.setCurrentUser(user);
                return "Login Successful";
            }
        } else {
            return "User does not exist, please register a new user.";
        }   
    }

    @ShellMethod(key = "logout", value = "Logout from a registered account.")
    public String logout() {
        currentUser.setCurrentUser(null);
        return "Logout successful.";
    }
}