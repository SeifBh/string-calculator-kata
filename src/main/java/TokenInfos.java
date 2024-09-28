import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "token", schema = "croupier")
public class TokenInfos {
    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 5000)
    private String token;
    private Date createdDate;
    private Date expirationDate;
    private String profile;
    private String channel;
    private Long ttl;
    private String aud;
    private String iss;
    private String roles;
    private String comment;
    private String stack;
    private String emails;
    private boolean mailSent;

    public TokenInfos() { }

    public TokenInfos(String token, Date createdDate, Date expirationDate, String channel, String roles, NewToken obj) {
        this.token = token;
        this.createdDate = createdDate;
        this.expirationDate = expirationDate;
        this.channel = channel;
        this.roles = roles;
        this.profile = obj.getProfile();
        this.ttl = obj.getTtl();
        this.aud = obj.getToken().getAud();
        this.iss = obj.getToken().getIss();
        this.comment = obj.getToken().getReason();
        this.stack = obj.getStack();
        this.emails = String.join(",", obj.getEmails());
        this.mailSent = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public long getTtl() {
        return ttl;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }

    public boolean isMailSent() {
        return mailSent;
    }

    public void setMailSent(boolean mailSent) {
        this.mailSent = mailSent;
    }
}
