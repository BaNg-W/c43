package cscc43.Stock;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockListsRepo extends CrudRepository<StockLists, Integer> {

    @Query(value = "SELECT * FROM stock_lists WHERE creator_id = ?1", nativeQuery = true)
    List<StockLists> findByCreatorId(Integer creatorId);
    
    @Query(value = "SELECT * FROM stock_lists", nativeQuery = true)
    List<StockLists> findAll();
}
