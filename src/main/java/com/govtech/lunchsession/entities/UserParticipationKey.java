package com.govtech.lunchsession.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserParticipationKey implements Serializable {
  @Column(name = "participant")
  Long participant;

  @Column(name = "lunch_session")
  Long session;
}
