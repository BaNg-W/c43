package cscc43.Stock;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StockRepo extends CrudRepository<Stock, StockId> {

}
