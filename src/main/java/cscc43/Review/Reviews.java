package cscc43.Review;

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
@Table(name = "reviews")
public class Reviews {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer reviewId;

    @Column(name = "stock_list_id")
    private Integer stockListId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "review_text")
    private String reviewText;

    public Reviews(Integer stockListId, Integer userId, String reviewText) {
        this.stockListId = stockListId;
        this.userId = userId;
        this.reviewText = reviewText;
    }
}
