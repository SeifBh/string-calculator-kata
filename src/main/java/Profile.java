import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "profile", schema = "croupier")
public class Profile {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String channel;
    private String roles;
    private boolean isAdmin;

    public Profile() {}


    public Profile(String channel) {
        this.channel = channel;
    }

    public Profile(String name, String channel, String roles, boolean isAdmin) {
        this.name = name;
        this.channel = channel;
        this.roles = roles;
        this.isAdmin = isAdmin;
    }

    public Profile(ProfileDto profileDto) {
        this.id = profileDto.getId();
        this.name = profileDto.getName();
        this.channel = profileDto.getChannel();
        this.roles = profileDto.getRoles();
        this.isAdmin = profileDto.isAdmin();
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
