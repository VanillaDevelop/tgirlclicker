package gg.nya.tgirlclicker.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends CrudRepository<Link, Long> {
    Optional<Link> findLinkByShorthand(String shorthand);

    Optional<Link> findLinkByLinkAndAlternativeMode(String link, boolean alternativeMode);

    boolean existsByShorthand(String shorthand);

    @Query("SELECT SUM(clickCount) FROM Link")
    int sumClickCount();
}
