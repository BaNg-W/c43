package cscc43.Stock;
import cscc43.appUser.AppUserRepo;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor

@Entity
@Table(name = "Stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer stock_id;

    @Column(nullable = false)
    private String timestamp;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private Double low;

    @Column(nullable = false)
    private Double high;

    @Column(nullable = false)
    private Double open;

    @Column(nullable = false)
    private Double close;

    @Column(nullable = false)
    private Long volume;

    public Stock(String timestamp, String symbol, Double low, Double high, Double open, Double close, Long volume) {
        this.timestamp = timestamp;
        this.symbol = symbol;
        this.low = low;
        this.high = high;
        this.open = open;
        this.close = close;
        this.volume = volume;
    }

    @PostConstruct
    public void postConstruct() {
        final StockRepo stockRepo;
        stockRepo.importDataFromCSV();
    }
}
