package cscc43.Stock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(StockId.class)
@Table(name = "Stocks")
public class Stock {

    @Id
    @Column(nullable = false)
    private String timestamp;

    @Id
    @Column(nullable = false)
    private String symbol;

    private Double open;

    private Double low;

    private Double high;

    private Double close;

    private Long volume;

    public Stock(String timestamp, Double open, Double low, Double high, Double close, Long volume, String symbol) {
        this.timestamp = timestamp;
        this.symbol = symbol;
        this.low = low;
        this.high = high;
        this.open = open;
        this.close = close;
        this.volume = volume;
    }
}

