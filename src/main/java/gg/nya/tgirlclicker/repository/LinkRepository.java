package gg.nya.tgirlclicker.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for managing Link entities.
 */
@Repository
public interface LinkRepository extends CrudRepository<Link, Long> {
    /**
     * Finds a Link by its shorthand.
     *
     * @param shorthand the shorthand of the link
     * @return an Optional containing the Link if found, or empty if not found
     */
    Optional<Link> findLinkByShorthand(String shorthand);

    /**
     * Finds a Link by its destination and alternative mode.
     * @param link the link destination
     * @param alternativeMode whether the link is in alternative mode
     * @return an Optional containing the Link if found, or empty if not found
     */
    Optional<Link> findLinkByLinkAndAlternativeMode(String link, boolean alternativeMode);

    /**
     * Finds the most recent links created by a specific client IP address within the last 24 hours.
     * @param clientIp the client IP address
     * @return a list of recent Link entities created by the specified client IP
     */
    @Query("SELECT l FROM Link l WHERE l.clientIp = ?1 AND l.createdDate >= (CURRENT_DATE - 1 DAY) ORDER BY l.createdDate DESC LIMIT 10")
    List<Link> findRecentLinksByClientIp(String clientIp);

    /**
     * Checks if a Link exists by its shorthand.
     *
     * @param shorthand the shorthand of the link
     * @return true if a Link with the given shorthand exists, false otherwise
     */
    boolean existsByShorthand(String shorthand);

    /**
     * Calculates the total click count across all Link entities.
     *
     * @return the total click count, or 0 if no links exist
     */
    @Query("SELECT COALESCE(SUM(clickCount), 0) FROM Link")
    int sumClickCount();
}
