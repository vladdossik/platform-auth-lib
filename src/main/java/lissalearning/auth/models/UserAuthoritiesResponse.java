package lissalearning.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthoritiesResponse {
    private String username;

    private String email;

    private UUID externalId;

    private Set<String> authorities;
}