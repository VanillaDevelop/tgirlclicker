package gg.nya.tgirlclicker.service;

import gg.nya.tgirlclicker.repository.Link;
import gg.nya.tgirlclicker.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LinkService {
    private static final Logger log = LoggerFactory.getLogger(LinkService.class.getName());

    private final int[] minutePenalty = {0, 0, 1, 5, 10, 30, 60, 120, 240, 1440};

    private final LinkRepository linkRepository;

    @Autowired
    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

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

    public Optional<Link> createOrFindLink(String link, boolean alternativeMode, String clientIp, String userAgent) {
        log.info("createLink, link: {}, alternativeMode: {}", link, alternativeMode);
        Optional<Link> existingLink = linkRepository.findLinkByLinkAndAlternativeMode(link, alternativeMode);
        if (existingLink.isPresent()) {
            log.debug("createLink, link already exists: {} (alt: {}) -> {}", link, alternativeMode, existingLink.get().getShorthand());
            return existingLink;
        }

        //Check if the IP can create a new at this time
        int minutePenalty = this.getNextLinkDurationMinutes(clientIp);
        if (minutePenalty > 0) {
            log.warn("createLink, client IP {} is under penalty, cannot create link for {} minutes", clientIp, minutePenalty);
            return Optional.empty();
        }

        String shorthand = generateUniqueShorthand();
        Link newLink = new Link(link, alternativeMode, shorthand, clientIp, userAgent);
        linkRepository.save(newLink);
        log.info("createLink, new link created: {} (alt: {}) -> {}", link, alternativeMode, shorthand);
        return Optional.of(newLink);
    }

    /**
     * Based on the client IP, returns the duration in minutes until the next link can be created.
     * @param clientIp the IP address of the client
     * @return the duration in minutes until the next link can be created. 0 means the client can create a link immediately.
     */
    private int getNextLinkDurationMinutes(String clientIp) {
        log.debug("getNextLinkDurationMinutes, checking next link duration for client IP: {}", clientIp);
        List<Link> createdLinks = linkRepository.findRecentLinksByClientIp(clientIp);
        log.debug("getNextLinkDurationMinutes, found {} recent links created by client IP: {}", createdLinks.size(), clientIp);

        if(createdLinks.isEmpty()) {
            return 0;
        }

        int minutes = minutePenalty[createdLinks.size() - 1];
        int elapsedMinutes = (int) ((System.currentTimeMillis() - createdLinks.getFirst().getCreatedDate().getTime()) / 60000);
        int remainingMinutes = Math.max(0, minutes - elapsedMinutes);

        log.debug("getNextLinkDurationMinutes, calculated remaining penalty: {} (total: {}, elapsed: {})",
                  remainingMinutes, minutes, elapsedMinutes);
        return remainingMinutes;

    }

    public int getTotalClickCount() {
        log.debug("getTotalClickCount, calculating total click count.");
        return linkRepository.sumClickCount();
    }

    private String generateUniqueShorthand() {
        String shorthand;
        int attempts = 0;
        do {
            shorthand = generateRandomShorthand((attempts / 100) + 4);
            attempts++;
        } while (linkRepository.existsByShorthand(shorthand));
        log.debug("generateUniqueShorthand, generated unique shorthand {} after {} attempts", shorthand, attempts);
        return shorthand;
    }

    private String generateRandomShorthand(int length) {
        if (length > 8) {
            throw new IllegalArgumentException("Shorthand length must not exceed 8 characters.");
        }
        return java.util.UUID.randomUUID().toString().substring(0, length);
    }
}
