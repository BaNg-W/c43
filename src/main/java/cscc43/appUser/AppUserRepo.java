package cscc43.appUser;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepo extends CrudRepository<AppUser, Integer> {

    @Query(value = "select * from users where username = ?1", nativeQuery=true)
    AppUser findByUsername(String username);
}
