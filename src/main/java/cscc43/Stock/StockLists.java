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
@Table(name = "stock_lists")
public class StockLists {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer stockListsId;

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "listname")
    private String listname;

    @Column(name = "publicity")
    private String publicity;

    public StockLists(Integer creatorId, String publicity) {
        this.creatorId = creatorId;
        this.publicity = publicity;
    }
}
