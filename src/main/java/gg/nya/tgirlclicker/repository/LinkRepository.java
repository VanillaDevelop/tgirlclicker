package gg.nya.tgirlclicker.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinkRepository extends CrudRepository<Link, Long> {
    Optional<Link> findLinkByShorthand(String shorthand);

    Optional<Link> findLinkByLinkAndAlternativeMode(String link, boolean alternativeMode);

    @Query("SELECT l FROM Link l WHERE l.clientIp = ?1 AND l.createdDate >= (CURRENT_DATE - 1 DAY) ORDER BY l.createdDate DESC LIMIT 10")
    List<Link> findRecentLinksByClientIp(String clientIp);

    boolean existsByShorthand(String shorthand);

    @Query("SELECT COALESCE(SUM(clickCount), 0) FROM Link")
    int sumClickCount();
}
