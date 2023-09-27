package lissalearning.auth.models;

import lombok.Data;

import java.util.UUID;

@Data
public class UserInfo {
    private UUID userId;
    private String username;
    private String email;
}
