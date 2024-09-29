package model;

public class KeycloakDto {
    
    private String url;
    private String clientId;
    private String realm;

    public KeycloakDto(String url, String clientId, String realm) {
        this.url = String.format("%s/auth", url);
        this.clientId = clientId;
        this.realm = realm;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
