package io.fortylines.hrcrm.mapper.Impl;

import io.fortylines.hrcrm.dto.ReadUserDto;
import io.fortylines.hrcrm.entity.User;
import io.fortylines.hrcrm.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserMapperImpl implements UserMapper {

    @Override
    public ReadUserDto toReadUserDto(User user) {
        ReadUserDto userDto = new ReadUserDto();
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setActive(user.isActive());
        userDto.setRoles(user.getRoles());
        userDto.setUsername(user.getUsername());

        return userDto;
    }
}
