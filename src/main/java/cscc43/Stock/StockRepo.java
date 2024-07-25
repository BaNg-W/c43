package cscc43.Stock;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface StockRepo extends CrudRepository<Stock, Integer> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO Stocks(timestamp, open, high, low, close, volume, symbol) SELECT timestamp, open, high, low, close, volume, symbol FROM CSVREAD('/data/SP500History.csv')", nativeQuery=true)
    void importDataFromCSV();
}
