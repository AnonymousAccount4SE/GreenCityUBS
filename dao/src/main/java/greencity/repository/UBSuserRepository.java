package greencity.repository;

import greencity.entity.user.User;
import greencity.entity.user.ubs.UBSuser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UBSuserRepository extends CrudRepository<UBSuser, Long> {
    /**
     * Finds list of saved user data by the id of user.
     *
     * @param userId the id of current user.
     * @return a list of {@link UBSuser} assigned to
     *         {@link greencity.entity.user.User}.
     */
    @Query("SELECT u FROM UBSuser u JOIN FETCH u.address address WHERE u.user.id = :userId")
    List<UBSuser> getAllByUserId(Long userId);

    /**
     * Finds list of UBSuser who have not paid of the order within three days.
     *
     * @param localDate - date when the user made an order.
     * @return a {@link List} of {@link UBSuser} - which need to send a message.
     */
    @Query(nativeQuery = true,
        value = "SELECT * FROM ubs_user u INNER JOIN orders o ON u.id = o.ubs_user_id "
            + "WHERE CAST(o.order_date AS DATE) = :localDate AND o.order_status LIKE 'FORMED'")
    List<UBSuser> getAllUBSusersWhoHaveNotPaid(LocalDate localDate);

    /**
     * Finds a UBSuser by email.
     *
     * @param email - UBSuser's email.
     * @return a {@link Optional} of {@link UBSuser}.
     */
    Optional<UBSuser> findByEmail(String email);

    /**
     * Find UbsUser by email and userId fk key.
     * 
     * @param email  {@link String}.
     * @param userId {@link Long}.
     * @return {@link UBSuser}.
     *
     * @author Yuriy Bahlay.
     */
    UBSuser findUBSuserByEmailAndUserId(String email, Long userId);

    /**
     * Find UbsUser by current User.
     *
     * @param user {@link User}
     * @return {@link UBSuser}
     */
    List<UBSuser> findUBSuserByUser(User user);

    /**
     * Find UbsUser by userId.
     *
     * @param userId {@link Long}.
     * @return {@link UBSuser}.
     *
     * @author Roman Sulymka.
     */
    UBSuser findUBSuserByUserId(Long userId);
}
