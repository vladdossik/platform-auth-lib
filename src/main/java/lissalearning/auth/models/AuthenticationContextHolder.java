package lissalearning.auth.models;

import lombok.Data;

import java.util.Optional;

@Data
public class AuthenticationContextHolder {
    private static final ThreadLocal<UserInfo> USER_INFO = new ThreadLocal();
    private static final ThreadLocal<String> AUTHORIZATION_TOKEN_TYPE = new ThreadLocal();
    private static final ThreadLocal<String> AUTHORIZATION_TOKEN = new ThreadLocal();

    public AuthenticationContextHolder() {

    }

    public static void setUserInfo(UserInfo userInfo) {
        USER_INFO.set(userInfo);
    }

    public static UserInfo getUserInfo() {
        return (UserInfo) Optional.ofNullable((UserInfo) USER_INFO.get()).orElseGet(() -> {
            UserInfo userInfo = new UserInfo();
            setUserInfo(userInfo);
            return userInfo;
        });
    }

    public static void setTokenType(String tokenType) {
        AUTHORIZATION_TOKEN_TYPE.set(tokenType);
    }

    public static void setToken(String token) {
        AUTHORIZATION_TOKEN.set(token);
    }


    public static String getTokenType() {
        return (String) AUTHORIZATION_TOKEN_TYPE.get();
    }

    public static String getToken() {
        return (String) AUTHORIZATION_TOKEN.get();
    }

    public static void cleanup() {
        USER_INFO.remove();
        AUTHORIZATION_TOKEN_TYPE.remove();
        AUTHORIZATION_TOKEN.remove();
    }

}
