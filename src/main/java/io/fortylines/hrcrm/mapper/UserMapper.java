package io.fortylines.hrcrm.mapper;

import io.fortylines.hrcrm.dto.ReadUserDto;
import io.fortylines.hrcrm.entity.User;

public interface UserMapper {
    ReadUserDto toReadUserDto(User user);
}
