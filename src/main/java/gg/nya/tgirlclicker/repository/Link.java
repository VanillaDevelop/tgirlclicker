package gg.nya.tgirlclicker.repository;

import jakarta.persistence.*;

@Entity
@Table(name = "links")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shorthand;
    
    @Column(nullable = false)
    private String link;
    
    @Column(nullable = false)
    private int clickCount = 0;
    
    public Link() {}
    
    public Link(String link) {
        this.link = link;
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
}
