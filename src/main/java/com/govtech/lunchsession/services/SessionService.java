package com.govtech.lunchsession.services;

import com.govtech.lunchsession.entities.SessionEntity;
import com.govtech.lunchsession.entities.UserParticipationEntity;
import com.govtech.lunchsession.entities.UserParticipationKey;
import com.govtech.lunchsession.openapi.api.SessionsApiDelegate;
import com.govtech.lunchsession.openapi.model.NewSession;
import com.govtech.lunchsession.openapi.model.Session;
import com.govtech.lunchsession.openapi.model.SuggestRestaurantRequest;
import com.govtech.lunchsession.repositories.SessionRepository;
import com.govtech.lunchsession.repositories.UserParticipationRepository;
import com.govtech.lunchsession.repositories.UserRepository;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SessionService implements SessionsApiDelegate {

  private UserRepository userRepository;
  private SessionRepository sessionRepository;
  private UserParticipationRepository userParticipationRepository;

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public ResponseEntity<Session> createSession(final NewSession newSession, final Long userId) {
    return getEntityAndProcess(
        userId,
        userRepository,
        user -> {
          var sessionToCreate = new SessionEntity(newSession.getName(), user);
          var createdSession = sessionRepository.save(sessionToCreate);
          createdSession.addParticipant(user);
          createdSession = sessionRepository.save(createdSession);
          return ResponseEntity.status(HttpStatus.CREATED).body(createdSession.toSession());
        });
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public ResponseEntity<Session> addSessionUser(Long sessionId, Long invitedUserId, Long userId) {
    return getEntityAndProcess(
        sessionId,
        sessionRepository,
        session -> {
          if (!session.getCreator().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
          }
          if (Session.StatusEnum.ENDED.getValue().equals(session.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          return getEntityAndProcess(
              invitedUserId,
              userRepository,
              invitedUser -> {
                session.addParticipant(invitedUser);
                sessionRepository.save(session);
                return ResponseEntity.status(HttpStatus.CREATED).body(session.toSession());
              });
        });
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public ResponseEntity<Void> suggestRestaurant(
      Long sessionId, SuggestRestaurantRequest suggestRestaurantRequest, Long userId) {

    return getEntityAndProcess(
        new UserParticipationKey(userId, sessionId),
        userParticipationRepository,
        (userParticipation -> {
          if (Session.StatusEnum.ENDED
              .getValue()
              .equals(userParticipation.getSession().getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }
          userParticipation.setRestaurant(suggestRestaurantRequest.getName());
          userParticipationRepository.save(userParticipation);
          return ResponseEntity.ok().build();
        }));
  }

  @Override
  @Transactional(isolation = Isolation.REPEATABLE_READ)
  public ResponseEntity<Session> endSession(Long sessionId, Long userId) {

    return getEntityAndProcess(
        sessionId,
        sessionRepository,
        session -> {
          if (!session.getCreator().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
          }

          if (Session.StatusEnum.ENDED.getValue().equals(session.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
          }

          var restaurants =
              session.getParticipants().stream()
                  .map(UserParticipationEntity::getRestaurant)
                  .filter(restaurant -> !Objects.isNull(restaurant))
                  .toList();
          if (restaurants.isEmpty()) {
            return ResponseEntity.notFound().build();
          }
          var chosen = restaurants.get(new Random().nextInt(restaurants.size()));
          session.setChosenRestaurant(chosen);
          session.setStatus(Session.StatusEnum.ENDED.getValue());
          return ResponseEntity.ok(sessionRepository.save(session).toSession());
        });
  }

  private <T, ID, R> ResponseEntity<R> getEntityAndProcess(
      final ID id,
      final CrudRepository<T, ID> repo,
      final Function<T, ResponseEntity<R>> processUser) {
    var entity = repo.findById(id);
    if (entity.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return processUser.apply(entity.get());
  }
}
