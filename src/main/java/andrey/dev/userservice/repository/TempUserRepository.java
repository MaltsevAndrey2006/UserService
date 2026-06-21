package andrey.dev.userservice.repository;

import andrey.dev.userservice.entity.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TempUserRepository extends JpaRepository<TempUser, Long> {
    void deleteByEmail(String email);

    Optional<TempUser> findByEmail(String email);
}
