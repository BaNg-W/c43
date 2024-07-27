package cscc43.Review;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface ReviewsRepo extends CrudRepository<Reviews, Integer> {

    @Query(value = "SELECT * FROM reviews WHERE stock_list_id = ?1", nativeQuery = true)
    List<Reviews> findByStockListId(Integer stockListId);
    
    @Query(value = "SELECT * FROM reviews WHERE stock_list_id = ?1 AND user_id = ?2", nativeQuery = true)
    Reviews findByStockListIdAndUserId(Integer stockListId, Integer userId);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM reviews WHERE stock_list_id = ?1 AND user_id = ?2", nativeQuery = true)
    void deleteByStockListIdAndUserId(Integer stockListId, Integer userId);
}
