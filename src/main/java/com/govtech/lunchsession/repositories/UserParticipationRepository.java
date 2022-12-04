package com.govtech.lunchsession.repositories;

import com.govtech.lunchsession.entities.UserParticipationEntity;
import com.govtech.lunchsession.entities.UserParticipationKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserParticipationRepository
    extends CrudRepository<UserParticipationEntity, UserParticipationKey> {}
