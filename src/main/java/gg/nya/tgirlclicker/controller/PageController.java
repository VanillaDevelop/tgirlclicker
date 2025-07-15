package gg.nya.tgirlclicker.controller;

import gg.nya.tgirlclicker.controller.model.CreateLinkDto;
import gg.nya.tgirlclicker.repository.Link;
import gg.nya.tgirlclicker.service.LinkService;
import gg.nya.tgirlclicker.session.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        if (userSession.isSecretMode()) {
            log.debug("index, user is in secret mode, serving UUID4");
            model.addAttribute("UUID4", userSession.getAuthorizeUUIDs().get(3));
        }
        log.debug("index, main page was served with total click count: {}", totalClickCount);
        return "index";
    }

    @PostMapping("/links")
    public String createLink(@Valid @ModelAttribute CreateLinkDto createLinkDto, BindingResult bindingResult,
                             RedirectAttributes redirectAttributes, HttpServletRequest request) {
        String clientIp = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "Unknown";

        log.info("createLink, request to create link: {} from IP: {} with User-Agent: {}", 
                createLinkDto, clientIp, userAgent);
        if (bindingResult.hasErrors()) {
            log.warn("createLink, validation errors: {}", bindingResult.getAllErrors());
            return "redirect:/";
        }
        boolean alternativeMode = false;
        if (userSession.isSecretMode() && createLinkDto.getAlternativeMode() != null
                && createLinkDto.getAlternativeMode().equals(userSession.getAuthorizeUUIDs().get(3))) {
            alternativeMode = true;
            log.debug("createLink, alternative mode enabled for link creation");
        }
        Optional<Link> createdLink = linkService.createOrFindLink(createLinkDto.getLink(), alternativeMode, clientIp, userAgent);

        if(createdLink.isPresent()) {
            log.info("createLink, link created successfully: {}", createdLink);
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            redirectAttributes.addFlashAttribute("createdLink", baseUrl + "/" + createdLink.get().getShorthand());
        }
        else {
            log.warn("createLink, could not create link, redirecting to index");
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot create another link at this time.");
        }
        return "redirect:/";
    }

    @PostMapping("/auth")
    public String auth(@RequestBody String UUID) {
        log.debug("auth, user authorization request with UUID: {}", UUID);
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

        if(resolvedLink.get().isAlternativeMode())
        {
            log.debug("redirectToLink, alternative mode enabled for link resolution for shorthand: {}", shorthand);
            model.addAttribute("link", resolvedLink.get().getLink());
            return "redirect";
        }
        else {
            log.debug("redirectToLink, redirecting directly for shorthand: {}", shorthand);
            return "redirect:" + resolvedLink.get().getLink();
        }
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("UUID3", userSession.getAuthorizeUUIDs().get(2));
        log.debug("about, about page was served");
        return "about";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
