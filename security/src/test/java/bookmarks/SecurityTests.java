/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package bookmarks;


import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.test.context.DefaultSecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Rob Winch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@DefaultSecurityTestExecutionListeners
public class SecurityTests {
    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .alwaysDo(print())
                .build();
    }

    @Test
    public void requestAccessToken() throws Exception {

        String clientId = "android-bookmarks",
               clientSecret = "123456",
               username = "rwinch",
               password = "password";

        // get a token
        RequestBuilder tokenRequest =
                get("/oauth/token")
                        .with(httpBasic(clientId,clientSecret))
                        .param("client_id", clientId)
                        .param("password", password)
                        .param("client_secret", clientSecret)
                        .param("username", username)
                        .param("grant_type", "password")
                        .param("scope", "write");

        mockMvc.perform(tokenRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(notNullValue()))
                .andExpect(jsonPath("$.refresh_token").value(notNullValue()))
                .andExpect(jsonPath("$.expires_in").value(notNullValue()))
                .andExpect(jsonPath("$.scope").value("write"))
                .andExpect(jsonPath("$.token_type").value("bearer"));
    }

    @Test
    public void getBookmarksNoUser() throws Exception {
        mockMvc.perform(get("/bookmarks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    @Test
    @WithMockUser("rwinch")
    public void getBookmarksRwinch() throws Exception {
        mockMvc.perform(get("/bookmarks"))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath(ALL_BOOKMARK_URIS_EXPRESSION).value(allElementsEndWith("/rwinch")));
    }

    @Test
    @WithMockUser("jhoeller")
    public void getBookmarksJhoeller() throws Exception {
        mockMvc.perform(get("/bookmarks"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath(ALL_BOOKMARK_URIS_EXPRESSION).value(allElementsEndWith("/jhoeller")));
    }

    @Test
    @WithMockUser("rwinch")
    public void getBookmarks1Rwinch() throws Exception {
        mockMvc.perform(get("/bookmarks/{1}",9))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.bookmark.id").value(9));
    }

    private static org.hamcrest.Matcher<Collection<String>> allElementsEndWith(final String suffix) {
        return new TypeSafeMatcher<Collection<String>>() {
            public boolean matchesSafely(Collection<String> result) {
                for(String r : result) {
                    if(r == null || !r.endsWith(suffix)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("all elements end with "+ suffix);
            }
        };
    }

    private static final String ALL_BOOKMARK_URIS_EXPRESSION = "$_embedded.bookmarkResourceList[*].bookmark.uri";
}
