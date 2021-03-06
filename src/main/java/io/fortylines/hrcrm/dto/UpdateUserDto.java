package io.fortylines.hrcrm.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateUserDto {

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    private Boolean isActive;

    @NotNull
    private Long roleId;
}
