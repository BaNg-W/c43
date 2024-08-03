package cscc43.commands;

import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import cscc43.appUser.AppUser;
import cscc43.appUser.AppUserRepo;
import cscc43.appUser.CurrentUser;
import cscc43.friend.FriendListRepo;
import cscc43.Stock.StockLists;
import cscc43.Stock.StockListsRepo;
import cscc43.Stock.StockListItems;
import cscc43.Stock.StockListItemsRepo;
import cscc43.Review.Reviews;
import cscc43.Review.ReviewsRepo;
import cscc43.Stock.Stock;
import cscc43.Stock.StockRepo;
import cscc43.Stock.StockCalculations;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.text.DecimalFormat;

@ShellComponent
public class StockListCommand {
    private final StockListsRepo stockListsRepo;
    private final StockListItemsRepo stockListItemsRepo;
    private final ReviewsRepo reviewsRepo;
    private final AppUserRepo appUserRepo;
    private final FriendListRepo friendListRepo;
    private final CurrentUser currentUser;
    private StockRepo stockRepo;

    private StockCalculations stockCalculations;

    private final Scanner scanner = new Scanner(System.in);

    public StockListCommand(StockListsRepo stockListsRepo, StockListItemsRepo stockListItemsRepo, ReviewsRepo reviewsRepo, AppUserRepo appUserRepo, FriendListRepo friendListRepo, CurrentUser currentUser, StockRepo stockRepo, StockCalculations stockCalculations) {
        this.stockListsRepo = stockListsRepo;
        this.stockListItemsRepo = stockListItemsRepo;
        this.reviewsRepo = reviewsRepo;
        this.appUserRepo = appUserRepo;
        this.friendListRepo = friendListRepo;
        this.currentUser = currentUser;
        this.stockRepo = stockRepo;
        this.stockCalculations = stockCalculations;
    }

