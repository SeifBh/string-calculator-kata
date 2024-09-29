package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class KeycloakJwtBody {

    @JsonProperty("resource_access")
    private JsonNode resourceAccess;

    @JsonProperty("preferred_username")
    private String preferredUsername;

    private List<String> groups;

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public JsonNode getResourceAccess() {
        return resourceAccess;
    }

    public void setResourceAccess(JsonNode resourceAccess) {
        this.resourceAccess = resourceAccess;
    }

    public String getPreferredUsername() {
        return preferredUsername;
    }

    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }
}
