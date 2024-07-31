package cscc43.commands;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import cscc43.portfolio.Portfolio;
import cscc43.portfolio.PortfolioRepo;
import cscc43.portfolio.PortfolioStock;
import cscc43.portfolio.PortfolioStockRepo;
import cscc43.Stock.Stock;
import cscc43.Stock.StockListItems;
import cscc43.Stock.StockListItemsRepo;
import cscc43.Stock.StockRepo;
import cscc43.appUser.CurrentUser;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.TerminalEmulatorDeviceConfiguration;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Map.Entry;

@ShellComponent
public class PortfolioCommand {
    private PortfolioRepo portfolioRepo;
    private CurrentUser currentUser;
    private StockRepo stockRepo;
    private PortfolioStockRepo portfolioStockRepo;

    private Scanner scanner = new Scanner(System.in);

    public PortfolioCommand(PortfolioRepo portfolioRepo, StockListItemsRepo stockListItemsRepo, CurrentUser currentUser, StockRepo stockRepo, PortfolioStockRepo portfolioStockRepo) {
        this.portfolioRepo = portfolioRepo;
        this.portfolioStockRepo = portfolioStockRepo;
        this.currentUser = currentUser;
        this.stockRepo = stockRepo;
    }

    @ShellMethod(key = "portfolio", value = "Enter portfolio management mode.")
    public void portfolio() {
        if (currentUser.getCurrentUser() == null) {
            System.out.println("Error: You must be logged in to manage portfolios.");
            return;
        }
        while (true) {
            System.out.println("\nPortfolio Management Menu:");
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
        Integer userId = currentUser.getCurrentUser().getUser_id();
        List<Portfolio> portfolios = portfolioRepo.findByUserId(userId);

        if (portfolios.isEmpty()) {
            System.out.println("You have no portfolios.");
        } else {
            System.out.println("Your Portfolios:");
            for (int i = 0; i < portfolios.size(); i++) {
                System.out.println((i + 1) + ". Portfolio ID: " + portfolios.get(i).getPortfolioId() + ", Cash Balance: " + portfolios.get(i).getCashBalance());
            }
        }
    }

    private void accessPortfolio() {
        Integer userId = currentUser.getCurrentUser().getUser_id();
        List<Portfolio> portfolios = portfolioRepo.findByUserId(userId);

        if (portfolios.isEmpty()) {
            System.out.println("You have no portfolios to access.");
            return;
        }

        System.out.println("\nSelect a Portfolio by number:");
        for (int i = 0; i < portfolios.size(); i++) {
            System.out.println((i + 1) + ". Portfolio ID: " + portfolios.get(i).getPortfolioId() + ", Cash Balance: " + portfolios.get(i).getCashBalance());
        }
        System.out.println("0. Back");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (choice == 0) {
            return;
        } else if (choice > 0 && choice <= portfolios.size()) {
            Portfolio portfolio = portfolios.get(choice - 1);
            managePortfolio(portfolio);
        } else {
            System.out.println("Invalid choice. Please try again.");
        }
    }

    private void createPortfolio() {

        Double initialBalance = 0.0;

        Integer userId = currentUser.getCurrentUser().getUser_id();

        Portfolio portfolio = new Portfolio(initialBalance, userId);
        portfolioRepo.save(portfolio);
        System.out.println("\nNew portfolio created with ID: " + portfolio.getPortfolioId() + " and initial balance: " + portfolio.getCashBalance());
    }

    private void deletePortfolio() {
        Integer userId = currentUser.getCurrentUser().getUser_id();
        List<Portfolio> portfolios = portfolioRepo.findByUserId(userId);

        if (portfolios.isEmpty()) {
            System.out.println("You have no portfolios to delete.");
            return;
        }

        System.out.println("Select a Portfolio to delete by number:");
        for (int i = 0; i < portfolios.size(); i++) {
            System.out.println((i + 1) + ". Portfolio ID: " + portfolios.get(i).getPortfolioId() + ", Cash Balance: " + portfolios.get(i).getCashBalance());
        }
        System.out.println("0. Back");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (choice == 0) {
            return;
        } else if (choice > 0 && choice <= portfolios.size()) {
            Portfolio portfolio = portfolios.get(choice - 1);
            portfolioRepo.delete(portfolio);
            System.out.println("Portfolio with ID: " + portfolio.getPortfolioId() + " has been deleted.");
        } else {
            System.out.println("Invalid choice. Please try again.");
        }
    }

    private void managePortfolio(Portfolio portfolio) {
    while (true) {

        List<PortfolioStock> stocks = portfolioStockRepo.findStocksByPortfolio(portfolio.getPortfolioId());

        double totalPortfolioValue = portfolio.getCashBalance(); // Start with cash balance
        System.out.println("Cash Balance: " + portfolio.getCashBalance());

        if (stocks.isEmpty()) {
            System.out.println("No stocks in the portfolio.");
        } else {
            System.out.println("Stocks in the portfolio:");
            for (PortfolioStock portfolioStock : stocks) {
                String symbol = portfolioStock.getSymbol();
                int shares = portfolioStock.getShares();

                // Get the latest stock price for this symbol
                Stock latestStock = stockRepo.findLastestStock(symbol);
                if (latestStock != null) {
                    double pricePerShare = latestStock.getClose();
                    double stockValue = shares * pricePerShare;
                    totalPortfolioValue += stockValue; // Add stock value to total portfolio value

                    // Print stock details
                    System.out.println("Symbol: " + symbol + ", Shares: " + shares + ", Worth: " + stockValue);
                } else {
                    System.out.println("Error: Latest stock price not found for symbol " + symbol);
                }
            }

            // Print total portfolio value
            System.out.println("Total portfolio worth: " + totalPortfolioValue);
        }

        System.out.println("\nPortfolio Management:");
        System.out.println("1. Manage Cash Balance");
        System.out.println("2. Manage Stocks");
        System.out.println("3. Display A Stock Graph");
        System.out.println("0. Back");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
                manageCash(portfolio);
                break;
            case 2:
                manageStocks(portfolio);
                break;
            case 3:
                displayStockGraph();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
}




    private void manageCash(Portfolio portfolio) {
        while (true) {
            System.out.println("Manage Cash Balance:");
            System.out.println("1. Withdraw Cash");
            System.out.println("2. Deposit Cash");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    withdrawCash(portfolio);
                    break;
                case 2:
                    depositCash(portfolio);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void withdrawCash(Portfolio portfolio) {
        System.out.print("Enter amount to withdraw: ");
        Double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        if (amount > portfolio.getCashBalance()) {
            System.out.println("Error: Insufficient balance.");
        } else {
            portfolio.setCashBalance(portfolio.getCashBalance() - amount);
            portfolioRepo.save(portfolio);
            System.out.println("Withdrawal successful. New balance: " + portfolio.getCashBalance());
        }
    }

    private void depositCash(Portfolio portfolio) {
        System.out.print("Enter amount to deposit: ");
        Double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        portfolio.setCashBalance(portfolio.getCashBalance() + amount);
        portfolioRepo.save(portfolio);
        System.out.println("Deposit successful. New balance: " + portfolio.getCashBalance());
    }

    private void manageStocks(Portfolio portfolio) {
         while (true) {
            System.out.println("Manage Stocks:");
            System.out.println("1. Buy Stock");
            System.out.println("2. Sell Stock");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    buyStock(portfolio);
                    break;
                case 2:
                    sellStock(portfolio);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

     private void buyStock(Portfolio portfolio) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();

        System.out.print("Enter number of shares to buy: ");
        try {
            int shares = Integer.parseInt(scanner.nextLine());

            Stock latestStock = stockRepo.findLastestStock(symbol);
            if (latestStock == null) {
                System.out.println("Error: Stock symbol not found.");
                return;
            }

            Double pricePerShare = latestStock.getClose();
            Double totalCost = shares * pricePerShare;

            if (totalCost > portfolio.getCashBalance()) {
                System.out.println("Error: Insufficient cash to complete the purchase.");
                return;
            }

            portfolio.setCashBalance(portfolio.getCashBalance() - totalCost);
            portfolioRepo.save(portfolio);

            PortfolioStock existingStock = portfolioStockRepo.findByPortfolioIdAndSymbol(portfolio.getPortfolioId(), symbol);
            if (existingStock != null) {
                existingStock.setShares(existingStock.getShares() + shares);
                portfolioStockRepo.save(existingStock);
            } else {
                PortfolioStock newStock = new PortfolioStock(portfolio.getPortfolioId(), symbol, shares);
                portfolioStockRepo.save(newStock);
            }

            System.out.println("Purchase successful. New balance: " + portfolio.getCashBalance());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers.");
        }
    }

    private void sellStock(Portfolio portfolio) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();

        System.out.print("Enter number of shares to sell: ");
        try {
            int shares = Integer.parseInt(scanner.nextLine());

            Stock latestStock = stockRepo.findLastestStock(symbol);
            if (latestStock== null) {
                System.out.println("Error: Stock symbol not found.");
                return;
            }

            Double pricePerShare = latestStock.getClose();
            Double totalRevenue = shares * pricePerShare;

            PortfolioStock existingStock = portfolioStockRepo.findByPortfolioIdAndSymbol(portfolio.getPortfolioId(), symbol);
            if (existingStock == null) {
                System.out.println("Error: No such stock found in portfolio.");
                return;
            }

            if (shares > existingStock.getShares()) {
                System.out.println("Error: Not enough shares to sell.");
                return;
            }

            existingStock.setShares(existingStock.getShares() - shares);
            if (existingStock.getShares() == 0) {
                portfolioStockRepo.deleteByPortfolioIdAndSymbol(portfolio.getPortfolioId(), symbol);
            } else {
                portfolioStockRepo.save(existingStock);
            }

            portfolio.setCashBalance(portfolio.getCashBalance() + totalRevenue);
            portfolioRepo.save(portfolio);

            System.out.println("Sale successful. New balance: " + portfolio.getCashBalance());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers.");
        }
    }



    // timestamp signifies the date AFTER for which the stock prices are to be displayed
    public void displayStockGraph() {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
    
        String dateString = "2013-01-01"; //FIX THIS to not be a static date later
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedDate;
        try {
            parsedDate = dateFormat.parse(dateString);
            java.sql.Date sqlDate = new java.sql.Date(parsedDate.getTime());
            List<Stock> stockList = stockRepo.findStockAfterDate(symbol, sqlDate);
            if (stockList.isEmpty()) {
                System.out.println("No stock data found for symbol " + symbol);
                return;
            }
            
            // Initialize Lanterna terminal
            DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
            terminalFactory.setTerminalEmulatorTitle("Stock Graph");
            
            Terminal terminal = terminalFactory.createTerminal();
            Screen screen = terminalFactory.createScreen();
            screen.startScreen();
            TextGraphics textGraphics = screen.newTextGraphics();

            // Determine the graph dimensions
            int width = 80;
            int height = 20;
            int maxPrice = (int) stockList.stream().mapToDouble(Stock::getClose).max().orElse(1);
            int minPrice = (int) stockList.stream().mapToDouble(Stock::getClose).min().orElse(0);

            // Draw the axes
            textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
            for (int i = 0; i < width; i++) {
                textGraphics.putString(i, height, "-");
            }
            for (int i = 0; i < height; i++) {
                textGraphics.putString(0, i, "|");
            }

            // Plot the stock prices
            textGraphics.setForegroundColor(TextColor.ANSI.GREEN);
            for (int i = 0; i < stockList.size(); i++) {
                Stock stock = stockList.get(i);
                int x = i + 1;
                int y = height - ((int) (stock.getClose() - minPrice) * height / (maxPrice - minPrice));
                textGraphics.putString(x, y, "*");
            }

            // Refresh the screen to show the graph
            screen.refresh();

            // Wait for user input before closing
            terminal.readInput();
            screen.stopScreen();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
