package bookmarks.web;

import bookmarks.AccountRepository;
import bookmarks.Bookmark;
import bookmarks.BookmarkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

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

