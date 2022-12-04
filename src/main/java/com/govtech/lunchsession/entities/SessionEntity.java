package com.govtech.lunchsession.entities;

import com.govtech.lunchsession.openapi.model.Session;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "lunch_sessions")
public class SessionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column @NonNull private String name;

  @Column(columnDefinition = "VARCHAR(255) DEFAULT 'voting' NOT NULL")
  private String status = "voting";

  @Column(name = "chosen_restaurant")
  private String chosenRestaurant;

  @ManyToOne
  @NonNull
  @JoinColumn(name = "creator", nullable = false)
  private UserEntity creator;

  @OneToMany(mappedBy = "session", cascade = CascadeType.MERGE, orphanRemoval = true)
  private Set<UserParticipationEntity> participants = new HashSet<>();

  //  private Participants

  public Session toSession() {
    return new Session()
        .id(id)
        .name(name)
        .status(Session.StatusEnum.fromValue(status))
        .creator(creator.toUser())
        .participants(
            participants.stream()
                .map(UserParticipationEntity::toParticipant)
                .collect(Collectors.toList()))
        .chosenRestaurant(chosenRestaurant);
  }

  public void addParticipant(final UserEntity user) {
    var vote = new UserParticipationEntity(user, this);
    participants.add(vote);
    user.getSessionsParticipated().add(vote);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    var session = (SessionEntity) o;
    return Objects.equals(name, session.name)
        && Objects.equals(status, session.status)
        && Objects.equals(creator, session.creator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, status, creator);
  }
}
