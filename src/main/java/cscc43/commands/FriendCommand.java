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

    public FriendCommand(AppUserRepo appUserRepo, CurrentUser currentUser) {
        this.appUserRepo = appUserRepo;
        this.currentUser = currentUser;
    }

    @Autowired
    private FriendRequestRepo friendRequestRepo;
    @Autowired
    private FriendListRepo friendListRepo;

    private Scanner scanner = new Scanner(System.in);

    @ShellMethod(key = "friend", value = "Enter the friend management phase.")
    public void enterFriendPhase() {
        if (currentUser.getCurrentUser() == null) {
            System.out.println("Error: You must be logged in to view your friends.");
            return;
        } else {
            Integer userId = currentUser.getCurrentUser().getUser_id();
            while (true) {
                System.out.println("Friend Management Menu:");
                System.out.println("1. Manage Friend Requests");
                System.out.println("2. Send a Friend Request");
                System.out.println("3. Remove a Friend. ");
                System.out.println("4. Review Friend List");
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
                        removeFriend();
                        break;
                    case 4:
                        reviewFriendList(userId);
                        break;
                    case 0:
                        System.out.println("Exiting Friend Management.");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
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
        System.out.print("Enter the username of the person to send a friend request to: \n");
        String username = scanner.nextLine();

        AppUser receiver = appUserRepo.findByUsername(username);
        if (receiver == null) {
            System.out.println("Error: User with username " + username + " does not exist.");
        } else if (friendRequestRepo.findSpecficRequest(receiver.getUser_id(), currentUser.getCurrentUser().getUser_id()) != null) {
            System.out.println("You already have a pending friend request with " + username + ".");
        } else {
            FriendRequest friendRequest = new FriendRequest(currentUser.getCurrentUser().getUser_id(), receiver.getUser_id(), "waiting");
            friendRequestRepo.save(friendRequest);
            System.out.println("Friend request sent to user " + username + ".");
        }
    }

    private void reviewFriendList(Integer userId) {
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


    public void removeFriend() {
        System.out.print("Enter the username of the person you wish to remove: \n");
        String friendUsername = scanner.nextLine();

        if (appUserRepo.findByUsername(friendUsername) == null) {
            System.out.println("Error: Friend with username " + friendUsername + " not found in your friend list.");
            return;
        }

        Integer friendId = appUserRepo.findByUsername(friendUsername).getUser_id();
        Integer userId = currentUser.getCurrentUser().getUser_id();

        // Check if the friend exists in the user's friend list
        FriendList friend = friendListRepo.findByUserId(userId).stream()
            .filter(f -> f.getFriendId().equals(friendId))
            .findFirst()
            .orElse(null);

        if (friend == null) {
            System.out.println("Error: Friend with username " + friendUsername + " not found in your friend list.");
            return;
        }
        
        // Delete the friend relationship for both directions
        friendListRepo.deleteByUserIdAndFriendId(userId, friendId);
        friendListRepo.deleteByUserIdAndFriendId(friendId, userId);

        System.out.println("Friend with user ID " + friendId + " has been removed.");
    }

    public Availability isFriendCommandAvailable() {
        return currentUser.getCurrentUser() != null ? Availability.available() : Availability.unavailable("You must be logged in to manage friends.");
    }
}
