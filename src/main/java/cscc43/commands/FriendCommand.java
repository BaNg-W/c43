package cscc43.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import cscc43.appUser.AppUser;
import cscc43.appUser.AppUserRepo;
import cscc43.appUser.CurrentUser;
import cscc43.friend.FriendList;
import cscc43.friend.FriendListRepo;
import cscc43.friend.FriendRequest;
import cscc43.friend.FriendRequestRepo;

import java.util.List;
import java.util.Scanner;

@ShellComponent
public class FriendCommand {
    private AppUserRepo appUserRepo;
    private CurrentUser currentUser;
    @Autowired
    private FriendRequestRepo friendRequestRepo;
    @Autowired
    private FriendListRepo friendListRepo;
    

    private Scanner scanner = new Scanner(System.in);

    @ShellMethod(key = "friend", value = "Enter the friend management phase.")
    public void enterFriendPhase() {
        while (true) {
            System.out.println("Friend Management Menu:");
            System.out.println("1. Manage Friend Requests");
            System.out.println("2. Send a Friend Request");
            System.out.println("3. Review Friend List");
            System.out.println("0. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    manageFriendRequests();
                    break;
                case 2:
                    sendFriendRequest();
                    break;
                case 3:
                    reviewFriendList();
                    break;
                case 0:
                    System.out.println("Exiting Friend Management.");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void manageFriendRequests() {
        Integer receiverId = currentUser.getCurrentUser().getUser_id();
        List<FriendRequest> pendingRequests = friendRequestRepo.findPendingRequestsByReceiverId(receiverId);
        if (pendingRequests.isEmpty()) {
            System.out.println("No pending friend requests.");
            return;
        }

        while (true) {
            System.out.println("Pending Friend Requests:");
            for (int i = 0; i < pendingRequests.size(); i++) {
                FriendRequest request = pendingRequests.get(i);
                AppUser sender = appUserRepo.findById(request.getSenderId()).orElse(null);
                System.out.println((i + 1) + ". " + (sender != null ? sender.getUsername() : "Unknown user"));
            }
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 0) {
                return;
            } else if (choice > 0 && choice <= pendingRequests.size()) {
                FriendRequest selectedRequest = pendingRequests.get(choice - 1);
                System.out.println("1. Accept Friend Request");
                System.out.println("2. Reject Friend Request");

                int action = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (action) {
                    case 1:
                        System.out.println(acceptFriendRequest(selectedRequest));
                        pendingRequests.remove(selectedRequest); // Remove from the list after action
                        break;
                    case 2:
                        System.out.println(rejectFriendRequest(selectedRequest));
                        pendingRequests.remove(selectedRequest); // Remove from the list after action
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private String acceptFriendRequest(FriendRequest friendRequest) {
        friendRequest.setStatus("accepted");
        friendRequestRepo.save(friendRequest);

        friendListRepo.save(new FriendList(friendRequest.getReceiverId(), friendRequest.getSenderId()));
        friendListRepo.save(new FriendList(friendRequest.getSenderId(), friendRequest.getReceiverId()));

        AppUser sender = appUserRepo.findById(friendRequest.getSenderId()).orElse(null);
        return "Friend request from " + (sender != null ? sender.getUsername() : "Unknown user") + " accepted.";
    }

    private String rejectFriendRequest(FriendRequest friendRequest) {
        friendRequest.setStatus("rejected");
        friendRequestRepo.save(friendRequest);

        AppUser sender = appUserRepo.findById(friendRequest.getSenderId()).orElse(null);
        return "Friend request from " + (sender != null ? sender.getUsername() : "Unknown user") + " rejected.";
    }

    private void sendFriendRequest() {
        System.out.print("Enter the user ID of the person to send a friend request to: ");
        int userId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        AppUser receiver = appUserRepo.findById(userId).orElse(null);
        if (receiver == null) {
            System.out.println("Error: User with user ID " + userId + " does not exist.");
            return;
        }

        String result = addFriend(userId);
        System.out.println(result);
    }

    private void reviewFriendList() {
        Integer userId = currentUser.getCurrentUser().getUser_id();
        if (userId == null) {
            System.out.println("Error: You must be logged in to view your friends.");
            return;
        }

        while (true) {
            Iterable<FriendList> friends = friendListRepo.findByUserId(userId);
            System.out.println("Your Friends:");
            for (FriendList friend : friends) {
                AppUser friendUser = appUserRepo.findById(friend.getFriendId()).orElse(null);
                System.out.println((friendUser != null ? friendUser.getUsername() : "Unknown user"));
            }
            System.out.println("0. Back");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 0) {
                return;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    @ShellMethod(key = "addFriend", value = "Send a friend request by user ID")
    public String addFriend(@ShellOption int userId) {
        Integer senderId = currentUser.getCurrentUser().getUser_id();

        AppUser receiver = appUserRepo.findById(userId).orElse(null);
        if (receiver == null) {
            return "Error: User with user ID " + userId + " does not exist.";
        }

        FriendRequest friendRequest = new FriendRequest(senderId, userId, "waiting");
        friendRequestRepo.save(friendRequest);
        return "Friend request sent to user ID " + userId;
    }


    @ShellMethod(key = "viewFriends", value = "View your friends.")
    public String viewFriends() {
        Integer userId = currentUser.getCurrentUser().getUser_id();

        Iterable<FriendList> friends = friendListRepo.findByUserId(userId);
        StringBuilder friendList = new StringBuilder("Friend IDs: \n");
        for (FriendList friend : friends) {
            friendList.append(friend.getFriendId()).append("\n");
        }
        return friendList.toString();
    }

    public Availability isFriendCommandAvailable() {
        return currentUser.getCurrentUser() != null ? Availability.available() : Availability.unavailable("You must be logged in to manage friends.");
    }
}
