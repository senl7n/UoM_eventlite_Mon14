package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.config.Elements.CSRF;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;
import static uk.ac.man.cs.eventlite.testutil.FormUtil.getCsrfToken;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;

    @Autowired
    private EventRepository eventRepository;

	private WebTestClient client;
    private final static String SESSION_KEY = "JSESSIONID";
    private final static String LOGIN_PATH = "/sign-in";

	@BeforeEach
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}

	@Test
	public void testGetAllEvents() {
		client.get().uri("/events").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk();
	}

	@Test
	public void getEventNotFound() {
		client.get().uri("/events/99").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound().expectHeader()
				.contentTypeCompatibleWith(MediaType.TEXT_HTML).expectBody(String.class).consumeWith(result -> {
					assertThat(result.getResponseBody(), containsString("99"));
				});
	}

    private String[] login() {
        String[] tokens = new String[2];

        EntityExchangeResult<String> result = client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get()
                .uri("/events").accept(MediaType.TEXT_HTML).exchange().expectBody(String.class).returnResult();
        tokens[0] = getCsrfToken(result.getResponseBody());
        tokens[1] = result.getResponseCookies().getFirst(SESSION_KEY).getValue();

        return tokens;
    }

    private String getCsrfToken(String body) {
        Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
        Matcher matcher = pattern.matcher(body);
        assertTrue(matcher.matches());
        return matcher.group(1);
    }

    @Test
    public void testDeleteEvent() throws Exception {
        String[] tokens = login();

        // Then, attempt to delete an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().delete().uri("/events/1")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isFound().expectBody(String.class);

        // Finally, check that the event has been deleted.
        client.get().uri("/events/1").accept(MediaType.TEXT_HTML).exchange().expectStatus().isNotFound();

    }

}
