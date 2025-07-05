package gg.nya.tgirlclicker.session;

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
    private boolean secretMode = false;
    private final List<UUID> authorizeUUIDs = new ArrayList<>();
    private final List<UUID> remainingUUIDs = new ArrayList<>();
    
    public UserSession() {
        authorizeUUIDs.add(UUID.randomUUID());
        authorizeUUIDs.add(UUID.randomUUID());
        authorizeUUIDs.add(UUID.randomUUID());
        remainingUUIDs.addAll(authorizeUUIDs);
    }

    public boolean isSecretMode() {
        return secretMode;
    }

    public List<UUID> getAuthorizeUUIDs() {
        return List.copyOf(authorizeUUIDs);
    }

    public void authorize(UUID uuid) {
        this.remainingUUIDs.remove(uuid);
        if (this.remainingUUIDs.isEmpty()) {
            this.secretMode = true;
        }
    }
}