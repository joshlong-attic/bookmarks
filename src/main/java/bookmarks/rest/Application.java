package bookmarks.rest;

import bookmarks.Account;
import bookmarks.AccountRepository;
import bookmarks.Bookmark;
import bookmarks.BookmarkRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.persistence.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.*;


@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    InitializingBean init(AccountRepository accountRepository,
                          BookmarkRepository bookmarkRepository) {
        return () ->
                Arrays.asList("jhoeller", "dsyer", "pwebb", "jlong").forEach(a -> {
                    Account account = accountRepository.save(new Account(a, "password"));
                    bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/" + a, "A description"));
                });
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequestMapping("/bookmarks")
class BookmarkRestController {

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(Principal principal,
                          @RequestBody Bookmark input) {

        Account account = accountRepository.findByUsername(principal.getName());
        Bookmark result = bookmarkRepository.save(new Bookmark(account, input.uri, input.description));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(result.id)
                .toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Bookmark> read(Principal principal) {
        return bookmarkRepository.findByAccountUsername(principal.getName());
    }

    @Autowired
    BookmarkRepository bookmarkRepository;

    @Autowired
    AccountRepository accountRepository;

}
