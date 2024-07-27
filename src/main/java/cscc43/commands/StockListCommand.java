package cscc43.commands;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.List;
import java.util.Scanner;

@ShellComponent
public class StockListCommand {
    private final StockListsRepo stockListsRepo;
    private final StockListItemsRepo stockListItemsRepo;
    private final ReviewsRepo reviewsRepo;
    private final AppUserRepo appUserRepo;
    private final FriendListRepo friendListRepo;
    private final CurrentUser currentUser;

    private final Scanner scanner = new Scanner(System.in);

    @Autowired
    public StockListCommand(StockListsRepo stockListsRepo, StockListItemsRepo stockListItemsRepo, ReviewsRepo reviewsRepo, AppUserRepo appUserRepo, FriendListRepo friendListRepo, CurrentUser currentUser) {
        this.stockListsRepo = stockListsRepo;
        this.stockListItemsRepo = stockListItemsRepo;
        this.reviewsRepo = reviewsRepo;
        this.appUserRepo = appUserRepo;
        this.friendListRepo = friendListRepo;
        this.currentUser = currentUser;
    }

    @ShellMethod(key = "stocklist", value = "Enter the stock list management phase.")
    public void enterStockListPhase() {
        if (currentUser.getCurrentUser() == null) {
            System.out.println("Error: You must be logged in to manage stock lists.");
            return;
        }

        while (true) {
            System.out.println("Stock List Management Menu:");
            System.out.println("1. View accessible stock lists");
            System.out.println("2. Manage your stock lists");
            System.out.println("0. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

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
        List<StockLists> accessibleStockLists = stockListsRepo.findAll();

        accessibleStockLists.removeIf(stockList -> {
            if (!"all".equals(stockList.getPublicity())) {
                boolean isFriend = friendListRepo.findByUserId(userId).stream()
                        .anyMatch(f -> f.getFriendId().equals(stockList.getCreatorId()));
                return !isFriend && !stockList.getCreatorId().equals(userId);
            }
            return false;
        });

        while (true) {
            System.out.println("Accessible Stock Lists:");
            for (int i = 0; i < accessibleStockLists.size(); i++) {
                StockLists stockList = accessibleStockLists.get(i);
                AppUser creator = appUserRepo.findById(stockList.getCreatorId()).orElse(null);
                String creatorName = creator != null ? creator.getUsername() : "Unknown user";
                System.out.println((i + 1) + ". " + stockList.getName() + " (Creator: " + creatorName + ")");
            }
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

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
        while (true) {
            System.out.println("Stock List: " + stockList.getName());
            List<StockListItems> items = stockListItemsRepo.findByStockListId(stockList.getStockListsId());
            for (StockListItems item : items) {
                System.out.println("Stock: " + item.getSymbol() + ", Shares: " + item.getShares());
            }

            System.out.println("1. Write or edit a review");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    writeOrEditReview(stockList);
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void writeOrEditReview(StockLists stockList) {
        Integer userId = currentUser.getCurrentUser().getUser_id();
        Reviews existingReview = reviewsRepo.findByStockListIdAndUserId(stockList.getStockListsId(), userId);
        if (existingReview != null) {
            System.out.println("Your existing review: " + existingReview.getReviewText());
        } else {
            existingReview = new Reviews();
            existingReview.setStockListId(stockList.getStockListsId());
            existingReview.setUserId(userId);
        }

        System.out.print("Enter your review (max 4000 characters): ");
        String reviewText = scanner.nextLine();
        existingReview.setReviewText(reviewText);
        reviewsRepo.save(existingReview);
        System.out.println("Review saved successfully.");
    }

    private void manageOwnStockLists() {
        Integer userId = currentUser.getCurrentUser().getUser_id();
        List<StockLists> stockLists = stockListsRepo.findByCreatorId(userId);
        while (true) {
            System.out.println("Your Stock Lists:");
            for (int i = 0; i < stockLists.size(); i++) {
                System.out.println((i + 1) + ". " + stockLists.get(i).getName());
            }
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 0) {
                return;
            } else if (choice > 0 && choice <= stockLists.size()) {
                StockLists selectedStockList = stockLists.get(choice - 1);
                manageStockList(selectedStockList);
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void manageStockList(StockLists stockList) {
        while (true) {
            System.out.println("Stock List: " + stockList.getName());
            List<StockListItems> items = stockListItemsRepo.findByStockListId(stockList.getStockListsId());
            for (StockListItems item : items) {
                System.out.println("Stock: " + item.getSymbol() + ", Shares: " + item.getShares());
            }

            System.out.println("1. Change publicity");
            System.out.println("2. Delete stock list");
            System.out.println("3. Add/Remove stocks");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

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
        System.out.print("Enter new publicity (all/friendsonly): ");
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
            System.out.println("Manage Stocks in " + stockList.getName());
            System.out.println("1. Add Stock");
            System.out.println("2. Remove Stock");
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

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
        scanner.nextLine(); // Consume newline

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
