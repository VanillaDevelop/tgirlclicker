package gg.nya.tgirlclicker.service;

import gg.nya.tgirlclicker.repository.Link;
import gg.nya.tgirlclicker.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LinkService {
    private static final Logger log = LoggerFactory.getLogger(LinkService.class.getName());

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
}
