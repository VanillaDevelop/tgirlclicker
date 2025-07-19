package gg.nya.tgirlclicker.repository;

import jakarta.persistence.*;

import java.util.Date;

/**
 * Represents a link in the database.
 * Each link has a shorthand, an alternative mode flag, the actual link,
 * a click count, client IP, user agent, and a creation date.
 */
@Entity
@Table(name = "links")
public class Link {
    /**
     * Unique identifier for the link.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The shorthand via which the link can be accessed.
     */
    @Column(nullable = false)
    private String shorthand;

    /**
     * Flag indicating if the link is in alternative mode.
     * Alternative mode will display the redirect page before redirecting to the link.
     */
    @Column(nullable = false)
    private boolean alternativeMode = false;

    /**
     * The actual link that the shorthand points to.
     */
    @Column(nullable = false)
    private String link;

    /**
     * The number of times the link has been requested.
     */
    @Column(nullable = false)
    private int clickCount = 0;

    /**
     * The IP address of the client that created the link.
     */
    @Column(nullable = false)
    private String clientIp;

    /**
     * The user agent of the client that created the link.
     */
    @Column(nullable = false)
    private String userAgent;

    /**
     * The date when the link was created.
     */
    @Column(nullable = false)
    private Date createdDate;

    public Link() {}
    
    public Link(String link, boolean alternativeMode, String shorthand, String clientIp, String userAgent) {
        this.link = link;
        this.alternativeMode = alternativeMode;
        this.shorthand = shorthand;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.createdDate = new Date();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public int getClickCount() {
        return clickCount;
    }
    
    public void setClickCount(int clickCount) {
        this.clickCount = clickCount;
    }

    public String getShorthand() {
        return shorthand;
    }

    public void setShorthand(String shorthand) {
        this.shorthand = shorthand;
    }

    public boolean isAlternativeMode() {
        return alternativeMode;
    }

    public void setAlternativeMode(boolean alternativeMode) {
        this.alternativeMode = alternativeMode;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "Link{" +
                "shorthand='" + shorthand + '\'' +
                ", alternativeMode=" + alternativeMode +
                ", link='" + link + '\'' +
                ", clickCount=" + clickCount +
                '}';
    }
}
