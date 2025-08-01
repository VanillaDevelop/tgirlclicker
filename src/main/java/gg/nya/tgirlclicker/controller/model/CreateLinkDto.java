package gg.nya.tgirlclicker.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Data Transfer Object for creating a link.
 */
public class CreateLinkDto {

    /**
     * The link to which the user should be redirected.
     */
    @NotNull(message = "Link cannot be null")
    @NotBlank(message = "Link cannot be blank")
    @Pattern(regexp = "^https?://.*", message = "Link must be a valid URL starting with http:// or https://")
    private String link;

    /**
     * If not null, this UUID is validated against @{@link gg.nya.tgirlclicker.session.UserSession}
     * to determine whether the link redirect should be delivered in the alternative mode.
     */
    private UUID alternativeMode;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public UUID getAlternativeMode() {
        return alternativeMode;
    }

    public void setAlternativeMode(UUID alternativeMode) {
        this.alternativeMode = alternativeMode;
    }

    @Override
    public String toString() {
        return "CreateLinkDto{" +
                "link='" + link + '\'' +
                ", alternativeMode=" + alternativeMode +
                '}';
    }
}
