package gg.nya.tgirlclicker.controller;

import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    Logger log = org.slf4j.LoggerFactory.getLogger(PageController.class);

    @GetMapping("/")
    public String index() {
        log.info("Test Log");
        return "index"; // This will resolve to src/main/resources/templates/index.html
    }
}
