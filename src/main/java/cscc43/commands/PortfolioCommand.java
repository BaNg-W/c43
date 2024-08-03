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
import cscc43.Stock.StockListItemsRepo;
import cscc43.Stock.StockRepo;
import cscc43.appUser.CurrentUser;
import cscc43.Stock.StockCalculations;

import java.text.SimpleDateFormat;
import java.util.*;
import java.text.DecimalFormat;

@ShellComponent
public class PortfolioCommand {
    private PortfolioRepo portfolioRepo;
    private CurrentUser currentUser;
    private StockRepo stockRepo;
    private PortfolioStockRepo portfolioStockRepo;

    private Scanner scanner = new Scanner(System.in);

    @Autowired
    private StockCalculations stockCalculations;


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
            scanner.nextLine();

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
                System.out.println((i + 1) + ". Portfolio ID: " + portfolios.get(i).getPortfolioId() + ", Cash Balance: " + Math.round(portfolios.get(i).getCashBalance() * 100.0) / 100.0);
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
            System.out.println((i + 1) + ". Portfolio ID: " + portfolios.get(i).getPortfolioId() + ", Cash Balance: " + Math.round(portfolios.get(i).getCashBalance() * 100.0) / 100.0);
        }
        System.out.println("0. Back");

        int choice = scanner.nextInt();
        scanner.nextLine();

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
            System.out.println((i + 1) + ". Portfolio ID: " + portfolios.get(i).getPortfolioId() + ", Cash Balance: " + Math.round(portfolios.get(i).getCashBalance() * 100.0) / 100.0);
        }
        System.out.println("0. Back");

        int choice = scanner.nextInt();
        scanner.nextLine();

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

    private Date[] setPortfolioTimeInterval() {
        System.out.println("\nDo you want to set a time interval for the statistic calculations? (default is all historical data)");
        System.out.println("1. Yes");
        System.out.println("2. No");

        int choice = scanner.nextInt();
        scanner.nextLine();

        Date startDate = null;
        Date endDate = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (choice == 1) {
                    System.out.println("Enter start date (yyyy-MM-dd): ");
                    String startDateStr = scanner.nextLine();
                    System.out.println("Enter end date (yyyy-MM-dd): ");
                    String endDateStr = scanner.nextLine();

                    startDate = new Date(dateFormat.parse(startDateStr).getTime());
                    endDate = new Date(dateFormat.parse(endDateStr).getTime());

            } else {
                startDate = new Date(dateFormat.parse("2013-02-08").getTime());
                endDate = new Date(dateFormat.parse("2018-02-07").getTime());
            }
        } catch (Exception e) {
            System.out.println("Invalid date format. Please try again.");
        }

        return new Date[]{startDate, endDate};
    }

    public void managePortfolio(Portfolio portfolio) {
        Date[] interval = setPortfolioTimeInterval();
        Date startDate = interval[0];
        Date endDate = interval[1];

        // DecimalFormat instance to ensure 2 decimal places
        DecimalFormat df = new DecimalFormat("0.00");

        while (true) {
            List<PortfolioStock> stocks = portfolioStockRepo.findStocksByPortfolio(portfolio.getPortfolioId());

            double totalPortfolioValue = 0; // Start at zero
            System.out.println("\nCash Balance: " + df.format(portfolio.getCashBalance()));

            if (stocks.isEmpty()) {
                System.out.println("No stocks in the portfolio.");
            } else {
                System.out.println("\nStocks in the portfolio:");
                for (PortfolioStock portfolioStock : stocks) {
                    String symbol = portfolioStock.getSymbol();
                    int shares = portfolioStock.getShares();
                    Double close = stockRepo.findLastestStock(symbol).getClose();

                    // Get the latest stock price for this symbol
                    Stock latestStock = stockRepo.findLastestStock(symbol);
                    if (latestStock != null) {
                        double stockWorth = latestStock.getClose() * shares;
                        totalPortfolioValue += stockWorth;

                        // Print stock details
                        System.out.printf("\n%s, Close: %s, Shares: %d, Worth: %s%n", symbol, df.format(close), shares, df.format(stockWorth));

                        // Calculate and display coefficient of variation and Beta
                        Object[] coefVarBeta = stockCalculations.calculateCoefficientOfVariationAndBeta(symbol, "SPX", startDate, endDate);
                        if (coefVarBeta != null && coefVarBeta.length == 2) {
                            double coefVar = (double) coefVarBeta[0];
                            double beta = (double) coefVarBeta[1];
                            System.out.printf("  Coefficient of Variation: %s, Beta: %s%n", df.format(coefVar), df.format(beta));
                        }
                    } else {
                        System.out.println("\nError: Latest stock price not found for symbol " + symbol);
                    }
                }

                // Print total portfolio value
                System.out.printf("\nPortfolio present market value: %s%n", df.format(totalPortfolioValue));

                // Print correlation matrix
                List<Object[]> correlationMatrix = stockCalculations.calculateCorrelationMatrix(portfolio.getPortfolioId(), startDate, endDate);
                System.out.println("\nCorrelation Matrix:");
                
                if (correlationMatrix == null || correlationMatrix.isEmpty()) {
                    System.out.println("No correlation matrix available.");
                    return;
                }
        
                // Extract unique symbols
                Set<String> symbolsSet = new HashSet<>();
                for (Object[] row : correlationMatrix) {
                    symbolsSet.add((String) row[0]);
                    symbolsSet.add((String) row[1]);
                }
                List<String> symbols = new ArrayList<>(symbolsSet);
                Collections.sort(symbols);
        
                int n = symbols.size();
                double[][] matrix = new double[n][n];
        
                // Fill matrix with correlation values
                for (Object[] row : correlationMatrix) {
                    String symbol1 = (String) row[0];
                    String symbol2 = (String) row[1];
                    double correlation = (Double) row[2];
        
                    int i = symbols.indexOf(symbol1);
                    int j = symbols.indexOf(symbol2);
        
                    matrix[i][j] = correlation;
                    matrix[j][i] = correlation;
                }
        
                // Print header row
                System.out.print("\t");
                for (String symbol : symbols) {
                    System.out.print(symbol + "\t");
                }
                System.out.println();
        
                // Print matrix
                for (int i = 0; i < n; i++) {
                    System.out.print(symbols.get(i) + "\t");
                    for (int j = 0; j < n; j++) {
                        System.out.printf("%.2f\t", matrix[i][j]);
                    }
                    System.out.println();
                }
                
                System.out.printf("\nCalculations based on data from %s to %s%n", startDate, endDate);
            }

            System.out.println("\nPortfolio Management:");
            System.out.println("1. Manage Cash Balance");
            System.out.println("2. Manage Stocks");
            System.out.println("3. Display A Stock Graph");
            System.out.println("4. Display A Prediction Graph");
            System.out.println("5. Set Time Interval for Calculations");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine();

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
                    displayPredictionGraph();
                    break;
                case 5:
                    interval = setPortfolioTimeInterval();
                    startDate = interval[0];
                    endDate = interval[1];
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
            scanner.nextLine();

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
        scanner.nextLine();

        if (amount > portfolio.getCashBalance()) {
            System.out.println("Error: Insufficient balance.");
        } else {
            portfolio.setCashBalance(portfolio.getCashBalance() - amount);
            portfolioRepo.save(portfolio);
            System.out.println("Withdrawal successful. New balance: " + Math.round(portfolio.getCashBalance() * 100.0) / 100.0);
        }
    }

    private void depositCash(Portfolio portfolio) {
        System.out.print("Enter amount to deposit: ");
        Double amount = scanner.nextDouble();
        scanner.nextLine();

        portfolio.setCashBalance(portfolio.getCashBalance() + amount);
        portfolioRepo.save(portfolio);
        System.out.println("Deposit successful. New balance: " + Math.round(portfolio.getCashBalance() * 100.0) / 100.0);
    }

    private void manageStocks(Portfolio portfolio) {
         while (true) {
            System.out.println("\nManage Stocks:");
            System.out.println("1. Buy Stock");
            System.out.println("2. Sell Stock");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine();

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

            System.out.println("Purchase successful. New balance: " + Math.round(portfolio.getCashBalance() * 100.0) / 100.0);
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

            System.out.println("Sale successful. New balance: " + Math.round(portfolio.getCashBalance() * 100.0) / 100.0);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid numbers.");
        }
    }

    public void displayStockGraph() {
        System.out.println("Enter time interval for the graph:");
        System.out.println("1. Week");
        System.out.println("2. Month");
        System.out.println("3. Quarter");
        System.out.println("4. Year");
        System.out.println("5. Five Years");
        System.out.println("0. Back");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                displayStockGraphByInterval(7);
                break;
            case 2:
                displayStockGraphByInterval(30);
                break;
            case 3:
                displayStockGraphByInterval(90);
                break;
            case 4:
                displayStockGraphByInterval(365);
                break;
            case 5:
                displayStockGraphByInterval(1825);
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }


    private void displayStockGraphByInterval(long interval) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
    
        Date latestDate = stockRepo.findLastestStock(symbol).getTimestamp();
        Date sqlDate = new Date(latestDate.getTime() - interval * 24 * 60 * 60 * 1000);
    
        try {
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
    
            System.out.println("\nStock prices for symbol " + symbol + " over the last " + interval + " days:\n");
    
            // Print the stock prices in a vertical ASCII chart with price labels
            int chartHeight = 20;
            for (int i = chartHeight; i >= 0; i--) {
                double threshold = minPrice + (maxPrice - minPrice) * i / chartHeight;
                System.out.printf("%8.2f | ", threshold); // Print price label
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
    
        System.out.println("\nPress Enter to go back.");
        scanner.nextLine();
    }


    public void displayPredictionGraph() {
        System.out.println("Enter time interval for the prediction:");
        System.out.println("1. Week");
        System.out.println("2. Month");
        System.out.println("3. Quarter");
        System.out.println("4. Year");
        System.out.println("5. Five Years");
        System.out.println("0. Back");
    
        int choice = scanner.nextInt();
        scanner.nextLine();
    
        switch (choice) {
            case 1:
                displayPredictionGraphByInterval(7);
                break;
            case 2:
                displayPredictionGraphByInterval(30);
                break;
            case 3:
                displayPredictionGraphByInterval(90);
                break;
            case 4:
                displayPredictionGraphByInterval(365);
                break;
            case 5:
                displayPredictionGraphByInterval(1825);
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }


    private void displayPredictionGraphByInterval(int interval) {
        System.out.print("\nEnter stock symbol: ");
        String symbol = scanner.nextLine();
    
        try {
            List<Stock> stockList = stockRepo.findStock(symbol);
            if (stockList.isEmpty()) {
                System.out.println("No stock data found for symbol " + symbol);
                return;
            }
    
            // Extract the closing prices from the stock data
            int dataSize = Math.min(stockList.size(), interval);
            double[] closingPrices = new double[dataSize];
            for (int i = 0; i < dataSize; i++) {
                closingPrices[i] = stockList.get(stockList.size() - dataSize + i).getClose();
            }
    
            // Prepare data for linear regression
            int n = closingPrices.length;
            double[] x = new double[n];
            double[] y = closingPrices;
            for (int i = 0; i < n; i++) {
                x[i] = i + 1;
            }
    
            // Calculate the means of x and y
            double xMean = 0, yMean = 0;
            for (int i = 0; i < n; i++) {
                xMean += x[i];
                yMean += y[i];
            }
            xMean /= n;
            yMean /= n;
    
            double numerator = 0, denominator = 0;
            for (int i = 0; i < n; i++) {
                numerator += (x[i] - xMean) * (y[i] - yMean);
                denominator += (x[i] - xMean) * (x[i] - xMean);
            }
            double slope = numerator / denominator;
            double intercept = yMean - slope * xMean;
    
            // Predict the future price for each day in the given interval
            double[] futurePrices = new double[interval];
            for (int i = 1; i <= interval; i++) {
                futurePrices[i - 1] = slope * (n + i) + intercept;
            }
    
            // Normalize the predicted prices to fit within a fixed height for the ASCII graph
            double maxPrice = Double.MIN_VALUE;
            double minPrice = Double.MAX_VALUE;
            for (double price : futurePrices) {
                if (price > maxPrice) maxPrice = price;
                if (price < minPrice) minPrice = price;
            }
    
            int graphHeight = 20;
            int maxDays = 50;
            char[][] graph = new char[graphHeight][maxDays];
            for (int i = 0; i < graphHeight; i++) {
                for (int j = 0; j < maxDays; j++) {
                    graph[i][j] = ' ';
                }
            }
    
            // Normalize the number of days to 50 if necessary
            int step = Math.max(1, interval / maxDays);
            for (int i = 0; i < maxDays; i++) {
                int index = i * step;
                if (index >= interval) break;
                int normalizedHeight = (int) ((futurePrices[index] - minPrice) / (maxPrice - minPrice) * (graphHeight - 1));
                graph[graphHeight - 1 - normalizedHeight][i] = '*';
            }
    
            // Print the ASCII graph with price labels
            System.out.println("\nPredicted prices for the next " + interval + " days:\n");
            double priceStep = (maxPrice - minPrice) / (graphHeight - 1);
            for (int i = 0; i < graphHeight; i++) {
                double priceLabel = maxPrice - i * priceStep;
                System.out.printf("%8.2f | ", priceLabel);
                for (int j = 0; j < maxDays; j++) {
                    System.out.print(graph[i][j] + " ");
                }
                System.out.println();
            }
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        System.out.println("\nPress Enter to go back.");
        scanner.nextLine();
    }
        
}