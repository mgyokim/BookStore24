package bookstore24.v2.auth.oauth.dto.profile;

import lombok.Data;

@Data
public class GoogleProfileDto {

    public String id;
    public String email;
    public String verified_email;
    public String name;
    public String given_name;
    public String family_name;
    public String picture;
    private String locale;

}
