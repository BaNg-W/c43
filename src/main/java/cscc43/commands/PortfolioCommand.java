package cscc43.commands;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import cscc43.portfolio.Portfolio;
import cscc43.portfolio.PortfolioRepo;
import cscc43.portfolio.PortfolioStock;
import cscc43.portfolio.PortfolioStockRepo;
import cscc43.Stock.Stock;
import cscc43.Stock.StockListItems;
import cscc43.Stock.StockListItemsRepo;
import cscc43.Stock.StockRepo;
import cscc43.appUser.CurrentUser;

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

    private void calculateAndPrintCovariance() {
        System.out.print("Enter the first stock symbol: ");
        String symbol1 = scanner.nextLine();
        System.out.print("Enter the second stock symbol: ");
        String symbol2 = scanner.nextLine();

        double covariance = calculateCovariance(symbol1, symbol2);
        System.out.printf("Covariance between %s and %s: %.2f\n", symbol1, symbol2, covariance);
    }

    public void managePortfolio(Portfolio portfolio) {
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

                        // Calculate and display coefficient of variation and Beta
                        double coefficientOfVariation = calculateCoefficientOfVariation(symbol);
                        System.out.println("Coefficient of Variation: " + coefficientOfVariation);

                        // Removed Beta calculation from here as it's handled separately
                    } else {
                        System.out.println("Error: Latest stock price not found for symbol " + symbol);
                    }
                }

                // Print total portfolio value
                System.out.println("Total portfolio worth: " + totalPortfolioValue);

                displayCorrelationMatrix(stocks);
            }

            System.out.println("\nPortfolio Management:");
            System.out.println("1. Manage Cash Balance");
            System.out.println("2. Manage Stocks");
            System.out.println("3. Display A Stock Graph");
            System.out.println("4. Calculate Covariance Between Two Stocks");
            System.out.println("5. Calculate Beta of a Stock");
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
                case 4:
                    calculateAndPrintCovariance();
                    break;
                case 5:
                    calculateAndPrintBeta();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void calculateAndPrintBeta() {
        System.out.print("Enter the stock symbol: ");
        String stockSymbol = scanner.nextLine();
        System.out.print("Enter the market index symbol: ");
        String marketSymbol = scanner.nextLine();

        double beta = calculateBeta(stockSymbol, marketSymbol);
        System.out.printf("Beta of %s relative to %s: %.2f\n", stockSymbol, marketSymbol, beta);
    }


    private double calculateCoefficientOfVariation(String symbol) {
        List<Stock> stockData = stockRepo.findBySymbol(symbol);
        if (stockData == null || stockData.isEmpty()) {
            return 0.0;
        }

        double mean = stockData.stream().mapToDouble(Stock::getClose).average().orElse(0.0);
        double variance = stockData.stream()
            .mapToDouble(stock -> Math.pow(stock.getClose() - mean, 2))
            .average().orElse(0.0);

        double standardDeviation = Math.sqrt(variance);
        return standardDeviation / mean;
    }

    private double calculateBeta(String symbol, String marketSymbol) {
        List<Stock> stockData = stockRepo.findBySymbol(symbol);
        List<Stock> marketData = stockRepo.findBySymbol(marketSymbol);

        if (stockData == null || stockData.isEmpty() || marketData == null || marketData.isEmpty()) {
            return 0.0;
        }

        double covariance = calculateCovariance(symbol, marketSymbol);
        double marketVariance = calculateVariance(marketSymbol);

        return covariance / marketVariance;
    }
    
    private double calculateCovariance(String symbol1, String symbol2) {
        List<Stock> data1 = stockRepo.findBySymbol(symbol1);
        List<Stock> data2 = stockRepo.findBySymbol(symbol2);
    
        if (data1 == null || data1.isEmpty() || data2 == null || data2.isEmpty() || data1.size() != data2.size()) {
            return 0.0;
        }
    
        double mean1 = data1.stream().mapToDouble(Stock::getClose).average().orElse(0.0);
        double mean2 = data2.stream().mapToDouble(Stock::getClose).average().orElse(0.0);
    
        double covariance = 0.0;
        for (int i = 0; i < data1.size(); i++) {
            double diff1 = data1.get(i).getClose() - mean1;
            double diff2 = data2.get(i).getClose() - mean2;
            covariance += diff1 * diff2;
        }
    
        return covariance / data1.size();
    }
    
    private double calculateVariance(String symbol) {
        List<Stock> stockData = stockRepo.findBySymbol(symbol);
        if (stockData == null || stockData.isEmpty()) {
            return 0.0;
        }
    
        double mean = stockData.stream().mapToDouble(Stock::getClose).average().orElse(0.0);
        return stockData.stream()
            .mapToDouble(stock -> Math.pow(stock.getClose() - mean, 2))
            .average().orElse(0.0);
    }
    

    private void displayCorrelationMatrix(List<PortfolioStock> stocks) {
        System.out.println("\nCorrelation Matrix:");
        for (PortfolioStock stock1 : stocks) {
            for (PortfolioStock stock2 : stocks) {
                double covariance = calculateCovariance(stock1.getSymbol(), stock2.getSymbol());
                double variance1 = calculateVariance(stock1.getSymbol());
                double variance2 = calculateVariance(stock2.getSymbol());
                double correlation = covariance / (Math.sqrt(variance1) * Math.sqrt(variance2));
                System.out.print(correlation + "\t");
            }
            System.out.println();
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
            
            // Determine the range of stock prices
            double minPrice = Double.MAX_VALUE;
            double maxPrice = Double.MIN_VALUE;
            for (Stock stock : stockList) {
                if (stock.getClose() < minPrice) {
                    minPrice = stock.getClose();
                }
                if (stock.getClose() > maxPrice) {
                    maxPrice = stock.getClose();
                }
            }

            // Normalize the number of dates to 50 if necessary
            int maxDates = 50;
            int step = Math.max(1, stockList.size() / maxDates);
            List<Stock> normalizedStockList = new ArrayList<>();
            for (int i = 0; i < stockList.size(); i += step) {
                normalizedStockList.add(stockList.get(i));
            }

            // Print the stock prices in a vertical ASCII chart
            int chartHeight = 20; // Height of the chart
            for (int i = chartHeight; i >= 0; i--) {
                double threshold = minPrice + (maxPrice - minPrice) * i / chartHeight;
                for (Stock stock : normalizedStockList) {
                    if (stock.getClose() >= threshold) {
                        System.out.print("* ");
                    } else {
                        System.out.print("  ");
                    }
                }
                System.out.println();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
