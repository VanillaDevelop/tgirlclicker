package gg.nya.tgirlclicker.service;

import gg.nya.tgirlclicker.repository.Link;
import gg.nya.tgirlclicker.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing links.
 * Handles link creation, retrieval, and click count incrementing.
 */
@Service
public class LinkService {
    private static final Logger log = LoggerFactory.getLogger(LinkService.class.getName());

    // Array defining the penalty in minutes based on the number of recent links created by a client IP.
    private final int[] minutePenalty = {0, 0, 1, 5, 10, 30, 60, 120, 240, 1440};

    private final LinkRepository linkRepository;

    @Autowired
    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    /**
     * Retrieves a link by its shorthand and increments its click count if found.
     * @param shorthand the shorthand of the link to retrieve
     * @return an Optional containing the Link if found, or empty if not found
     */
    @CacheEvict(value = "totalClickCount", allEntries = true)
    public Optional<Link> retrieveAndIncrementClickCount(String shorthand) {
        log.debug("retrieveAndIncrementClickCount, searching for shorthand: {}", shorthand);

        Optional<Link> linkOpt = linkRepository.findLinkByShorthand(shorthand);
        if (linkOpt.isPresent()) {
            Link link = linkOpt.get();
            link.setClickCount(link.getClickCount() + 1);
            linkRepository.save(link);
            log.debug("retrieveAndIncrementClickCount, found link: {}, new click count: {}", link.getShorthand(),
                    link.getClickCount());
        }
        return linkOpt;
    }

    /**
     * Creates a new link or finds an existing one based on the provided link and alternative mode.
     * @param link the link to be created or found
     * @param alternativeMode indicates if the link should be created in alternative mode
     * @param clientIp the IP address of the client creating the link
     * @param userAgent the user agent of the client creating the link
     * @return an Optional containing the created or found Link,
     * or empty if the client can not create a new link at this time
     */
    public Optional<Link> createOrFindLink(String link, boolean alternativeMode, String clientIp, String userAgent) {
        log.debug("createLink, link: {}, alternativeMode: {}, clientIp: {}, userAgent: {}", link, alternativeMode,
                clientIp, userAgent);

        Optional<Link> existingLink = linkRepository.findLinkByLinkAndAlternativeMode(link, alternativeMode);
        if (existingLink.isPresent()) {
            log.debug("createLink, link already exists: {} (alt: {}) -> {}, returning", link, alternativeMode,
                    existingLink.get().getShorthand());
            return existingLink;
        }

        //Check if the IP can create a new at this time
        int minutePenalty = getNextLinkDurationMinutes(clientIp);
        if (minutePenalty > 0) {
            log.warn("createLink, client IP {} is under penalty, cannot create link for {} minutes", clientIp,
                    minutePenalty);
            return Optional.empty();
        }

        String shorthand = generateUniqueShorthand();
        Link newLink = new Link(link, alternativeMode, shorthand, clientIp, userAgent);
        linkRepository.save(newLink);

        log.debug("createLink, new link created: {} (alt: {}) -> {}, returning", link, alternativeMode, shorthand);
        return Optional.of(newLink);
    }

    /**
     * Returns the total number of clicks across all links.
     * @return the total click count
     */
    @Cacheable(value = "totalClickCount", cacheManager = "cacheManager")
    public int getTotalClickCount() {
        log.debug("getTotalClickCount, retrieving total click count from repository");

        int totalClickCount = linkRepository.sumClickCount();
        log.debug("getTotalClickCount, returning total click count: {}", totalClickCount);
        return totalClickCount;
    }

    /**
     * Based on the client IP, returns the duration in minutes until the next link can be created.
     * @param clientIp the IP address of the client
     * @return the duration in minutes until the next link can be created.
     * 0 means the client can create a link immediately.
     */
    private int getNextLinkDurationMinutes(String clientIp) {
        log.debug("getNextLinkDurationMinutes, checking next link duration for client IP: {}", clientIp);

        List<Link> createdLinks = linkRepository.findRecentLinksByClientIp(clientIp);
        log.debug("getNextLinkDurationMinutes, found {} recent links created by client IP: {}",
                createdLinks.size(), clientIp);

        if(createdLinks.isEmpty()) {
            log.debug("getNextLinkDurationMinutes, no recent links found for client IP: {}, returning 0",
                      clientIp);
            return 0;
        }

        int minutes = minutePenalty[createdLinks.size() - 1];
        int elapsedMinutes =
                (int) ((System.currentTimeMillis() - createdLinks.getFirst().getCreatedDate().getTime()) / 60000);
        int remainingMinutes = Math.max(0, minutes - elapsedMinutes);

        log.debug("getNextLinkDurationMinutes, calculated remaining penalty: {} (total: {}, elapsed: {})",
                  remainingMinutes, minutes, elapsedMinutes);
        return remainingMinutes;

    }

    /**
     * Generates a unique shorthand for a new link.
     * @return a unique shorthand string
     */
    private String generateUniqueShorthand() {
        log.debug("generateUniqueShorthand, generating unique shorthand for new link");

        String shorthand;
        int attempts = 0;
        do {
            shorthand = generateRandomShorthand((attempts / 100) + 4);
            attempts++;
        } while (linkRepository.existsByShorthand(shorthand));
        log.debug("generateUniqueShorthand, generated unique shorthand {} after {} attempt(s)", shorthand, attempts);
        return shorthand;
    }

    /**
     * Generates a random shorthand of the specified length.
     * @param length the length of the shorthand to generate, must be between 4 and 8 characters
     * @return a random shorthand string of the specified length
     */
    private String generateRandomShorthand(int length) {
        if (length > 8) {
            throw new IllegalArgumentException("Shorthand length must not exceed 8 characters.");
        }
        return java.util.UUID.randomUUID().toString().substring(0, length);
    }
}
