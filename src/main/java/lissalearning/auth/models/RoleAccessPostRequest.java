package lissalearning.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoleAccessPostRequest {
    private String role;
}
