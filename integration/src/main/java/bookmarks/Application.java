package bookmarks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.annotation.*;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.transformer.ObjectToStringTransformer;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

@Configuration
@ComponentScan
@IntegrationComponentScan
@EnableAutoConfiguration
@EnableIntegration
public class Application {

    public static class BookmarkMessage {
        URI uri;
        String description;

        @Override
        public String toString() {
            return "BookmarkMessage{" +
                    "uri=" + uri +
                    ", description='" + description + '\'' +
                    '}';
        }

        public BookmarkMessage(URI uri, String description) {
            this.uri = uri;
            this.description = description;
        }

        public URI getUri() {
            return uri;
        }

        public String getDescription() {
            return description;
        }
    }

    @Bean
    CommandLineRunner init(AccountRepository accountRepository,
                           BookmarkGateway bookmarkGateway) {
        return (evt) -> {

            Arrays.asList("jhoeller", "gprussell",
                    "dsyer", "pwebb", "ogierke", "rwinch", "mfisher", "mpollack").forEach(a -> {
                accountRepository.save(new Account(a, "password"));
            });


            Long id = bookmarkGateway.bookmark("dsyer", new BookmarkMessage( URI.create("http://spring.io"), "The best destination for Spring related content"));
            System.out.println("id for bookmark: " + id);

        };
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    private static final String FILE_REQUESTS_CHANNEL = "fileRequests";
    private static final String REGULAR_REQUESTS_CHANNEL = "requests";

    @Bean(name = FILE_REQUESTS_CHANNEL)
    MessageChannel fileRequests() {
        return new DirectChannel();
    }

    @Bean(name = REGULAR_REQUESTS_CHANNEL)
    MessageChannel requests() {
        return new DirectChannel();
    }

    @MessagingGateway(defaultRequestChannel = REGULAR_REQUESTS_CHANNEL)
    public interface BookmarkGateway {
        Long bookmark(@Header("username") String username, BookmarkMessage bookmark);
    }


    @Bean
    @InboundChannelAdapter(value = FILE_REQUESTS_CHANNEL, poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "1"))
    FileReadingMessageSource fileReadingMessageSource() throws IOException {
        FileReadingMessageSource files = new FileReadingMessageSource();
        files.setAutoCreateDirectory(true);
        files.setDirectory(new File("/Users/jlong/Desktop/in/"));
        files.setScanEachPoll(true);
        return files;
    }


    @Bean
    IntegrationFlow regularBookmarkFlow() {
        return IntegrationFlows.from(this.requests())
                .transform(new ObjectToStringTransformer())
                .transform( msg ->  232)
                .get();
    }

    @Bean
    IntegrationFlow fileBookmarkFlow() {
        return IntegrationFlows.from( FILE_REQUESTS_CHANNEL)
                .transform(new FileToStringTransformer())
                .handle(msg -> System.out.println(msg.toString()))
                .get();
    }


}






