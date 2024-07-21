package cscc43.friend;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendListRepo extends CrudRepository<FriendList, Integer> {

    @Query(value = "SELECT * FROM friend_list WHERE user_id = ?1", nativeQuery = true)
    List<FriendList> findByUserId(Integer userId);

    @Query(value = "DELETE FROM friend_list WHERE user_id = ?1 AND friend_id = ?2", nativeQuery = true)
    void deleteFriend(Integer userId, Integer friendId);
}
