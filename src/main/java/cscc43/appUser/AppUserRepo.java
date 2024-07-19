package cscc43.appUser;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface AppUserRepo extends CrudRepository<AppUser, Integer> {

    @Query(value = "select * from users where username = ?1", nativeQuery=true)
    AppUser findByUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "update users set user_status = 1 where username = ?1", nativeQuery=true)
    void setLogin(String username);

    @Modifying
    @Transactional
    @Query(value = "update users set user_status = 0 where username = ?1", nativeQuery=true)
    void setLogout(String username);
}
