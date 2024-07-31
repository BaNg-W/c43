package cscc43.Stock;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StockRepo extends CrudRepository<Stock, StockId> {

    @Query(value = "SELECT * FROM stocks WHERE symbol = ?1 AND timestamp >= ?2", nativeQuery = true)
    List<Stock> findStockAfterDate(String symbol, Date timestamp);

    @Query(value = "SELECT * FROM stocks WHERE symbol = ?1", nativeQuery = true)
    List<Stock> findStock(String symbol);

    @Query(value = "SELECT * FROM stocks WHERE symbol = ?1 ORDER BY timestamp DESC LIMIT 1", nativeQuery = true)
    Stock findLastestStock(String symbol);

}
