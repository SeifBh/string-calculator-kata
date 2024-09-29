package model;

public class ProfileDto {

    private Long id;
    private String name;
    private String channel;
    private String roles;
    private boolean isAdmin;

    public ProfileDto() {}

    public ProfileDto(String channel) {
        this.channel = channel;
    }

    public ProfileDto(String name, String channel, String roles, boolean isAdmin) {
        this.name = name;
        this.channel = channel;
        this.roles = roles;
        this.isAdmin = isAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
