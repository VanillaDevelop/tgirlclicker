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
        if(model.getAttribute("createdLink")  != null) {
            log.debug("index, createdLink {} attribute found in model, setting response active", model.getAttribute("createdLink"));
            model.addAttribute("responseActive", true);
        }
        log.debug("index, main page was served with total click count: {}", totalClickCount);
        return "index";
    }

    @PostMapping("/links")
    public String createLink(@Valid @ModelAttribute CreateLinkDto createLinkDto, BindingResult bindingResult,
                             RedirectAttributes redirectAttributes, HttpServletRequest request) {
        log.info("createLink, request to create link: {}", createLinkDto);
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
        Link createdLink = linkService.createOrFindLink(createLinkDto.getLink(), alternativeMode);
        log.info("createLink, link created successfully: {}", createdLink);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        redirectAttributes.addFlashAttribute("createdLink", baseUrl + "/" + createdLink.getShorthand());
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
