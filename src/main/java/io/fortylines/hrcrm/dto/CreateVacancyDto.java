package io.fortylines.hrcrm.dto;

import io.fortylines.hrcrm.entity.Competencies;
import io.fortylines.hrcrm.entity.User;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateVacancyDto {

    @NotNull
    private String title;

    @NotNull
    private String description;

    @NotNull
    private String requirements;

    @NotNull
    private List<Competencies> competencies;

    private User userId;
}
