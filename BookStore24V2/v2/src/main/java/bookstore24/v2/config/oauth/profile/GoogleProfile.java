package bookstore24.v2.config.oauth.profile;

import lombok.Data;

@Data
public class GoogleProfile {

    public String id;
    public String email;
    public String verified_email;
    public String name;
    public String given_name;
    public String family_name;
    public String picture;
    private String locale;

}
