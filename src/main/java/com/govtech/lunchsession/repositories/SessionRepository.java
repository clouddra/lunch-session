package com.govtech.lunchsession.repositories;

import com.govtech.lunchsession.entities.SessionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends CrudRepository<SessionEntity, Long> {}
