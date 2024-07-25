package cscc43.Stock;

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
@Table(name = "stock_list_items")
public class StockListItems {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer itemId;

    @Column(name = "stocklist_id")
    private Integer stocklistId;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "shares")
    private Integer shares;

    public StockListItems(Integer stocklistId, String symbol, Integer shares) {
        this.stocklistId = stocklistId;
        this.symbol = symbol;
        this.shares = shares;
    }
}
