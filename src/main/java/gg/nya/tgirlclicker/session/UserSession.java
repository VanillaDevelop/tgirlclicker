package gg.nya.tgirlclicker.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * UserSession class to manage user-specific session data.
 * This class is scoped to the session and uses a proxy for lazy loading.
 * It handles secret mode activation and UUID authorization.
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession implements Serializable {
    private final Logger log = LoggerFactory.getLogger(UserSession.class);

    //Whether the user can create alternative links
    private boolean secretMode = false;
    //List of UUIDs the user must POST to enable secret mode
    private final List<UUID> authorizeUUIDs = new ArrayList<>();
    //List of remaining UUIDs that the user has not yet authorized
    private final List<UUID> remainingUUIDs = new ArrayList<>();

    public UserSession() {
        log.info("New UserSession initialized, generating authorization UUIDs.");
        // Generate 4 random UUIDs, of which 3 need to be POSTed and the 4th needs to be sent on link generation
        authorizeUUIDs.add(UUID.randomUUID());
        authorizeUUIDs.add(UUID.randomUUID());
        authorizeUUIDs.add(UUID.randomUUID());
        authorizeUUIDs.add(UUID.randomUUID());
        // Initialize remainingUUIDs with the first 3 UUIDs
        remainingUUIDs.addAll(authorizeUUIDs.subList(0, 3));
    }

    /**
     * Authorizes a user by removing the provided UUID from the remaining UUIDs.
     * If all UUIDs are authorized, secret mode is enabled.
     *
     * @param uuid the UUID to authorize
     */
    public void authorize(UUID uuid) {
        log.debug("authorize, user authorization request with UUID: {}", uuid);
        boolean success = this.remainingUUIDs.remove(uuid);
        log.debug("authorize, success: {}, remaining UUID count: {}", success, this.remainingUUIDs.size());

        if (this.remainingUUIDs.isEmpty()) {
            this.secretMode = true;
            log.info("authorize, user has authorized all UUIDs, enabling secret mode.");
        }
    }

    public boolean isSecretMode() {
        return secretMode;
    }

    public List<UUID> getAuthorizeUUIDs() {
        return List.copyOf(authorizeUUIDs);
    }
}