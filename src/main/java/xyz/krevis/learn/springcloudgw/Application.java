package xyz.krevis.learn.springcloudgw;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import xyz.krevis.learn.springcloudgw.Application.UriConfiguration;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(UriConfiguration.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {

        String httpUri = uriConfiguration.getHttpbin();

        return builder.routes()
                      .route(predicateSpec -> predicateSpec.path("/get") // Path predicate
                                                           .filters(gatewayFilterSpec -> gatewayFilterSpec.addRequestHeader("Hello", "World"))
                                                           .uri(httpUri))
                      .route(predicateSpec -> predicateSpec.host("*.circuitbreaker.com") // Host predicate
                                                           .filters(gatewayFilterSpec -> gatewayFilterSpec.circuitBreaker(config -> config.setName("mycmd")
                                                                                                                                          .setFallbackUri("forward:/fallback")))
                                                           .uri(httpUri))
                      .build();
    }

    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("fallback");
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                      .route("path_route", r -> r.path("/get")
                                                 .uri("http://httpbin.org"))
                      .route("host_route", r -> r.host("*.myhost.org")
                                                 .uri("http://httpbin.org"))
                      .route("rewrite_route", r -> r.host("*.rewrite.org")
                                                    .filters(f -> f.rewritePath("/foo/(?<segment>.*)", "/${segment}"))
                                                    .uri("http://httpbin.org"))
                      .route("hystrix_route", r -> r.host("*.hystrix.org")
//                                                    .filters(f -> f.hystrix(c -> c.setName("slowcmd")))
                                                    .uri("http://httpbin.org"))
                      .route("hystrix_fallback_route", r -> r.host("*.hystrixfallback.org")
//                                                             .filters(f -> f.hystrix(c -> c.setName("slowcmd").setFallbackUri("forward:/hystrixfallback")))
                                                             .uri("http://httpbin.org"))
                      .route("limit_route", r -> r.host("*.limited.org").and().path("/anything/**")
//                                                  .filters(f -> f.requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))
                                                  .uri("http://httpbin.org"))
                      .build();
    }

    @ConfigurationProperties
    @Getter
    @Setter
    class UriConfiguration {

        private String httpbin = "http://httpbin.org:80";
    }
}
