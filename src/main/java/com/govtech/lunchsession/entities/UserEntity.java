package com.govtech.lunchsession.entities;

import com.govtech.lunchsession.openapi.model.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "lunch_users")
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column @NonNull private String email;

  @Column @NonNull private String name;

  @OneToMany(mappedBy = "creator")
  private Set<SessionEntity> createdSessions;

  @OneToMany(mappedBy = "participant", cascade = CascadeType.MERGE, orphanRemoval = true)
  private Set<UserParticipationEntity> sessionsParticipated = new HashSet<>();

  public User toUser() {
    return new User().name(name).id(id).email(email);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) return false;

    var user = (UserEntity) o;
    return Objects.equals(email, user.email) && Objects.equals(name, user.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, name);
  }
}
