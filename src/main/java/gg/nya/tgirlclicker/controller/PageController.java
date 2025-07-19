package gg.nya.tgirlclicker.controller;

import gg.nya.tgirlclicker.controller.model.CreateLinkDto;
import gg.nya.tgirlclicker.repository.Link;
import gg.nya.tgirlclicker.service.LinkService;
import gg.nya.tgirlclicker.session.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

/**
 * PageController handles the main web pages and link creation logic.
 * It serves the index page, processes link creation, and redirects to links.
 */
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

    /**
     * Serves the main index page with total click count and user session information.
     *
     * @param model Attribute model for the view.
     * @return Returns the index view with model attributes.
     */
    @GetMapping("/")
    public String index(Model model) {
        log.info("index, request to serve main page");

        int totalClickCount = linkService.getTotalClickCount();
        model.addAttribute("totalClickCount", totalClickCount);
        model.addAttribute("UUID1", userSession.getAuthorizeUUIDs().get(0));
        model.addAttribute("UUID2", userSession.getAuthorizeUUIDs().get(1));
        model.addAttribute("userSecretMode", userSession.isSecretMode());
        if (userSession.isSecretMode()) {
            // If the user is in secret mode, show the UUID that needs to be POSTed to authorize the alternative mode
            model.addAttribute("UUID4", userSession.getAuthorizeUUIDs().get(3));
        }

        log.debug("index, main page was served with total click count: {}, secret mode: {}",
                totalClickCount, userSession.isSecretMode());

        return "index";
    }

    /**
     * POST request that handles the creation of a new link.
     * @param createLinkDto The DTO containing the link information to be created.
     * @param bindingResult BindingResult to capture validation errors.
     * @param redirectAttributes RedirectAttributes to pass attributes to the redirect.
     * @param request The HTTP request to extract client information.
     * @return Redirects to the index page with a flash attribute indicating the created link or an error message.
     */
    @PostMapping("/links")
    public String createLink(@Valid @ModelAttribute CreateLinkDto createLinkDto, BindingResult bindingResult,
                             RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.info("createLink, request to create link: {}", createLinkDto);

        if (bindingResult.hasErrors()) {
            log.warn("createLink, validation errors: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid link format. Please try again.");

            return "redirect:/";
        }

        boolean alternativeMode = false;
        if (createLinkDto.getAlternativeMode() != null) {
            if (!userSession.isSecretMode()) {
                log.warn("createLink, alternative mode requested but user is not in secret mode, so request is ignored.");
            }
            else if (!userSession.getAuthorizeUUIDs().get(3).equals(createLinkDto.getAlternativeMode())) {
                log.warn("createLink, alternative mode requested but UUID does not match expected, so request is ignored.");
            }
            else {
                log.debug("createLink, alternative mode requested and UUID matches, enabling alternative mode for link creation");
                alternativeMode = true;
            }
        }

        log.debug("createLink, creating {}, alternative mode {}, IP {}, User-Agent: {}",
                createLinkDto.getLink(), alternativeMode, MDC.get("clientIp"), MDC.get("userAgent"));
        Optional<Link> createdLink = linkService.createOrFindLink(createLinkDto.getLink(), alternativeMode,
                MDC.get("clientIp"), MDC.get("userAgent"));

        if(createdLink.isPresent()) {
            String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + createdLink.get().getShorthand();
            redirectAttributes.addFlashAttribute("createdLink", baseUrl);
            log.debug("createLink, link object created successfully: {}; Returning {}", createdLink, baseUrl);
        }
        else {
            log.warn("createLink, could not create link, redirecting to index");
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot create another link at this time.");
        }

        return "redirect:/";
    }

    /**
     * Handles a user's request to unlock the secret mode.
     * @param UUID The UUID to authorize the user session.
     * @return Redirects to the index page after authorization, whether successful or not.
     */
    @PostMapping("/auth")
    public String auth(@RequestBody String UUID) {
        log.info("auth, user authorization request with UUID: {}", UUID);
        userSession.authorize(java.util.UUID.fromString(UUID));

        return "redirect:/";
    }

    /**
     * Handles the redirection to a link based on its shorthand.
     * @param shorthand The shorthand identifier for the link.
     * @param model Model to add attributes for the view.
     * @return Directly redirects to the link in normal mode. Opens the redirect view in alternative mode. Redirects
     * to the index page if the link is not found.
     */
    @GetMapping("/{shorthand}")
    public String redirectToLink(@PathVariable String shorthand, Model model) {
        log.info("redirectToLink, request to resolve shorthand: {}", shorthand);

        Optional<Link> resolvedLink = linkService.retrieveAndIncrementClickCount(shorthand);
        if(resolvedLink.isEmpty()) {
            log.warn("redirectToLink, no link found for shorthand: {}", shorthand);
            return "redirect:/";
        }

        log.debug("redirectToLink, resolved link: {} -> {}",
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

    /**
     * Serves the about page with information about the application.
     * @param model Attribute model for the view.
     * @return Returns the about view with model attributes.
     */
    @GetMapping("/about")
    public String about(Model model) {
        log.info("about, request to serve about page");

        model.addAttribute("UUID3", userSession.getAuthorizeUUIDs().get(2));
        log.debug("about, about page was served");
        return "about";
    }
}
