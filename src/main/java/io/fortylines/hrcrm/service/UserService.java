package io.fortylines.hrcrm.service;

import io.fortylines.hrcrm.dto.CreateUserDto;
import io.fortylines.hrcrm.dto.ReadUserDto;
import io.fortylines.hrcrm.dto.UpdateUserDto;
import io.fortylines.hrcrm.entity.User;

public interface UserService {
    ReadUserDto getUserProfile(Long id);

    User getById(Long id);

    ReadUserDto createNewUser(CreateUserDto createUserDto);

    ReadUserDto update(Long id, UpdateUserDto updateUserDto);

    void delete(Long id);
}
