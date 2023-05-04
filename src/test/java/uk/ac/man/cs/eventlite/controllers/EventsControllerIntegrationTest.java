package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.api.Pageable;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;
import com.sys1yagi.mastodon4j.api.method.Timelines;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

        // Execute the GET request for a non-existing event
        long nonExistingEventId = 99;
        client.get().uri("/events/description?id=" + nonExistingEventId)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
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

        client.method(HttpMethod.DELETE).uri(uriBuilder -> uriBuilder
                        .path("/events/6")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(cookies -> {
                    cookies.add(SESSION_KEY, tokens[1]);
                }).exchange().expectStatus().isFound().expectHeader().value("Location", endsWith("/events"));

        client.method(HttpMethod.DELETE).uri(uriBuilder -> uriBuilder
                        .path("/events/6")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(cookies -> {
                    cookies.add(SESSION_KEY, tokens[1]);
                }).exchange().expectStatus().isNotFound();
    }

    @Test
    public void testEditPage() throws Exception {
        String[] tokens = login();

        // Then, attempt to open the edit page using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

    }

    @Test
    public void testEditPageError() throws Exception {
        String[] tokens = login();

        // Then, attempt to open the edit page using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5?error=1")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Please enter a valid name/venue ID."));
                });

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5?error=2")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Please enter a valid date."));
                });

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5?error=3")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Please enter a future date."));
                });

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5?error=4")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Unknown error."));
                });
    }

    @Test
    public void testEditEvent() throws Exception {
        String[] tokens = login();

        // Then, attempt to edit an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/edit/5")
                        .queryParam("id", "2")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "1")
                        .queryParam("date", "2030-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Changed Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", endsWith("/events"));


        // Finally, check that the event has been edited.
        client.get().uri("/events/description?id=5").accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Changed Description"));
                });
    }

    @Test
    public void testEditEventInvalidName() throws Exception {
        String[] tokens = login();

        // Then, attempt to edit an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/edit/5")
                        .queryParam("id", "2")
                        .queryParam("name", "")
                        .queryParam("venue_id", "1")
                        .queryParam("date", "2030-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=1"));
    }

    @Test
    public void testEditEventInvalidVenueId() throws Exception {
        String[] tokens = login();

        // Then, attempt to edit an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/edit/5")
                        .queryParam("id", "2")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "")
                        .queryParam("date", "2030-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=1"));

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/edit/5")
                        .queryParam("id", "2")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "99")
                        .queryParam("date", "2030-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=1"));
    }

    @Test
    public void testEditEventInvalidDate() throws Exception {
        String[] tokens = login();

        // Then, attempt to edit an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/edit/5")
                        .queryParam("id", "2")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "1")
                        .queryParam("date", "")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=2"));
    }

    @Test
    public void testEditEventFutureDate() throws Exception {
        String[] tokens = login();

        // Then, attempt to edit an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/edit/5")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/edit/5")
                        .queryParam("id", "2")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "1")
                        .queryParam("date", "2000-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=3"));
    }

    @Test
    public void testAddPage() throws Exception {
        String[] tokens = login();

        // Then, attempt to open the add page using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

    }

    @Test
    public void testAddPageError() throws Exception {
        String[] tokens = login();

        // Then, attempt to open the edit page using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add?error=1&name=&date=&time=&description=&venue_id=99")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Please enter a valid name/venue ID."));
                });

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add?error=2&name=Test%20Event&date=&time=&description=&venue_id=2")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Please enter a valid date."));
                });

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add?error=3&name=Test%20Event&date=2023-05-02&time=&description=&venue_id=2")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Please enter a future date."));
                });

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add?error=4")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk()
                .expectBody(String.class).consumeWith(result -> {
                    assertThat(result.getResponseBody(), containsString("Unknown error."));
                });
    }

    @Test
    public void testAddEvent() throws Exception {
        String[] tokens = login();

        // Then, attempt to add an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/add")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "1")
                        .queryParam("date", "2030-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", endsWith("/events"));
    }

    @Test
    public void testAddEventInvalidName() throws Exception {
        String[] tokens = login();

        // Then, attempt to add an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/add")
                        .queryParam("name", "")
                        .queryParam("venue_id", "1")
                        .queryParam("date", "2030-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=1"));
    }

    @Test
    public void testAddEventInvalidVenueId() throws Exception {
        String[] tokens = login();

        // Then, attempt to add an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/add")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "")
                        .queryParam("date", "2030-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=1"));

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/add")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "99")
                        .queryParam("date", "2030-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=1"));
    }

    @Test
    public void testAddEventInvalidDate() throws Exception {
        String[] tokens = login();

        // Then, attempt to add an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/add")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "1")
                        .queryParam("date", "")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=2"));
    }

    @Test
    public void testAddEventFutureDate() throws Exception {
        String[] tokens = login();

        // Then, attempt to add an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                        .path("/events/add")
                        .queryParam("name", "Test Event")
                        .queryParam("venue_id", "1")
                        .queryParam("date", "2000-01-01")
                        .queryParam("time", "12:00")
                        .queryParam("description", "Test Description")
                        .build())
                .accept(MediaType.TEXT_HTML).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=3"));
    }

    @Test
    public void testPostComment() {
        String[] tokens = login();

        // Then, attempt to add an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);
        String comment_content = "Test Comment " + LocalDateTime.now();
        client.post().uri(uriBuilder -> uriBuilder
                .path("/events/postComment/5")
                .queryParam("comment", comment_content)
                .build()).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=0"));

        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        String accessToken = "AQHbd7AEwgaFHDARmaYPSPjkIvFrIMu-ZWhnpU2AIN0";

        MastodonClient mastodonClient = new MastodonClient.Builder("mastodon.online", new OkHttpClient.Builder(), new Gson())
                .accessToken(accessToken)
                .useStreamingApi()
                .build();

        Timelines timelines = new Timelines(mastodonClient);
        Statuses statusesMethod = new Statuses(mastodonClient);
        Pageable<Status> statuses = null;
        try {
            statuses = timelines.getHome().execute();
        }
        catch (Mastodon4jRequestException e) {
            e.printStackTrace();
        }
        List<Status> statusesList = statuses.getPart();
        for (Status status : statusesList) {
            if (status.getAccount().getDisplayName().equals("MoN14") && status.getContent().contains(comment_content)) {
                try {
                    statusesMethod.deleteStatus(status.getId());
                }
                catch (Mastodon4jRequestException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    @Test
    public void testPostCommentWithBadData() {
        String[] tokens = login();

        // Then, attempt to add an event using the authenticated session cookie and CSRF token.
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("_csrf", tokens[0]);

        client.mutate().filter(basicAuthentication("Rob", "Haines")).build().get().uri("/events/add")
                .accept(MediaType.TEXT_HTML).exchange().expectStatus().isOk().expectBody(String.class);

        client.post().uri(uriBuilder -> uriBuilder
                .path("/events/postComment/5")
                .queryParam("comment", "")
                .build()).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData).cookies(c -> c.add(SESSION_KEY, tokens[1])).exchange().expectStatus().isFound().expectHeader()
                .value("Location", containsString("error=1"));
    }

}
