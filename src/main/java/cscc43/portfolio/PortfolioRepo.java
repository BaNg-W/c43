package cscc43.portfolio;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioRepo extends CrudRepository<Portfolio, Integer> {

    @Query(value = "SELECT * FROM portfolio WHERE user_id = ?1", nativeQuery = true)
    List<Portfolio> findByUserId(Integer userId);
}