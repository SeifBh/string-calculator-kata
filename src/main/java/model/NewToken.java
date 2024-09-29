package model;

import java.util.List;

public class NewToken {

    private String stack;
    private String profile;
    private Long ttl;
    private List<String> emails;
    private TokenDto token;

    public NewToken() { }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public TokenDto getToken() {
        return token;
    }

    public void setToken(TokenDto token) {
        this.token = token;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
}
