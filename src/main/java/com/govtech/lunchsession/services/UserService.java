package com.govtech.lunchsession.services;

import com.govtech.lunchsession.entities.UserEntity;
import com.govtech.lunchsession.openapi.api.UsersApiDelegate;
import com.govtech.lunchsession.openapi.model.NewUser;
import com.govtech.lunchsession.openapi.model.User;
import com.govtech.lunchsession.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UsersApiDelegate {

  private UserRepository userRepository;

  @Override
  public ResponseEntity<User> createUser(final NewUser newUser) {
    var newUserSaved = userRepository.save(new UserEntity(newUser.getEmail(), newUser.getName()));
    return ResponseEntity.status(HttpStatus.CREATED).body(newUserSaved.toUser());
  }
}
