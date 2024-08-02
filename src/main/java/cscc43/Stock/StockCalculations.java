package cscc43.Stock;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.Date;
import java.util.List;

@Component
public class StockCalculations {

    private final EntityManager entityManager;
    public StockCalculations(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Object[] calculateCoefficientOfVariationAndBeta(String stockSymbol, String marketSymbol, Date startDate, Date endDate) {
        String sql = """
            WITH stock_returns AS (
                SELECT 
                    timestamp,
                    (close - LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp)) / LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp) AS return
                FROM 
                    stocks
                WHERE 
                    symbol = :stockSymbol AND timestamp BETWEEN :startDate AND :endDate
            ), 
            market_returns AS (
                SELECT 
                    timestamp,
                    (close - LAG(close) OVER (ORDER BY timestamp)) / LAG(close) OVER (ORDER BY timestamp) AS return
                FROM 
                    stocks
                WHERE 
                    symbol = :marketSymbol AND timestamp BETWEEN :startDate AND :endDate
            ),
            combined_returns AS (
                SELECT 
                    s.timestamp,
                    s.return AS stock_return,
                    m.return AS market_return
                FROM 
                    stock_returns s
                JOIN 
                    market_returns m ON s.timestamp = m.timestamp
            ),
            mean_returns AS (
                SELECT 
                    AVG(stock_return) AS mean_stock_return,
                    AVG(market_return) AS mean_market_return
                FROM 
                    combined_returns
            ),
            covariance AS (
                SELECT 
                    AVG((stock_return - mean_stock_return) * (market_return - mean_market_return)) AS covar
                FROM 
                    combined_returns, mean_returns
            ),
            variance_market AS (
                SELECT 
                    AVG(POWER(market_return - mean_market_return, 2)) AS var_market
                FROM 
                    combined_returns, mean_returns
            ),
            beta AS (
                SELECT 
                    covar / var_market AS beta
                FROM 
                    covariance, variance_market
            ),
            stddev_stock AS (
                SELECT 
                    STDDEV(stock_return) AS stddev_stock
                FROM 
                    combined_returns
            )
            SELECT 
                (stddev_stock / mean_stock_return) AS coefficient_of_variation,
                beta 
            FROM 
                stddev_stock, beta, mean_returns;
        """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("stockSymbol", stockSymbol);
        query.setParameter("marketSymbol", marketSymbol);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        return (Object[]) query.getSingleResult();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> calculateCorrelationMatrix(Integer portfolioId, Date startDate, Date endDate) {
        String sql = """
            WITH stock_returns AS (
                SELECT 
                    timestamp,
                    symbol,
                    (close - LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp)) / LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp) AS return
                FROM 
                    stocks
                WHERE 
                    symbol IN (SELECT symbol FROM portfolio_stocks WHERE portfolio_id = :portfolioId) AND timestamp BETWEEN :startDate AND :endDate
            ),
            correlation_matrix AS (
                SELECT 
                    s1.symbol AS symbol1, 
                    s2.symbol AS symbol2,
                    CORR(s1.return, s2.return) AS correlation
                FROM 
                    stock_returns s1, 
                    stock_returns s2
                WHERE 
                    s1.timestamp = s2.timestamp
                GROUP BY 
                    s1.symbol, 
                    s2.symbol
            )
            SELECT 
                symbol1, 
                symbol2,
                correlation 
            FROM 
                correlation_matrix;
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("portfolioId", portfolioId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

         return (List<Object[]>) query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> calculateCorrelationMatrixST(Integer stockListId, Date startDate, Date endDate) {
        String sql = """
            WITH stock_returns AS (
                SELECT 
                    timestamp,
                    symbol,
                    (close - LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp)) / LAG(close) OVER (PARTITION BY symbol ORDER BY timestamp) AS return
                FROM 
                    stocks
                WHERE 
                    symbol IN (SELECT symbol FROM stock_list_items WHERE stocklist_id = :stockListId) AND timestamp BETWEEN :startDate AND :endDate
            ),
            correlation_matrix AS (
                SELECT 
                    s1.symbol AS symbol1, 
                    s2.symbol AS symbol2,
                    CORR(s1.return, s2.return) AS correlation
                FROM 
                    stock_returns s1, 
                    stock_returns s2
                WHERE 
                    s1.timestamp = s2.timestamp
                GROUP BY 
                    s1.symbol, 
                    s2.symbol
            )
            SELECT 
                symbol1, 
                symbol2,
                correlation 
            FROM 
                correlation_matrix;
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("stockListId", stockListId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

         return (List<Object[]>) query.getResultList();
    }
}
