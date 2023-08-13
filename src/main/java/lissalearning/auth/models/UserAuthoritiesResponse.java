package lissalearning.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthoritiesResponse {
    private String username;

    private String email;

    private Set<String> authorities;
}