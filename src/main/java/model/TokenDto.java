package model;

import java.util.Map;

public class TokenDto {

    private String sub;
    private String iss;
    private String aud;
    private String reason;
    private String secret;
    private Map<String, String> claims;

    public TokenDto() { }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Map<String, String> getClaims() {
        return claims;
    }

    public void setClaims(Map<String, String> claims) {
        this.claims = claims;
    }
}
