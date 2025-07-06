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

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession implements Serializable {
    private final Logger log = LoggerFactory.getLogger(UserSession.class);

    private boolean secretMode = false;
    private final List<UUID> authorizeUUIDs = new ArrayList<>();
    private final List<UUID> remainingUUIDs = new ArrayList<>();
    
    public UserSession() {
        log.info("New UserSession initialized, generating authorization UUIDs.");
        authorizeUUIDs.add(UUID.randomUUID());
        authorizeUUIDs.add(UUID.randomUUID());
        authorizeUUIDs.add(UUID.randomUUID());
        authorizeUUIDs.add(UUID.randomUUID());
        remainingUUIDs.addAll(authorizeUUIDs.subList(0, 3));
    }

    public boolean isSecretMode() {
        return secretMode;
    }

    public List<UUID> getAuthorizeUUIDs() {
        return List.copyOf(authorizeUUIDs);
    }

    public void authorize(UUID uuid) {
        log.info("authorize, user authorization request with UUID: {}", uuid);
        boolean success = this.remainingUUIDs.remove(uuid);
        log.debug("authorize, success: {}, remaining UUID count: {}", success, this.remainingUUIDs.size());

        if (this.remainingUUIDs.isEmpty()) {
            this.secretMode = true;
            log.info("authorize, user has authorized all UUIDs, enabling secret mode.");
        }
    }
}