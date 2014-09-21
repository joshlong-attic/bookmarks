# REST with Spring  

REST has quickly become the de-facto standard for building web services on the web. There's a much larger discussion to be had about how REST fits in the world of micro-services, but - for this tutorial - let's just look at building RESTful services.

Why REST? [_REST In Practice_](http://www.amazon.com/gp/product/0596805829?ie=UTF8&tag=martinfowlerc-20&linkCode=as2&camp=1789&creative=9325&creativeASIN=0596805829)  proffers, [to borrow Martin Fowler's phrasing](),  "the notion that the web is an existence proof of a massively scalable distributed system that works really well, and we can take ideas from that to build integrated systems more easily." I think that's a pretty good reason: REST embraces the precepts of the web itself, and embraces its architecture, benefits and all.  

What benefits? Principally all those that come for free with HTTP as a platform itself. Application security (encryption and authentication) are known quantities today for which there are known solutions. Caching is built into the protocol. Service routing, through DNS, is a resilient and well-known system already ubiquitously support. <!-- need to flesh this part out: lots of infrastructure to support HTTP already: routing, caching, security, etc., and all of it can be used here. -->

<!--It's not hard to see why so many people are moving to REST. (remove this?) --> 
REST, however ubiquitous, is not a standard, _per se_, but an approach, a style, a _constraint_ on the HTTP protocol. Its implementation may vary in style, approach. As an API consumer this can be a frustrating experience. The quality of REST services varies wildly.  

Dr. Leonard Richardson put together a maturity model that interprets various levels of compliance with RESTful principles, and grades them.  It describes 4 levels, starting at *level 0* (). Martin Fowler [has a very good write-up on the maturity model](http://martinfowler.com/articles/richardsonMaturityModel.html).  

* **Level 0**: the Swamp of POX -  at this level, we're just using HTTP as a transport. You could call SOAP a *Level 0* technoloogy. It uses HTTP, but as a transport. It's worth mentioning that you could also use SOAP [on top of something like JMS](http://www.w3.org/TR/soapjms/), with no HTTP at all. SOAP, thus, is _not_ RESTful. It's only just HTTP-aware.
* **Level 1**: Resources - at this level, a service might use HTTP URIs to distinguish between nouns, or entities, in the system. For example, you might route requests to `/customers`, `/users`, etc. XML-RPC is an example of a *Level 1* technology: it uses HTTP, and it can use URIs to distinguish endpoints. Ultimately, though, XML-RPC is not RESTful: it's using HTTP as a transport for something else (remote procedure calls).
* **Level 2**: HTTP Verbs - this is the level you want to be at.  If you do *everything* wrong with Spring MVC, you'll probably still end up here. At this level, services take advantage of native HTTP qualities like headers, status codes, distinct URIs, and more. This is where we'll start our journey. 
* **Level 3**: Hypermedia Controls - This final level is where we'll strive to be. Hypermedia, as practiced using the [HATEOAS](http://en.wikipedia.org/wiki/HATEOAS) ("HATEOAS" is a truly welcome acronym for the mouthful, "Hypermedia as the Engine of Application State") design pattern. Hypermedia promtes service longevity by decoupling the consumer of a service from initimate knowledge of that service's surface area and topology. It *describes* REST services. The service can answer questions about what to call, and when. We'll look at this in depth later. <!-- more on this; spell out what it means -->

<IMG src = "http://martinfowler.com/articles/images/richardsonMaturityModel/overview.png" width = "500" />
<!-- http://martinfowler.com/articles/richardsonMaturityModel.html   
  what's the rght way to handle attribution to Mr. Fowler?
-->

## Getting Started
As we work through this tutorial, we'll use [Spring Boot](http://spring.io/projects/spring-boot). Spring Boot removes a lot of the boilerplate typical of application development. You can get started by going to the [Spring Initializr](http://start.spring.io/)  and selecting the checkboxes that correspond to the *type* of workload your application will support. In this case, we're going to build a *web* application. So, select "Web" and then choose  "Generate." A `.zip` will start downloading. Unzip it. In it, you'll find a simple, Maven or Gradle-ready directory structure, complete with a Maven `pom.xml` and a Gradle `build.gradle`. Delete the build artifact you don't want to use. Most people are using Maven these days, so - where appropriate - the examples in this tutorial will be Maven based. However, if you haven't looked at Gradle, do. It's *very* nice. 

Spring Boot can work with any IDE. You can use Eclipse, IntelliJ IDEA, Netbeans, etc. [The Spring Tool Suite](https://spring.io/tools/) is an open-source, Eclipse-based IDE distribution that provides a superset of the Java EE distribution of Eclipse. It includes features that making working with Spring applications even easier. It is, by no means, required. But consider it if you want that extra *oomph* for your keystrokes. Here's a video demonstrating how to get started with STS and Spring Boot. This is a general introduction to familiarize you with the tools. 

<iframe width="560" height="315" src="//www.youtube.com/embed/p8AdyMlpmPk" frameborder="0" allowfullscreen></iframe> 

## The Story so Far...
All of our examples will be based on Spring Boot. We'll reprint the same setup code for each example. Our example models a simple bookmark service, à la Instapaper or other cloud-based bookmarking services. Our bookmark service simply collects a URI, and a description. All bookmarks belong to a user account. This relationship is modeled using JPA and Spring Data JPA repositories in [the `model` module](https://github.com/joshlong/bookmarks/tree/tutorial/model/). 

We won't dive too much into the code. We're using two JPA entities to handle model the records as they'll live in a database. We're using a standard SQL database to store our records so that the domain is as immediately useful to as large an audience as possible. 

The first class models our user account. Aptly, with a JPA entity called `Account`, in `model/src/main/java/bookmarks/Account.java`:

<!-- 
 	I suppose i should go an make those entities use getters/setters. tbd ... 
	:( wish we could use groovy or scala for this. or even lombok. 
-->
```
package bookmarks;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;


@Entity
public class Account {

    @OneToMany(mappedBy = "account")
    public Set<Bookmark> bookmarks = new HashSet<>();

    @Id
    @GeneratedValue
    public Long id;

    @JsonIgnore
    public String password;
    public String username;

    public Account(String name, String password) {
        this.username = name;
        this.password = password;
    }

    Account() { // jpa only
    }
}
```


Each `Account` may have no, one, or many `Bookmark` entities. This is a 1:N relationship. The code for the `Bookmark` entity is shown in `model/src/main/java/bookmarks/Bookmark.java`:

```
package bookmarks;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Bookmark {

    @JsonIgnore
    @ManyToOne
    public Account account;

    @Id
    @GeneratedValue
    public Long id;

    Bookmark() { // jpa only
    }

    public Bookmark(Account account, String uri, String description) {
        this.uri = uri;
        this.description = description;
        this.account = account;
    }

    public String uri;
    public String description;
}
```

We'll use [two Spring Data JPA repositories to handle the tedious database interactions](https://spring.io/guides/gs/accessing-data-jpa/). Spring Data repositories are typically interfaces with methods supporting reading, updating, deleting, and creating records against a backend data store. Some  repositories also typically support data paging, and sorting, where appropriate. Spring Data synthesizes implementations based on conventions found in the naming of the methods in the interface. There are multiple repository implementations besides the JPA ones. You can use Spring Data MongoDB, Spring Data GemFire, Spring Data Cassandra, etc. 

One repository will manage our `Account` entities, called `AccountRepository`, shown in  `model/src/main/java/bookmarks/AccountRepository.java`. One custom finder-method, `findByUsername`, will, *basically*, create a JPA query of the form `select a from Account a where a.username = :username`, run it (passing in the method argument `username` as a named parameter for the query), and return the results for us. Convenient!

```
package bookmarks;

import org.springframework.data.jpa.repository.JpaRepository; 

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
}
```

Here's the repository for working with `Bookmark` entities.

```
package bookmarks;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Collection<Bookmark> findByAccountUsername(String username);
}
```

The `BookmarkRepository` has a similar finder method, but this one dereferences the `username` property on the `Bookmark` entity's `Account` relationship, ultimately requiring a join of some sort. The JPA query it generates is, *roughly*, `SELECT b from Bookmark b WHERE b.account.username = :username`.

Our application will use Spring Boot. A Spring Boot application is, at a minimum, a `public static void main` entry-point and the `@EnableAutoConfiguration` annotation. This tells Spring Boot to help out, wherever possible. Our `Application` class is also a good place to stick of odds and ends, like a callback that will run when the application starts and which gives us a great hook to add dummy-data to the system. Here's what our simplest `Application.java` class will look like:

```
... 

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    CommandLineRunner init(AccountRepository accountRepository, BookmarkRepository bookmarkRepository) {
        return (evt) ->
                Arrays.asList("jhoeller", "dsyer", "pwebb", "ogierke", "rwinch", "mfisher", "mpollack").forEach(a -> {
                    Account account = accountRepository.save(new Account(a, "password"));
                    bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + a, "A description"));
                    bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + a, "A description"));
                });
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

```

Once started, Spring Boot will call all beans of type `CommandLineRunner`, giving them a callback. In this case, `CommandLineRunner` is an interface with one *abstract* method, which means that - in the world of Java 8 - we can substitute its definition with a lambda expression. All the examples in this tutorial will use Java 8. There is no reason, however, that you couldn't use Java 6 or 7, simply substituting the more concise lamba syntax for a slightly more verbose anonymous innner class implementing the interface in question. 


## HTTP is the Platform 

HTTP URIs are a natural way to describe hierarchies, or relationships. For example, we might start our REST API at the account level. All URIs start with an account's username. Thus, for an account named `bob`, we might address that account as `/users/bob` or even just `/bob`. To access the collection of bookmarks for that user, we can *descend* one level down (like a file system) to the `/bob/bookmarks` resource. 

REST does *not* prescribe a representation or encoding. REST, short for *Representational STate Transfer*, defers to HTTP's content-negotiation mechanism to let clients and services agree upon a mutually understood representation of data coming from a service, if possible. There are many ways to handle content negotiation, but in the simplest case, a client sends a request with an `Accept` header that specifies a comma-delimited list of acceptable mime types (for example: `Accept: application/json, application/xml, */*`). If the service can produce any of those mime types, it responds with a representation in the first understood mime type. 

We can use HTTP verbs to manipulate the data represented by those URIs. 

*  the HTTP `GET` verb tells the service to *get*, or retreive, the resource designated by a URI. How it does this is, of course, implementation specific. The backend code might talk to a database, a file system, another webservice, etc. The client doesn't need to be aware of this, though. To the client, all resources are HTTP resources, and in the world of HTTP, there's only one way to ask for data: `GET`. `GET` calls have no body in the request, but typically return a body. The response to an HTTP `GET` request for  `/bob/bookmarks/6` might look like:

 	```
 	{
		id: 6,
		uri: "http://bookmark.com/2/bob",
		description: "A description"
	}
	```
	
*  the HTTP `DELETE` verb tells the service to remove the resource designated by a URI. Again, this is implementation specific.   `DELETE` calls have no body.
*  the HTTP `PUT` verb tells the service to update the resource designated by a URI with the body of the enclosed request. Thus, to update the resource at `/bob/bookmarks`, I might send the same JSON representation returned from the `GET` call, with updated fields. The service will *replace* the value.
* the HTTP `POST` verb tells the service to *do something* with the enclosed body of the request. There's no hard and fast rules here, but typically an HTTP `POST` call to `/bob/bookmarks` will *add*, or *append*, the enclosed body to the collection (database, filesystem, whatever) designated by the `/bob/bookmarks` URI. It can be a little confusing, though. An HTTP `POST` to `/bob/bookmarks/1`, on the other hand, might be treated in the same way as an HTTP `PUT` call; the service could take the enclosed body and use it to *replace* the resource designated by the URI. 

Of course, sometimes things don't go to plan. Perhaps the browser timed out, or the service has timed out, or the service encounters an error. We've all gotten the annoying 404 ("Page not found") error when attempting to visit a page that doesn't exist or couldn't be routed to correctly. That 404 is a *status code*. It conveys information about the state of the operation. There are [*many* status codes](http://en.wikipedia.org/wiki/List_of_HTTP_status_codes) divided along ranges for different purposes. When you make a request to a webpage in the browser, it is an HTTP `GET` call, and - if the page shows up - it will have returned a 200 status code. 200 means `OK`; you may not know it, but it's there. 

* Status codes in the **100x range** (from 100-199) are *informational*, and describe the processing fo the request.  
* Status codes in the **200x range** (from 200-299) indicate  the action requested by the client was received, understood, accepted and processed successfully
* Status codes in the **300x range** (from 300-399) indicate  that the client must take additional action to complete the request, such as following a *redirect*
* Status codes in the **400x range** (from 400-499)   is intended for cases in which the client seems to have errored and must correct the request before continuing. The aforementioned 404 is an example of this.
* Status codes in the **500x range** (from 500-599)  is intended for cases where the server failed to fulfill an apparently valid request.

Spring MVC makes it a breeze to employ all of these constructs in designing your API. 

## Building a REST service 

The first cut of a bookmark REST service should at least support reading from, and adding to, an account's bookmarks, as well as reading individual ones. Below, `BookmarkRestController` demonstrates a good first cut. 

```
package bookmarks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    CommandLineRunner init(AccountRepository accountRepository, BookmarkRepository bookmarkRepository) {
        return (evt) ->
                Arrays.asList("jhoeller", "dsyer", "pwebb", "ogierke", "rwinch", "mfisher", "mpollack").forEach(a -> {
                    Account account = accountRepository.save(new Account(a, "password"));
                    bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + a, "A description"));
                    bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + a, "A description"));
                });
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequestMapping("/{userId}/bookmarks")
class BookmarkRestController {

    private final BookmarkRepository bookmarkRepository;
    private final AccountRepository accountRepository;


    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {
        return accountRepository.findByUsername(userId).map(
                account -> {
                    Bookmark result = bookmarkRepository.save(new Bookmark(account, input.uri, input.description));

                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                            .buildAndExpand(result.id)
                            .toUri());
                    return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
                }
        ).orElseThrow(() -> new RuntimeException("could not find the user '" + userId + "'"));

    }

    @RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
    Bookmark readBookmark(@PathVariable String userId, @PathVariable Long bookmarkId) {
        return this.bookmarkRepository.findOne(bookmarkId);
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Bookmark> readBookmarks(@PathVariable String userId) {
        return bookmarkRepository.findByAccountUsername(userId);
    }

    @Autowired
    BookmarkRestController(BookmarkRepository bookmarkRepository, AccountRepository accountRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.accountRepository = accountRepository;
    }
}
```  

`BookmarkRestController` is a simple Spring MVC `@RestController`-annotated component. `@RestController` exposes the annotated bean's methods as HTTP endpoints using metadata furnished by the `@RequestMapping` annotation on each method. A method will be put into service if an incoming HTTP request matches the qualifications stipulated by the   `@RequestMapping` annotation on the method.  



 `@RestController`, when it sits at the type level, provides defaults for all the methods in the type. Each individual method may override most of the type-level annotation. Some things are *contextual*. For example, the `BookmarkRestController` handles *all* requests that start with a username (like `bob`) followed by `/bookmarks`. Any methods in the type that further qualify the URI, like `readBookmark`, are *added* to the root request mapping. Thus, `readBookmark` is, in effect, mapped to `/{userId}/bookmarks/{bookmarkId}`. Methods that don't specify a path just inherit the path mapped at the type level. The `add` method responds to the URI specified at the type level, but it *only* responds to HTTP requests with the verb 
 
 The `{userId}` and `{bookmarkId}` tokens in the path are *path variables*. They're globs, or wildcards. Spring MVC will extract those portions of the URI, and make them available as arguments of the same name that are  passed to the controller method and annotated with `@PathVariable`. For an HTTP `GET` request to the URI `/bob/bookmarks/4234`, the `@PathVariable String userId` argument will be `"bob"`, and the `@PathVariable Long bookmarkId` will be coerced to a `long` value of `4234`. 
 
These controller methods return simple POJOs - `Collection<Bookmark>`, and `Bookmark`, etc., in all but the `add` case. Spring MVC converts these return values using *content-negotiation*. Spring MVC will inspect the requested, `Accept`able response mime-types, then find a configured `HttpMessageConverter` that claims to be able to convert objects to that mime type, if one is configured. Spring Boot automatically wires up  an `HttpMessageConverter` that can convert generic `Object`s to [JSON](http://www.json.org/), absent any more specific converter.   `HttpMessageConverter`s work in both directions: incoming requests bodies can be converted to Java objects, and Java objects can be converted into HTTP response bodies. 
 
 The `add` method specifies a parameter of type `Bookmark` - a POJO. Spring MVC will convert the incoming HTTP request (containing, perhaps, valid JSON) to a POJO using the appropriate `HttpMessageConverter`. 
 
The `add` method accepts incoming HTTP requests, saves them and then sends back a `ResponseEntity<T>`. `ResponseEntity` is a wrapper for a response *and*, optionally, HTTP headers and a status code. The `add` method sends back a `ResponseEntity` with a status code of 201 (`CREATED`) and a header (`Location`) that the client can consult to learn how the newly created record is referenable. It's a bit like extracting the just generated primary key after saving a record in the database. 

There are paths not taken. By default Spring Boot sets up a pretty generous collection of `HttpMessageConverter` implementations suitable for common use, but it's easy to add support for other, perhaps more compact formats using the usual Spring MVC configuration.  

Spring MVC natively supports file uploads via controller arguments of type `MultipartFile multipartFile`.

Spring MVC makes it easy to write service-oriented code whose shape is untainted by `HttpServlet` APIs.
This code can be easily unit tested, extended through Spring AOP. We'll look at how to unit test these Spring MVC components in the next section.

<!--- todo show some output-->

<!-- 
  TODO a video showing how to put this first example together
 -->
#### Testing a REST Service
<!-- todo general purpose information on testing REST endponts --> 



## Building a HATEOAS REST Service
<!-- 
 
 Some of these sentences are from, and inspired by, the very nice Wikipedia article on REST 
  (http://en.wikipedia.org/wiki/Representational_state_transfer#Uniform_interface)
  
-->
The first cut of the API works very well. If this service were well documented, it would be workable for REST clients in many different languages. It is a clean API, in that it takes advantage of some of the primitives that HTTP provides, in a well-understood way. One measure of an API is by its compliance with  the [uniform interface principle](http://en.wikipedia.org/wiki/Representational_state_transfer#Uniform_interface). HTTP REST APIs like the one we have so far stack up pretty well. Each message includes enough information to describe how to process the message. For example, a client might  decide which parser to invoke based on  the `Content-Type` header in the request message. The state in the system is mapped into uniquely identifying resource URIs. State is addressable. Mutations in state are done through known HTTP verbs (`POST`, `GET`, `DELETE`, `PUT`, etc.). <wikipedia>Thus, when a client holds a representation of a resource, including any metadata attached, it has enough information to modify or delete the resource. </wikipedia>

But, we can do better. The services as they stand are adequate to the task but lack.. *staying power*. <wikipedia> Clients must know the API a priori. Changes in the API break clients and they *break* the documentation about the service.  Hypermedia as the engine of application state (a.k.a. [**HATEOAS**](http://en.wikipedia.org/wiki/HATEOAS)) is one more constraint that addresses and removes this coupling.  Clients make state transitions only through actions that are dynamically identified within hypermedia by the server (e.g., by hyperlinks within hypertext). Except for simple fixed entry points to the application, a client does not assume that any particular action is available for any particular resources beyond those described in representations previously received from the server.  
</wikipedia>

Let's look at a revised cut of this API, this time embracing  HATEOAS with [Spring HATEOAS](http://spring.io/projects/spring-hateoas).  It is a slight simplification to say that Spring HATEOAS makes it easy to provide links - metadata about payloads being returned to the client - but that is how we will approach it. Fundamentally, all we will do is *wrap* our response payloads using Spring HATEOAS' `ResourceSupport` type. `ResourceSupport` accumulates `Link` objects which in turn describe useful, related resources. For example, a resource describing an account in an e-commerce solution could have a link to the resource for that account's orders, a link to that account's current shopping cart, and a link that can be used to retrieve that resources state again. 

These `Link`s, by the way, are of the same sort as the  `<link/>` element so often used in HTML pages to import CSS stylesheets. They have an `href` attribute and a `rel` attribute. The `href` attribute points to where the CSS stylesheet lives, and the `rel` tells the client (the browser) *why* this resource is important (because it's a stylesheet to be used in rendering the page). It's not hard to translate a `link` element into JSON, either.

<!-- we should spend a minute talking about HAL-->


The `BookmarkResource` type *wraps* a `Bookmark` and provides a nice, centralized place to keep link-building logic.  
At a minimum, a resource should provide a link to itself (usually a link whose `rel` value is `self`). We could simply write out `http://127.0.0.1:8080/{userId}/bookmarks/{id}` (substituing the path variables for appropriate values), but this will fail as soon as we move to a different host, port, and context root. We could use the `ServletUriComponentsBuilder` to simplify some of this work. But why should we? After all, Spring MVC *already knows* about this URI. It's *in* the `@RequestMapping` information on every controller method! Spring HATEOAS provides the convenient static `ControllerLinkBuilder.linkTo` and `ControllerLinkBuilder.methodOn` methods to extract the URI from the controller metadata itself - a marked improvement and in keeping with the DRY (do not repeat yourself) principle.  The example shows how to build a `Link` object directly, specifying an arbitrary value for `href` (in this case, the URI for the bookmark itself), how to build a link based on the `@RequestMapping` metadata on a Spring MVC controller, and how to build a link based on the `@RequestMapping` metadata on a specific Spring MVC controller method.

With this in place, the only remaining changes substitute  `Bookmark` types for `BookmarkResource` types. 




``` 
package bookmarks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    CommandLineRunner init(AccountRepository accountRepository, BookmarkRepository bookmarkRepository) {
        return (evt) ->
                Arrays.asList("jhoeller", "dsyer", "pwebb", "ogierke", "rwinch", "mfisher", "mpollack").forEach(a -> {
                    Account account = accountRepository.save(new Account(a, "password"));
                    bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + a, "A description"));
                    bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + a, "A description"));
                });
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

class BookmarkResource extends ResourceSupport {

    private final Bookmark bookmark;

    public BookmarkResource(Bookmark bookmark) {
        String username = bookmark.account.username;
        this.bookmark = bookmark;
        this.add(new Link(bookmark.uri, "bookmark-uri"));
        this.add(linkTo(BookmarkRestController.class, username).withRel("bookmarks"));
        this.add(linkTo(methodOn(BookmarkRestController.class, username).readBookmark(username, bookmark.id)).withSelfRel());
    }

    public Bookmark getBookmark() {
        return bookmark;
    }
}

@RestController
@RequestMapping("/{userId}/bookmarks")
class BookmarkRestController {

    private final BookmarkRepository bookmarkRepository;

    private final AccountRepository accountRepository;

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {
        return accountRepository.findByUsername(userId)
            .map(account -> {
                Bookmark bookmark = bookmarkRepository.save(new Bookmark(account, input.uri, input.description));

                HttpHeaders httpHeaders = new HttpHeaders();

                Link forOneBookmark = new BookmarkResource(bookmark).getLink("self");
                httpHeaders.setLocation(URI.create(forOneBookmark.getHref()));

                return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
            }
        ).orElseThrow(() -> new RuntimeException("could not find the user '" + userId + "'"));

    }

    @RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
    BookmarkResource readBookmark(@PathVariable String userId, @PathVariable Long bookmarkId) {
        return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
    }

    @RequestMapping(method = RequestMethod.GET)
    Resources<BookmarkResource> readBookmarks(@PathVariable String userId) {
        List<BookmarkResource> bookmarkResourceList =
                bookmarkRepository.findByAccountUsername(userId).stream()
                        .map(BookmarkResource::new).collect(Collectors.toList());
        return new Resources<BookmarkResource>(bookmarkResourceList);
    }

    @Autowired
    BookmarkRestController(BookmarkRepository bookmarkRepository,
                           AccountRepository accountRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.accountRepository = accountRepository;
    }
}

```


<!--- todo show some output-->

##   Error Handling
<!-- todo 
 	 talk about vndErrors 
 	 talk about sending about useful response 
 	 talk about status codes through @ControllerAdvice 
-->
REST APIs are exposed through HTTP. HTTP *is* the contract. It follows that error-handling in a REST API follows the norms of HTTP, as well. There are a few layers that you may introduce error handling in Spring MVC. 

#### Returning an appropriate Mime-Type - `VndErrors`

#### Send Usable Error Messages
Remember, error handling is as much about development as production. Human beings - actual people - will rely on the constraints conveyed through helpful error messages to build their clients. Providing useful error messages is a huge way to improve development velocity.

#### Generic Error Handling with `@ControllerAdvice` 



## Securing a REST Service   

Thus far we've proceeded from the assumption that all clients are trustworthy, and that they should have unmitigated access to all the data. This is rarely actually the case. An open REST API is an insecure one. It's not hard to fix that, though. [Spring Security](http://spring.io/projects/spring-security) provides primitives for securing application access. Fundamentally, Spring Security needs to have some idea of your application's users and their privileges. These privileges, or *authorities*, answer the question: what may an application user see,  or do?  

At the heart of Spring Security is the `UserDetailsService` interface, which has *one job*: given a username, produce a `UserDetails` implementation,  `UserDetails` implementations must be able to answer questions about an account's validity, its password, its username, and its authorities (represented by instances of type `org.springframework.security.core.GrantedAuthority`). 


```
package org.springframework.security.core.userdetails;

public interface UserDetailsService {

    org.springframework.security.core.userdetails.UserDetails loadUserByUsername(java.lang.String s) 
        throws org.springframework.security.core.userdetails.UsernameNotFoundException;
        
}
```

Spring Security provides many implementations of this contract that adapt  existing identity providers, like Active Directory, LDAP, `pam`, CAAS, etc. [Spring Social](http://spring.io/projects/spring-social) even provides a nice integration that delegates to    different OAuth-based services like Facebook, Twitter, etc., for authentication.  
 
Our example already has a notion of an `Account`, so we can simply adapt that by providing our own `UserDetailsService` implementation, as shown below in the `WebSecurityConfiguration` `@Configuration`-class . Spring Security will ask this `UserDetailsService`  if it has any questions about an authentication request. 

### Client Authentication and Authorization on the Open Web with Spring Security  OAuth 
We can authenticate client requests in a myriad of ways. Clients could send, for example, an HTTP-basic username  and password on each request. They could transmit an x509 certificate on each request. There are indeed numerous approaches that could be used here, with numerous tradeoffs.  

Our API is meant to be consumed over the open-web. It's meant to be used by all manner of HTML5 and native mobile and desktop clients that we intend to build. We shall use  diverse clients with diverse security capabilities, and any solution we pick should be able to accomodate that. We should also decouple the user's username and password from the application's session. After all, if I reset my Twitter password, I don't want to be forced to re-authenticate every client signed in. On the other hand, if someone *does* hijack one of our clients (perhaps a user has lost a  phone), we don't want the party that stole the device to be able to lock our users out of their accounts. 
 
[OAuth](http://oauth.net/) provides a clean way to handle these concerns.  You've no doubt used OAuth already. One common case is when installing a Facebook plugin or game. Typically the flow looks like this: 

* user finds a game or piece of functionality on the web that requires access to a user's Facebook data in order to function. One common example is "Sign in With Facebook"-style scenarios.
* a user clicks "install," or "add," and is then redirected to a trusted domain (for example: Facebook.com) where the user is prompted to grant certain permissions (like "Post to wall," or "Read Basic information") 
* the user confirms these permissions and is subsequently redirected back to the source application where the source application now has an access token. It will use this access token to make requests  to Facebook on your behalf.  

In this example, any old client can talk to Facebook and the client has, at the end of the process, an access token. This access token is transmitted via all subsequent REST requests, sort of like an HTTP cookie. The username and password need not be retransmitted and the client may cache the access token for a finite or infinite period. Users of the client need not re-authenticate every time they open an application, for example. Even better: access tokens are specific to each client. They may be used to signal that one client needs more permissoins than others.  In the flow above, requests always ended up at Facebook.com where - if the user is not already signed into Facebook, he or she will be pompted to login and then assign permissions to the client. This has the benefit of ensuring that any sensitive information, like a username and password, is never entered in the wild in untrusted applications that might maliciously try to capture that username and password. Our application, will not be available to any old client. We can be sure that any client we deploy is *friendly*, as it will be one of *our* clients. OAuth supports a simpler flow whereby a user authenticates (typically by sending a username and password) from the client and the service returns an OAuth access token directly, sidestepping the need for a redirect to a trusted domain. This is the approach we will take: the reuslt will be that our clients will have an access token that's decoupled from the user's username and password, and the access token can be used to confer different levels of security on different clients.  

Setting up OAuth security for our application is easy.  The `OAuth2Configuration` configuration class describes one client (here, one for a hypothetical Android client) that needs the `ROLE_USER` and the `write` scope.  Spring Security OAuth will read this information from the `AuthenticationManager` that is ultimately configured using our custom `UserDetailsService` implementation. 

OAuth is very flexible. You could, for example, deploy an authorization server that's share by many REST APIs. In this case, our OAuth implementation lives adjacent to our bookmarks REST API. They are one and the same. This is why we've used both `@EnableResourceServer` and `@EnableAuthorizationServer` in the same configuration class. 

### You Said Something about the Open Web? 
We expect that some of the clients to our service will be HTML5-based. They will want to talk to our REST API from different domains, and in most browsers this runs afoul of cross-site-scripting security measures. We can explicitly enable XSS for well-known clients by exposing CORS (cross-origin request scripting) headers. These headers, when present in the service responses, signal to the browser that requests of the origin, shape and configuration described in the headers *are* permitted, even across domains. Our API is decorated with a simple `javax.servlet.Filter` that adds these headers on every request. In the example, we're delegating to a property - `tagit.origin` - if provided or a default of `http://localhost:9000` where we might have, for example, a JavaScript client making requests to the service. We could just as easily read this information from a datastore which we can change without recompiling the code. We've only specified a single origin here, but we could just as easily specified numerous clients.  

### Using HTTPS (SSL/TLS) to prevent Man-in-the-Middle Attacks
Spring Boot provides an embedded web server (Apache Tomcat, by default) that can be configured programmatically to do anything that the standalone Apache Tomcat webserver can do.  To configure HTTPS (SSL/TLS), you need only register a `org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer` bean. Our bean simply wires up the Apache Tomcat connector to support HTTPS (SSL or TLS-encrypted) traffic. HTTPS requires a signed certificate certificate and a certificate password which we provide using property values. Note that, to support ease of configuration, this bean's only available using the Spring profile named `https`: this means that you can run and test the application *without* the profile applied or switch it on just as easily. 

### Putting it All Together 

<!-- show how to gen the certificate, make OAuth requests from CURL, etc. -->
  
```
package bookmarks;

import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
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
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.web.bind.annotation.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class Application {
	
    // CORS
    @Bean
    FilterRegistrationBean corsFilter(@Value("${tagit.origin:http://localhost:9000}") String origin) {
        return new FilterRegistrationBean(new Filter() {
            public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest request = (HttpServletRequest) req;
                HttpServletResponse response = (HttpServletResponse) res;
                String method = request.getMethod();
                // this value could just as easily have come from a database
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE");
                response.setHeader("Access-Control-Max-Age", Long.toString(60 * 60));
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Headers", "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
                if ("OPTIONS".equals(method)) {
                    response.setStatus(HttpStatus.OK.value());
                } else {
                    chain.doFilter(req, res);
                }
            }

            public void init(FilterConfig filterConfig) {
            }

            public void destroy() {
            }
        });
    }

    // expose HTTPS
    @Profile("https")
    @Bean
    EmbeddedServletContainerCustomizer https(@Value("${keystore.file}") Resource keystoreFile,
										      @Value("${keystore.pass}") String keystorePass) throws Exception {

        String absoluteKeystoreFile = keystoreFile.getFile().getAbsolutePath();
        return (ConfigurableEmbeddedServletContainer container) -> {
            if (container instanceof TomcatEmbeddedServletContainerFactory) {
                TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
                tomcat.addConnectorCustomizers(
                        (connector) -> {
                            connector.setPort(8443);
                            connector.setSecure(true);
                            connector.setScheme("https");
                            Http11NioProtocol proto = (Http11NioProtocol) connector.getProtocolHandler();
                            proto.setSSLEnabled(true);
                            proto.setKeystoreFile(absoluteKeystoreFile);
                            proto.setKeystorePass(keystorePass);
                            proto.setKeystoreType("PKCS12");
                            proto.setKeyAlias("tomcat");
                        }
                );
            }
        };
    }

    @Bean
    CommandLineRunner init(AccountRepository accountRepository, BookmarkRepository bookmarkRepository) {
        return (evt) ->
                Arrays.asList("jhoeller", "dsyer", "pwebb", "ogierke", "rwinch", "mfisher", "mpollack").forEach(a -> {
                    Account account = accountRepository.save(new Account(a, "password"));
                    bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + a, "A description"));
                    bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + a, "A description"));
                });
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}


@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        UserDetailsService userDetailsService = (username) ->
                accountRepository.findByUsername(username)
                        .map(a -> new User(a.username, a.password, true, true, true, true, AuthorityUtils.createAuthorityList("USER", "write")))
                        .orElseThrow(() -> new UsernameNotFoundException("could not find the user '" + username + "'"));
        auth.userDetailsService(userDetailsService);
    }
}


@Configuration
@EnableResourceServer
@EnableAuthorizationServer
class OAuth2Configuration extends AuthorizationServerConfigurerAdapter {

    String applicationName = "bookmarks";

    // This is required for password grants, which we specify below as one of the  {@literal authorizedGrantTypes()}.
    @Autowired
    AuthenticationManager authenticationManager;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        clients.inMemory()
                .withClient("android-" + applicationName)
                .authorizedGrantTypes("password", "authorization_code", "refresh_token")
                .authorities("ROLE_USER")
                .scopes("write")
                .resourceIds(applicationName)
                .secret("123456");
    }
}


class BookmarkResource extends ResourceSupport {

    private final Bookmark bookmark;

    public BookmarkResource(Bookmark bookmark) {
        String username = bookmark.account.username;
        this.bookmark = bookmark;
        this.add(new Link(bookmark.uri, "bookmark-uri"));
        this.add(linkTo(BookmarkRestController.class, username).withRel("bookmarks"));
        this.add(linkTo(methodOn(BookmarkRestController.class, username).readBookmark(null, bookmark.id)).withSelfRel());
    }

    public Bookmark getBookmark() {
        return bookmark;
    }
}

@RestController
@RequestMapping("/bookmarks")
class BookmarkRestController {

    private final BookmarkRepository bookmarkRepository;

    private final AccountRepository accountRepository;

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {
        return accountRepository.findByUsername(userId)
                .map(account -> {
                            Bookmark bookmark = bookmarkRepository.save(
                                    new Bookmark(account, input.uri, input.description));
                            HttpHeaders httpHeaders = new HttpHeaders();
                            Link forOneBookmark = new BookmarkResource(bookmark).getLink("self");
                            httpHeaders.setLocation(URI.create(forOneBookmark.getHref()));
                            return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
                        }
                ).orElseThrow(() -> new RuntimeException("could not find the user '" + userId + "'"));
    }

    @RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
    BookmarkResource readBookmark(Principal principal,
                                  @PathVariable Long bookmarkId) {
        return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
    }

    @RequestMapping(method = RequestMethod.GET)
    Resources<BookmarkResource> readBookmarks(Principal principal) {
        List<BookmarkResource> bookmarkResourceList =
                bookmarkRepository.findByAccountUsername(principal.getName()).stream()
                        .map(BookmarkResource::new).collect(Collectors.toList());
        return new Resources<BookmarkResource>(bookmarkResourceList);
    }

    @Autowired
    BookmarkRestController(BookmarkRepository bookmarkRepository, AccountRepository accountRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.accountRepository = accountRepository;
    }
}


```


<!-- TODO: talk about storing Access Tokens in a single store that multiple client nodes use for federated ID in mind. Then show how you can use Spring Boot's autoconfiguration approach to share across an organization's services w/ no configuration apart from `@EnableAutoConfiguration`. Show an example of Spring Security OAuth persiting to Redis or something more horizontally scalable than a SQL DB.    -->


#### Testing a Secure REST Service
<!-- Sometimes Rob Winch has to help put out fires, rescue cats from trees, lead/develop/release one of his 50 or so amazing projects and - ever so infrequently - eat or sleep. Im sure he'd probably help us with this section, too, though. He's busy. But not Rob-busy.  -->

## Consuming an OAuth REST Service 