    @ShellMethod(key = "stocklist", value = "Enter stock list management mode.")
    public void enterStockListPhase() {
        if (currentUser.getCurrentUser() == null) {
            System.out.println("Error: You must be logged in to manage stock lists.");
            return;
        }

        while (true) {
            System.out.println("\nStock List Management Menu:");
            System.out.println("1. View accessible stock lists");
            System.out.println("2. Manage your stock lists");
            System.out.println("0. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    viewAccessibleStockLists();
                    break;
                case 2:
                    manageOwnStockLists();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewAccessibleStockLists() {
        Integer userId = currentUser.getCurrentUser().getUser_id();
        List<StockLists> accessibleStockLists = stockListsRepo.findAllStockLists();

        accessibleStockLists.removeIf(stockList -> {
            if ("private".equals(stockList.getPublicity())) {
                return !stockList.getCreatorId().equals(userId);
            } else if ("friendsOnly".equals(stockList.getPublicity())) {
                boolean isFriend = friendListRepo.findByUserId(userId).stream()
                        .anyMatch(f -> f.getFriendId().equals(stockList.getCreatorId()));
                return !isFriend && !stockList.getCreatorId().equals(userId);
            }
            return false;
        });

        while (true) {
            System.out.println("\nAccessible Stock Lists:");
            for (int i = 0; i < accessibleStockLists.size(); i++) {
                StockLists stockList = accessibleStockLists.get(i);
                AppUser creator = appUserRepo.findById(stockList.getCreatorId()).orElse(null);
                String creatorName = creator != null ? creator.getUsername() : "Unknown user";
                System.out.println((i + 1) + ". " + stockList.getName() + " (Creator: " + creatorName + ")");
            }
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                return;
            } else if (choice > 0 && choice <= accessibleStockLists.size()) {
                StockLists selectedStockList = accessibleStockLists.get(choice - 1);
                viewStockListDetails(selectedStockList);
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void viewStockListDetails(StockLists stockList) {
        Date[] interval = setStockListTimeInterval();
         DecimalFormat df = new DecimalFormat("0.00");
        Date startDate = interval[0];
        Date endDate = interval[1];

        while (true) {
            System.out.println("\nStock List: " + stockList.getName());
            List<StockListItems> items = stockListItemsRepo.findByStockListId(stockList.getStockListsId());

            if (items.isEmpty()) {
                System.out.println("No stocks in this list.");
            } else {
                for (StockListItems item : items) {
                    String symbol = item.getSymbol();
                    int shares = item.getShares();

                    // Get the latest stock price for this symbol
                    Stock latestStock = stockRepo.findLastestStock(symbol);
                    if (latestStock != null) {
                        double stockWorth = latestStock.getClose() * shares;

                        // Print stock details
                        System.out.printf("\nStock: %s, Shares: %d, Worth: %.2f%n", symbol, shares, stockWorth);

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

                // Print correlation matrix
                List<Object[]> correlationMatrix = stockCalculations.calculateCorrelationMatrixST(stockList.getStockListsId(), startDate, endDate);
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
                
            }

            displayReviews(stockList);
            System.out.println("1. Write or edit a review");
            System.out.println("2. Set Time Interval for Calculations");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    writeOrEditReview(stockList);
                    break;
                case 2:
                    interval = setStockListTimeInterval();
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

    private Date[] setStockListTimeInterval() {
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



    private void displayReviews(StockLists stockList) {
        List<Reviews> reviews = reviewsRepo.findByStockListId(stockList.getStockListsId());
        System.out.println("\nReviews:");
        for (Reviews review : reviews) {
            AppUser reviewer = appUserRepo.findById(review.getUserId()).orElse(null);
            String reviewerName = reviewer != null ? reviewer.getUsername() : "Unknown user";
            System.out.println("Review by " + reviewerName + ":" + review.getReviewText());
            System.out.println();
        }
    }

    private void writeOrEditReview(StockLists stockList) {
        Integer userId = currentUser.getCurrentUser().getUser_id();
        Reviews existingReview = reviewsRepo.findByStockListIdAndUserId(stockList.getStockListsId(), userId);
        if (existingReview != null) {
            System.out.println("\nYour existing review: " + existingReview.getReviewText());
        } else {
            existingReview = new Reviews();
            existingReview.setStockListId(stockList.getStockListsId());
            existingReview.setUserId(userId);
        }

        System.out.print("\nEnter your review (max 4000 characters): ");
        String reviewText = scanner.nextLine();
        existingReview.setReviewText(reviewText);
        reviewsRepo.save(existingReview);
        System.out.println("Review saved successfully.");
    }

    private void manageOwnStockLists() {
        while (true) {
            Integer userId = currentUser.getCurrentUser().getUser_id();
            List<StockLists> stockLists = stockListsRepo.findByCreatorId(userId);

            System.out.println("\nYour Stock Lists:");
            for (int i = 0; i < stockLists.size(); i++) {
                System.out.println((i + 1) + ". " + stockLists.get(i).getName());
            }
            System.out.println((stockLists.size() + 1) + ". Create new empty stock list");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 0) {
                return;
            } else if (choice == (stockLists.size() + 1)) {
                createEmptyStockList();
            } else if (choice > 0 && choice <= stockLists.size()) {
                StockLists selectedStockList = stockLists.get(choice - 1);
                manageStockList(selectedStockList);
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void createEmptyStockList() {
        System.out.print("Enter the name of the new stock list: ");
        String name = scanner.nextLine();

        Integer userId = currentUser.getCurrentUser().getUser_id();

        StockLists newStockList = new StockLists(userId, name, "private");

        stockListsRepo.save(newStockList);

        System.out.println("New stock list created successfully.");
    }


    private void manageStockList(StockLists stockList) {
        while (true) {
            System.out.println("\nStock List: " + stockList.getName());
            List<StockListItems> items = stockListItemsRepo.findByStockListId(stockList.getStockListsId());
            for (StockListItems item : items) {
                System.out.println("Stock: " + item.getSymbol() + ", Shares: " + item.getShares());
            }

            System.out.println("1. Change publicity");
            System.out.println("2. Delete stock list");
            System.out.println("3. Add/Remove stocks");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    changePublicity(stockList);
                    break;
                case 2:
                    deleteStockList(stockList);
                    return;
                case 3:
                    manageStocks(stockList);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void changePublicity(StockLists stockList) {
        System.out.print("\nEnter new publicity (all/friendsOnly/private): ");
        String publicity = scanner.nextLine();
        stockList.setPublicity(publicity);
        stockListsRepo.save(stockList);
        System.out.println("Publicity updated successfully.");
    }

    private void deleteStockList(StockLists stockList) {
        stockListsRepo.delete(stockList);
        System.out.println("Stock list deleted successfully.");
    }

    private void manageStocks(StockLists stockList) {
        while (true) {
            System.out.println("\nManage Stocks in " + stockList.getName());
            System.out.println("1. Add Stock");
            System.out.println("2. Remove Stock");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addStock(stockList);
                    break;
                case 2:
                    removeStock(stockList);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void addStock(StockLists stockList) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();
        System.out.print("Enter shares: ");
        int shares = scanner.nextInt();
        scanner.nextLine();

        if (stockRepo.findLastestStock(symbol) == null) {
            System.out.println("Error: Stock not found.");
            return;
        }

        StockListItems stockListItem = new StockListItems(stockList.getStockListsId(), symbol, shares);
        stockListItemsRepo.save(stockListItem);

        System.out.println("Stock added successfully.");
    }

    private void removeStock(StockLists stockList) {
        System.out.print("Enter stock symbol: ");
        String symbol = scanner.nextLine();

        List<StockListItems> items = stockListItemsRepo.findByStockListIdAndSymbol(stockList.getStockListsId(), symbol);
        if (items.isEmpty()) {
            System.out.println("Stock not found.");
        } else {
            stockListItemsRepo.delete(items.get(0));
            System.out.println("Stock removed successfully.");
        }
    }

    public Availability isStockListCommandAvailable() {
        return currentUser.getCurrentUser() != null ? Availability.available() : Availability.unavailable("You must be logged in to manage stock lists.");
    }
}
