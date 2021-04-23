package io.fortylines.hrcrm.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateUserDto {

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String email;

    private Boolean isActive;

    @NotNull
    private Long roleId;
}
