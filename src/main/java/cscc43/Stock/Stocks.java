package cscc43.Stock;

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
public class Stocks {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer stock_id;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private Double low_price;

    @Column(nullable = false)
    private Double high_price;

    @Column(nullable = false)
    private Double open_price;

    @Column(nullable = false)
    private Double close_price;

    @Column(nullable = false)
    private Long volume;

    public Stocks(String date, String symbol, Double low_price, Double high_price, Double open_price, Double close_price, Long volume) {
        this.date = date;
        this.symbol = symbol;
        this.low_price = low_price;
        this.high_price = high_price;
        this.open_price = open_price;
        this.close_price = close_price;
        this.volume = volume;
    }
}
