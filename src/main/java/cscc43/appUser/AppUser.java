package cscc43.appUser;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor

@Entity
@Table(name = "Users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer user_id;
    private String username;
    private String password_hash;
    private String email;

}