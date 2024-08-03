package cscc43.portfolio;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface PortfolioStockRepo extends CrudRepository<PortfolioStock, Integer> {

    @Query(value = "SELECT * FROM portfolio_stocks WHERE portfolio_id = ?1", nativeQuery = true)
    List<PortfolioStock> findStocksByPortfolio(Integer portfolioId);

    @Query(value = "SELECT * FROM portfolio_stocks WHERE portfolio_id = ?1 AND symbol = ?2", nativeQuery = true)
    PortfolioStock findByPortfolioIdAndSymbol(Integer portfolioId, String symbol);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM portfolio_stocks WHERE portfolio_id = ?1 AND symbol = ?2", nativeQuery = true)
    void deleteByPortfolioIdAndSymbol(Integer portfolioId, String symbol);
    
}


