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
@Table(name = "portfolio")
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer portfolioId;

    @Column(name = "cash_balance")
    private Double cashBalance;

    @Column(name = "user_id")
    private Integer userId;

    public Portfolio(Double cashBalance, Integer userId) {
        this.cashBalance = cashBalance;
        this.userId = userId;
    }
}
