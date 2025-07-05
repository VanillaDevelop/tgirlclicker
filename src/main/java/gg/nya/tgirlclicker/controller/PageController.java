package gg.nya.tgirlclicker.controller;

import gg.nya.tgirlclicker.repository.Link;
import gg.nya.tgirlclicker.service.LinkService;
import gg.nya.tgirlclicker.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class PageController {
    private static final Logger log = LoggerFactory.getLogger(PageController.class);

    private final LinkService linkService;
    private final UserSession userSession;

    @Autowired
    public PageController(LinkService linkService, UserSession userSession) {
        this.linkService = linkService;
        this.userSession = userSession;
    }

    @GetMapping("/")
    public String index(Model model) {
        int totalClickCount = linkService.getTotalClickCount();
        model.addAttribute("totalClickCount", totalClickCount);
        model.addAttribute("UUID1", userSession.getAuthorizeUUIDs().get(0));
        model.addAttribute("UUID2", userSession.getAuthorizeUUIDs().get(1));
        model.addAttribute("userSecretMode", userSession.isSecretMode());
        log.debug("index, main page was served with total click count: {}", totalClickCount);
        return "index";
    }

    @GetMapping("/auth/{UUID}")
    public String auth(@PathVariable String UUID) {
        log.debug("auth, authorization page was served");
        userSession.authorize(java.util.UUID.fromString(UUID));
        return "redirect:/";
    }

    @GetMapping("/{shorthand}")
    public String redirectToLink(@PathVariable String shorthand, Model model) {
        log.info("redirectToLink, request to resolve shorthand: {}", shorthand);
        Optional<Link> resolvedLink = linkService.retrieveAndIncrementClickCount(shorthand);
        if(resolvedLink.isEmpty()) {
            log.warn("redirectToLink, no link found for shorthand: {}", shorthand);
            return "redirect:/";
        }

        log.info("redirectToLink, resolved link: {} -> {}",
                resolvedLink.get().getShorthand(),
                resolvedLink.get().getLink());

        model.addAttribute("link", resolvedLink.get().getLink());
        return "redirect";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("UUID3", userSession.getAuthorizeUUIDs().get(2));
        log.debug("about, about page was served");
        return "about";
    }
}
