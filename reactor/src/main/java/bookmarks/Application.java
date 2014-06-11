package bookmarks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.selector.Selectors;
import reactor.spring.context.config.EnableReactor;


@Configuration
@ComponentScan
@EnableReactor
@EnableAutoConfiguration
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Logger log() {
        return LoggerFactory.getLogger(Application.class);
    }

    @Bean
    Reactor reactor(Environment env) {
        return Reactors.reactor().env(env).dispatcher(Environment.RING_BUFFER).get();
    }

    @Bean
    CommandLineRunner init(Reactor reactor) {
        return a -> {


            reactor.on(Selectors.$("test.topic"), e -> {
                System.out.println(e.toString());
            });


            reactor.notify("test.topic", Event.wrap("Hello World!"));
        };
    }


}
