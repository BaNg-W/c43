package cscc43.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import cscc43.appUser.AppUserRepo;
import cscc43.appUser.CurrentUser;
import cscc43.friend.FriendList;
import cscc43.friend.FriendListRepo;
import cscc43.friend.FriendRequest;
import cscc43.friend.FriendRequestRepo;
import cscc43.appUser.AppUser;

@ShellComponent
public class FriendCommand {
    private final AppUserRepo appUserRepo;
    private final CurrentUser currentUser;

    @Autowired
    public FriendCommand(AppUserRepo appUserRepo, CurrentUser currentUser) {
        this.appUserRepo = appUserRepo;
        this.currentUser = currentUser;
    }
    //write code for manage friend request
    @Autowired
    private FriendRequestRepo friendRequestRepo;
    @Autowired
    private FriendListRepo friendListRepo;
    
    @ShellMethod(key = "addFriend", value = "Add friend to friend list.")
    public String addFriend(@ShellOption String username) {
        Integer senderId = currentUser.getCurrentUser().getUser_id();
        AppUser receiver = appUserRepo.findByUsername(username);
        if (receiver == null) {
            return "Error: User with username " + username + " does not exist.";
        }
        FriendRequest friendRequest = new FriendRequest(senderId, receiver.getUserId(), "waiting");
        friendRequestRepo.save(friendRequest);
        return "Friend request sent to " + username;
    }

    @ShellMethod(key = "acceptFriend", value = "Accept a friend request by username")
    public String acceptFriend(@ShellOption String username) {
        Integer receiverId = currentUser.getCurrentUser().getUser_id();//TBD

        FriendRequest friendRequest = friendRequestRepo.findPendingRequest(sender.getUserId(), receiverId);
        if (friendRequest == null) {
            return "Error: No pending friend request from " + username;
        }

        friendRequest.setStatus("accepted");
        friendRequestRepo.save(friendRequest);

        friendListRepo.save(new FriendList(receiverId, sender.getUserId()));
        //friendListRepo.save(new FriendList(sender.getUserId(), receiverId));

        return "Friend request from " + username + " accepted.";
    }

    @ShellMethod(key = "rejectFriend", value = "Reject a friend request by username")
    public String rejectFriend(@ShellOption String username) {
        Integer receiverId = currentUser.getCurrentUser().getUser_id();;
        if (receiverId == null) {
            return "Error: You must be logged in to reject a friend request.";
        }

        AppUser sender = appUserRepo.findByUsername(username);
        if (sender == null) {
            return "Error: User with username " + username + " does not exist.";
        }

        FriendRequest friendRequest = friendRequestRepo.findPendingRequest(sender.getUserId(), receiverId);
        if (friendRequest == null) {
            return "Error: No pending friend request from " + username;
        }

        friendRequest.setStatus("rejected");
        friendRequestRepo.save(friendRequest);

        return "Friend request from " + username + " rejected.";
    }

    @ShellMethod(key = "viewFriends", value = "View your friends.")
    public String viewFriends() {
        Integer userId = currentUser.getCurrentUser().getUser_id();
        if (userId == null) {
            return "Error: You must be logged in to view your friends.";
        }

        Iterable<FriendList> friends = friendListRepo.findByUserId(userId);
        StringBuilder friendList = new StringBuilder("Friend IDs: \n");
        for (FriendList friend : friends) {
            friendList.append(friend.getFriendId()).append("\n");
        }
        return friendList.toString();
    }

}