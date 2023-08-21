# platform-auth-lib

## Описание
Библиотека для авторизации пользователя

## Инструкция для подключения
Добавить в pom.xml проекта следующие строчки. Вместо Tag добавить версию последнего релиза
``` xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
``` xml
<dependency>
    <groupId>com.github.vladdossik</groupId>
    <artifactId>platform-auth-lib</artifactId>
    <version>v.0.0.5</version>
</dependency>
```

и если отстутствует security, то его тоже нужно будет добавить
``` xml
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

В application.yml добавить следующие настройки
``` yml
auth-lib:
  web:
    auth:
      baseUrl: ${AUTH_SERVICE_URL}
      responseTimeout: 1000
      maxBodySizeForLog: 512
```
В docker-compose.yml добавить
``` dockerfile
AUTH_SERVICE_URL: http://auth-service:8081
```
Затем в классе приложения (class SomeSpringApplication) прописать ComponentScan
``` java
@ComponentScan(basePackages = "lissalearning")
public class SomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SomeApplication.class, args);
	}

}
```

Создать UserDetailsServiceImpl и добавить метод loadUserByToken
``` java
@Transactional
public UserDetailsImpl loadUserByToken(HttpServletRequest httpServletRequest) throws ApiClientException {
    return authClient.getUserDetails(httpServletRequest);
}
```
Создать AuthFilter и переопределить метод doFilterInternal
``` java
@Autowired
private UserDetailsServiceImpl userDetailsService;

@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        try {
            UserDetailsImpl userDetails = userDetailsService.loadUserByToken(request);
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ApiClientException ex) {
            logger.error("Cannot set user authentication: {}", ex);
        }
        filterChain.doFilter(request, response);
    }
```
Создать SecurityConfiguration, пробросить фильтр и userDetailsService
``` java
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public AuthFilter authorizationFilter() {
        return new AuthFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);

        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```
Если нужно добавить дополнительные поля у пользователя, то нужно создать класс, который имплементирует интерфейс UserDetails и методом build, который принимает в себя UserDetailsImpl user из либы
``` java
public class UserAuthenticationDetails implements UserDetails {
    private static final long serialVersionUID = 1L;

    private UUID id; // дополнительное поле 

    private String username;

    private Collection<? extends GrantedAuthority> authorities;

    public UserAuthenticationDetails(UUID id, String username,
                                     Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.authorities = authorities;
    }

    public static UserAuthenticationDetails build(UserDetailsImpl user, UUID externalId) {
        return new UserAuthenticationDetails(
            externalId,
            user.getUsername(),
            user.getAuthorities());
    }
    //getters, setters and equals
}
```

Затем в контроллерах добавить PreAuthorize, где нужно по необходимости прикрыть нужными ролями  
Пример из user-service, UserController class  
Данные могут получить админ, модератор или сам пользователь
``` java
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR') or authentication.principal.getId() == #externalId")
@GetMapping("/{externalId}")
@Operation(summary = "Получить пользователя по id")
public UserResponseDto getUserById(@PathVariable UUID externalId) {
    return userService.getUserById(externalId);
}
```
Также не забыть добавить в Swagger возможность прокидывать Authorization header
``` java
@Bean
    public OpenAPI getOpenApi() {
        return new OpenAPI()
            .info(
                new Info()
                    .title("Api")
                    .description(
                        "Description")
            ).addSecurityItem(
                new SecurityRequirement()
                    .addList("Bearer Authentication"))
            .components(
                new Components()
                    .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));

    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
            .bearerFormat("JWT")
            .scheme("bearer");
    }
```