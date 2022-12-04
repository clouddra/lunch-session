package com.govtech.lunchsession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govtech.lunchsession.entities.SessionEntity;
import com.govtech.lunchsession.entities.UserEntity;
import com.govtech.lunchsession.entities.UserParticipationKey;
import com.govtech.lunchsession.openapi.model.NewSession;
import com.govtech.lunchsession.openapi.model.Session;
import com.govtech.lunchsession.openapi.model.Session.StatusEnum;
import com.govtech.lunchsession.openapi.model.SuggestRestaurantRequest;
import com.govtech.lunchsession.repositories.SessionRepository;
import com.govtech.lunchsession.repositories.UserParticipationRepository;
import com.govtech.lunchsession.repositories.UserRepository;
import com.govtech.lunchsession.services.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SessionApplicationTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private SessionRepository sessionRepository;
  @Autowired private UserParticipationRepository userParticipationRepository;
  @Autowired private SessionService sessionService;

  @Test
  public void createSessionShouldFailWith404WhenUserNotFound() throws Exception {
    mockMvc
        .perform(
            post("/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new NewSession().name("createSessionShouldFailWith404WhenUserNotFound")))
                .header("X-USER-ID", 999L)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void createSessionShouldFailWith409WSameSessionIsCreatedForSameUser() throws Exception {
    var user =
        addUser(
            "createSessionShouldFailWith409WSameSessionIsCreatedForSameUser@email.com",
            "createSessionShouldFailWith409WSameSessionIsCreatedForSameUser");
    var newSession = new NewSession().name(user.getName());
    mockMvc
        .perform(
            post("/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newSession))
                .header("X-USER-ID", user.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(newSession.getName()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("voting"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.creator.email").value(user.getEmail()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.creator.name").value(user.getName()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.participants[0].email").value(user.getEmail()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.participants[0].name").value(user.getName()));
    mockMvc
        .perform(
            post("/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newSession))
                .header("X-USER-ID", user.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  public void addUserToSessionSuccessfully() throws Exception {
    var user1 = addUser("addUserToSessionSuccessfully1@email.com", "addUserToSessionSuccessfully1");
    var user2 = addUser("addUserToSessionSuccessfully2@email.com", "addUserToSessionSuccessfully2");
    var session =
        sessionService
            .createSession(new NewSession().name(user1.getName()), user1.getId())
            .getBody();
    mockMvc
        .perform(
            put("/v1/sessions/{sessionId}/user/{invitedUserId}", session.getId(), user2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", user1.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(session.getId()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(session.getName()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(session.getStatus().getValue()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.creator.email").value(user1.getEmail()))
        .andExpect(MockMvcResultMatchers.jsonPath("$.creator.name").value(user1.getName()))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.participants[*].name", hasItem(user2.getName())))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.participants[*].email", hasItem(user2.getEmail())));
  }

  @Test
  public void addUserToNonExistentSession() throws Exception {
    var user1 = addUser("addUserToNonExistentSession1@email.com", "addUserToNonExistentSession1");
    var user2 = addUser("addUserToNonExistentSession2@email.com", "addUserToNonExistentSession2");
    mockMvc
        .perform(
            put("/v1/sessions/{sessionId}/user/{invitedUserId}", 999L, user2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", user1.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void addUserToSessionUnauthorizedUser() throws Exception {
    var user1 =
        addUser("addUserToSessionUnauthorizedUser1@email.com", "addUserToSessionUnauthorizedUser1");
    var user2 =
        addUser("addUserToSessionUnauthorizedUser2@email.com", "addUserToSessionUnauthorizedUser2");
    var session =
        sessionService
            .createSession(new NewSession().name(user2.getName()), user2.getId())
            .getBody();
    mockMvc
        .perform(
            put("/v1/sessions/{sessionId}/user/{invitedUserId}", session.getId(), user2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", user1.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  public void addUserToEndedSession() throws Exception {
    var user1 = addUser("addUserToEndedSession1@email.com", "addUserToEndedSession1");
    var user2 = addUser("addUserToEndedSession2@email.com", "addUserToEndedSession2");
    var session =
        endSessionFromRepo(
            sessionService
                .createSession(new NewSession().name(user1.getName()), user1.getId())
                .getBody());
    mockMvc
        .perform(
            put("/v1/sessions/{sessionId}/user/{invitedUserId}", session.getId(), user2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", user1.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  public void suggestRestaurantSuccess() throws Exception {
    var user = addUser("suggestRestaurantSuccess@email.com", "suggestRestaurantSuccess");
    var session =
        sessionService.createSession(new NewSession().name(user.getName()), user.getId()).getBody();
    ;

    mockMvc
        .perform(
            put("/v1/sessions/{sessionId}/suggest", session.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new SuggestRestaurantRequest().name("restaurant_name")))
                .header("X-USER-ID", user.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    assertThat(
        userParticipationRepository
            .findById(new UserParticipationKey(user.getId(), session.getId()))
            .get()
            .getRestaurant(),
        is("restaurant_name"));
  }

  @Test
  public void suggestRestaurantNotFound() throws Exception {
    var user = addUser("suggestRestaurantNotFound@email.com", "suggestRestaurantNotFound");
    var session =
        sessionService.createSession(new NewSession().name(user.getName()), user.getId()).getBody();

    mockMvc
        .perform(
            put("/v1/sessions/{sessionId}/suggest", session.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new SuggestRestaurantRequest().name("restaurant_name")))
                .header("X-USER-ID", 999)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            put("/v1/sessions/{sessionId}/suggest", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new SuggestRestaurantRequest().name("restaurant_name")))
                .header("X-USER-ID", user.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void suggestRestaurantEndedSession() throws Exception {
    var user = addUser("suggestRestaurantEndedSession@email.com", "suggestRestaurantEndedSession");
    var session =
        endSessionFromRepo(
            sessionService
                .createSession(new NewSession().name(user.getName()), user.getId())
                .getBody());

    mockMvc
        .perform(
            put("/v1/sessions/{sessionId}/suggest", session.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new SuggestRestaurantRequest().name("restaurant_name")))
                .header("X-USER-ID", user.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  public void endSession() throws Exception {
    var user1 = addUser("endSession1@email.com", "endSession1");
    var user2 = addUser("endSession2@email.com", "endSession2");
    var user3 = addUser("endSession3@email.com", "endSession3");

    var session =
        sessionService
            .createSession(new NewSession().name(user1.getName()), user1.getId())
            .getBody();

    sessionService.addSessionUser(session.getId(), user2.getId(), user1.getId());
    sessionService.addSessionUser(session.getId(), user3.getId(), user1.getId());

    sessionService.suggestRestaurant(
        session.getId(), new SuggestRestaurantRequest().name("user1suggestion"), user1.getId());
    sessionService.suggestRestaurant(
        session.getId(), new SuggestRestaurantRequest().name("user2suggestion"), user2.getId());
    sessionService.suggestRestaurant(
        session.getId(), new SuggestRestaurantRequest().name("user2suggestion"), user3.getId());

    mockMvc
        .perform(
            post("/v1/sessions/{sessionId}/end", session.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", user1.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath(
                "$.chosenRestaurant",
                oneOf("user1suggestion", "user2suggestion", "user3suggestion")))
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("ended"));
  }

  @Test
  public void endSessionForEndedSession() throws Exception {
    var user = addUser("endSessionForEndedSession@email.com", "endSessionForEndedSession");

    var session =
        endSessionFromRepo(
            sessionService
                .createSession(new NewSession().name(user.getName()), user.getId())
                .getBody());

    mockMvc
        .perform(
            post("/v1/sessions/{sessionId}/end", session.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", user.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  public void endSessionWhenNoSuggestions() throws Exception {
    var user = addUser("endSessionWhenNoSuggestions@email.com", "endSessionWhenNoSuggestions");

    var session =
        sessionService.createSession(new NewSession().name(user.getName()), user.getId()).getBody();

    mockMvc
        .perform(
            post("/v1/sessions/{sessionId}/end", session.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-USER-ID", user.getId())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  private UserEntity addUser(final String email, final String name) {
    return userRepository.save(new UserEntity(email, name));
  }

  private SessionEntity endSessionFromRepo(final Session session) {
    var sessionEntity = sessionRepository.findById(session.getId()).get();
    sessionEntity.setStatus(StatusEnum.ENDED.getValue());
    return sessionRepository.save(sessionEntity);
  }
}
