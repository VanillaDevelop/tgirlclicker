package gg.nya.tgirlclicker.repository;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "links")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shorthand;

    @Column(nullable = false)
    private boolean alternativeMode = false;
    
    @Column(nullable = false)
    private String link;
    
    @Column(nullable = false)
    private int clickCount = 0;

    @Column(nullable = false)
    private String clientIp;

    @Column(nullable = false)
    private String userAgent;

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
    
    public void incrementClickCount() {
        this.clickCount++;
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

    @Override
    public String toString() {
        return "Link{" +
                "shorthand='" + shorthand + '\'' +
                ", alternativeMode=" + alternativeMode +
                ", link='" + link + '\'' +
                ", clickCount=" + clickCount +
                '}';
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
}
