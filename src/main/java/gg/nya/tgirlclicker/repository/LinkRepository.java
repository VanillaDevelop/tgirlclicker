package gg.nya.tgirlclicker.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends CrudRepository<Link, Long> {
    Optional<Link> findLinkByShorthand(String shorthand);
}
