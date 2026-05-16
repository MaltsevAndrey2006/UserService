package andrey.dev.userservice.repository;

import andrey.dev.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {


    @Modifying
    @Query("UPDATE User u SET u.email = :#{#user.email} " +
            ", u.active = :#{#user.active} " +
            ",u.birthDate = :#{#user.birthDate} " +
            ",u.name = :#{#user.name} " +
            ",u.surname = :#{#user.surname} WHERE u.id =:id ")
    int updateUser(@Param("user") User user, @Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE users SET active = true WHERE id = :id", nativeQuery = true)
    int activateUser(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE users SET active = false WHERE id = :id", nativeQuery = true)
    int deactivateUser(@Param("id") Long id);

    Optional<User> findById(Long id);

    User save(User user);
}
