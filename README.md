# REST with Spring  

REST has quickly become the de-facto standard for building web services on the web. There's a much larger discussion to be had about how REST fits in the world of micro-services, but - for this tutorial - let's just look at building RESTful services.

Why REST? [_REST In Practice_](http://www.amazon.com/gp/product/0596805829?ie=UTF8&tag=martinfowlerc-20&linkCode=as2&camp=1789&creative=9325&creativeASIN=0596805829)  proffers, [to borrow Martin Fowler's phrasing](),  "the notion that the web is an existence proof of a massively scalable distributed system that works really well, and we can take ideas from that to build integrated systems more easily." I think that's a pretty good reason: REST embraces the precepts of the web itself, and embraces its architecture, benefits and all.  

What benefits? Principally all those that come for free with HTTP as a platform itself. Application security (encryption and authentication) are known quantities today for which there are known solutions. Caching is built into the protocol. Service routing, through DNS, is a resilient and well-known system already ubiquitously support. <!-- need to flesh this part out: lots of infrastructure to support HTTP already: routing, caching, security, etc., and all of it can be used here. -->

It's not hard to see why so many people are moving to it.  

REST, however ubiquitous, is not a standard, _per se_, but an approach, a style, a _constraint_ on the HTTP protocol. Its implementation may vary in style, approach. As an API consumer this can be a frustrating experience. The quality of REST services varies wildly.  

Dr. Leonard Richardson put together a maturity model that interprets various levels of compliance with RESTful principles, and grades them.  It describes 4 levels, starting at *level 0* (). Martin Fowler [has a very good write-up on the maturity model](http://martinfowler.com/articles/richardsonMaturityModel.html).  

* **Level 0**: the Swamp of POX -  at this level, we're just using HTTP as a transport. You could call SOAP a *Level 0* technoloogy. It uses HTTP, but as a transport. It's worth mentioning that you could also use SOAP [on top of something like JMS](http://www.w3.org/TR/soapjms/), with no HTTP at all. SOAP, thus, is _not_ RESTful. It's only just HTTP-aware.
* **Level 1**: Resources - at this level, a service might use HTTP URIs to distinguish between nouns, or entities, in the system. For example, you might route requests to `/customers`, `/users`, etc. XML-RPC is an example of a *Level 1* technology: it uses HTTP, and it can use URIs to distinguish endpoints. Ultimately, though, XML-RPC is not RESTful: it's using HTTP as a transport for something else (remote procedure calls).
* **Level 2**: HTTP Verbs - this is the level you want to be at.  If you do *everything* wrong with Spring MVC, you'll probably still end up here. At this level, services take advantage of native HTTP qualities like headers, status codes, distinct URIs, and more. This is where we'll start our journey.
* **Level 3**: Hypermedia Controls - This final level is where we'll strive to be. Hypermedia, as practiced using the [HATEOAS](http://en.wikipedia.org/wiki/HATEOAS) design pattern, 

<IMG src = "http://martinfowler.com/articles/images/richardsonMaturityModel/overview.png" 
 width = "500" />
<!-- http://martinfowler.com/articles/richardsonMaturityModel.html   
  what's the rght way to handle attribution to Mr. Fowler?
-->

## Getting Started
As we work through this tutorial, we'll use [Spring Boot](http://spring.io/projects/spring-boot). Spring Boot removes a lot of the boilerplate typical of application development. You can get started by going to the [Spring Initializr](http://start.spring.io/)  and selecting the checkboxes that correspond to the *type* of workload your application will support. In this case, we're going to build a *web* application. So, select "Web" and then choose  "Generate." A `.zip` will start downloading. Unzip it. In it, you'll find a simple, Maven or Gradle-ready directory structure, complete with a Maven `pom.xml` and a Gradle `build.gradle`. Delete the build artifact you don't want to use. Most people are using Maven these days, so - where appropriate - the examples in this tutorial will be Maven based. However, if you haven't looked at Gradle, do. It's *very* nice. 

Spring Boot can work with any IDE. You can use Eclipse, IntelliJ IDEA, Netbeans, etc. The Spring Tool Suite is an open-source, Eclipse-based IDE distribution that provides a superset of the Java EE distribution of Eclipse. You can use it 

## Building a REST service 

## Building a HATEOAS REST Service


## Securing a REST Service with OAuth 
TODO: talk about storing Access Tokens in a single store that multiple client nodes use for federated ID in mind. Then show how you can use Spring Boot's autoconfiguration approach to share across an organization's services w/ no configuration apart from `@EnableAutoConfiguration`. Show an example of Spring Security OAuth persiting to Redis or something more horizontally scalable than a SQL DB.  

## Consuming an OAuth REST Service 

