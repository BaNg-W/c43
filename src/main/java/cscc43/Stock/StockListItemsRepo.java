package cscc43.Stock;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface StockListItemsRepo extends CrudRepository<StockListItems, Integer> {

    @Query(value = "SELECT * FROM stock_list_items WHERE stocklist_id = ?1", nativeQuery = true)
    List<StockListItems> findByStockListId(Integer stockListId);
    
    @Query(value = "SELECT * FROM stock_list_items WHERE stocklist_id = ?1 AND symbol = ?2", nativeQuery = true)
    List<StockListItems> findByStockListIdAndSymbol(Integer stockListId, String symbol);
    
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM stock_list_items WHERE stocklist_id = ?1 AND symbol = ?2", nativeQuery = true)
    void deleteByStockListIdAndSymbol(Integer stockListId, String symbol);
}
