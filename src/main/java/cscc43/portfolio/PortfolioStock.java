package cscc43.portfolio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "portfolio_stocks")
public class PortfolioStock {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "portfolio_id")
    private Integer portfolioId;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "shares")
    private Integer shares;

    public PortfolioStock(Integer portfolioId, String symbol, Integer shares) {
        this.portfolioId = portfolioId;
        this.symbol = symbol;
        this.shares = shares;
    }
}
