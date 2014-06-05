package bookmarks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;



@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    ApplicationListener<ContextRefreshedEvent> init(AccountRepository accountRepository,
                                                    BookmarkRepository bookmarkRepository) {
        return (evt) ->
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
