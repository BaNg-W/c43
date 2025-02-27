package cscc43.friend;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepo extends CrudRepository<FriendRequest, Integer> {

    @Query(value = "SELECT * FROM friend_request WHERE receiver_id = ?1 AND status = 'waiting'", nativeQuery = true)
    List<FriendRequest> findPendingRequestsByReceiverId(Integer receiverId);

    @Query(value = "SELECT * FROM friend_request WHERE receiver_id = ?1 AND sender_id = ?2 AND status = 'waiting'", nativeQuery = true)
    FriendRequest findSpecficRequest(Integer receiverId, Integer senderId);

    @Query(value = "SELECT * FROM friend_request WHERE request_id = ?1 status = 'waiting'", nativeQuery = true)
    FriendRequest findRequestById(Integer requestId);
}

