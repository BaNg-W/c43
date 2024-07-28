package cscc43.commands;

import java.util.Scanner;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import cscc43.Stock.StockRepo;
import cscc43.appUser.CurrentUser;

@ShellComponent
public class PortfolioCommand {
    private final StockRepo stockRepo;
    private final CurrentUser currentUser;

    private final Scanner scanner = new Scanner(System.in);

    public PortfolioCommand(StockRepo stockRepo, CurrentUser currentUser) {
        this.stockRepo = stockRepo;
        this.currentUser = currentUser;
    }

    @ShellMethod(key = "portfolio", value = "Enter portfolio management mode.")
    public void portfolio() {
        if (currentUser.getCurrentUser() == null) {
            System.out.println("Error: You must be logged in to manage stock lists.");
            return;
        }
        while (true) {
            System.out.println("Portfolio Management Menu:");
            System.out.println("1. View your list of Portfolios.");
            System.out.println("2. Access a specific Portfolio.");
            System.out.println("3. Create a new Portfolio.");
            System.out.println("4. Delete a specific Portfolio.");
            System.out.println("0. Exit");

            int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    viewPortfolios();
                    break;
                case 2:
                    accessPortfolio();
                    break;
                case 3:
                    createPortfolio();
                    break;
                case 4:
                    deletePortfolio();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewPortfolios() {
    }

    private void accessPortfolio() {
    }

    private void createPortfolio() {
    }

    private void deletePortfolio() {
    }
    
}
