package vn.system.app.modules.facebook.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fb_uid")
    private String fbUid;

    @Column(columnDefinition = "TEXT")
    private String cookieJson;

    @Column(name = "profile_path")
    private String profilePath;

    // ----- GETTERS & SETTERS -----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFbUid() {
        return fbUid;
    }

    public void setFbUid(String fbUid) {
        this.fbUid = fbUid;
    }

    public String getCookieJson() {
        return cookieJson;
    }

    public void setCookieJson(String cookieJson) {
        this.cookieJson = cookieJson;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
}
