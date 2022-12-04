package com.govtech.lunchsession.entities;

import com.govtech.lunchsession.openapi.model.Participant;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_participation")
public class UserParticipationEntity {
  @EmbeddedId private UserParticipationKey id;

  @ManyToOne
  @MapsId("user")
  @JoinColumn(name = "participant")
  @NonNull
  UserEntity participant;

  @ManyToOne
  @MapsId("session")
  @JoinColumn(name = "lunch_session")
  @NonNull
  private SessionEntity session;

  @Column private String restaurant;

  public UserParticipationEntity(final UserEntity participant, final SessionEntity session) {
    this.participant = participant;
    this.session = session;
    this.id = new UserParticipationKey(participant.getId(), session.getId());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    var userParticipation = (UserParticipationEntity) o;
    return Objects.equals(participant, userParticipation.participant)
        && Objects.equals(session, userParticipation.session);
  }

  @Override
  public int hashCode() {
    return Objects.hash(participant, session);
  }

  public Participant toParticipant() {
    return new Participant()
        .name(participant.getName())
        .email(participant.getEmail())
        .id(participant.getId())
        .restaurant(restaurant);
  }
}
