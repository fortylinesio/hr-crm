package io.fortylines.hrcrm.dtoService.Impl;

import io.fortylines.hrcrm.dto.CreateCandidateDto;
import io.fortylines.hrcrm.dto.ReadCandidateDto;
import io.fortylines.hrcrm.dto.UpdateCandidateDto;
import io.fortylines.hrcrm.dtoService.CandidateDtoService;
import io.fortylines.hrcrm.entity.Candidate;
import io.fortylines.hrcrm.entity.Vacancy;
import io.fortylines.hrcrm.mapper.CandidateMapper;
import io.fortylines.hrcrm.service.CandidateService;
import io.fortylines.hrcrm.service.S3FileUploadService;
import io.fortylines.hrcrm.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class DefaultCandidateDtoService implements CandidateDtoService {

    @Value("${file.upload.dir}")
    public String FILE_DIRECTORY;

    private final VacancyService vacancyService;
    private final CandidateService candidateService;
    private final CandidateMapper candidateMapper;
    private final S3FileUploadService s3FileUploadService;

    @Autowired
    public DefaultCandidateDtoService(VacancyService vacancyService, CandidateService candidateService,
                                      CandidateMapper candidateMapper, S3FileUploadService s3FileUploadService) {
        this.vacancyService = vacancyService;
        this.candidateService = candidateService;
        this.candidateMapper = candidateMapper;
        this.s3FileUploadService = s3FileUploadService;
    }

    @Override
    public ReadCandidateDto create(MultipartFile resume, CreateCandidateDto createCandidateDto) throws IOException {
        Candidate newCandidate = new Candidate();
        Vacancy vacancy = vacancyService.getById(createCandidateDto.getVacancyId());
        String dateNow = LocalDateTime.now().withNano(0).withSecond(0).toString().replaceAll(":", "-");
        String fileName = dateNow + "_" + resume.getOriginalFilename();
        s3FileUploadService.upload(fileName, resume);

        newCandidate.setFileName(fileName);
        newCandidate.setFirstName(createCandidateDto.getFirstName());
        newCandidate.setLastName(createCandidateDto.getLastName());
        newCandidate.setPhoneNumber(createCandidateDto.getPhoneNumber());
        newCandidate.setDegree(createCandidateDto.getDegree());
        newCandidate.setDepartment(createCandidateDto.getDepartment());
        newCandidate.setDiscord(createCandidateDto.getDiscord());
        newCandidate.setEmail(createCandidateDto.getEmail());
        newCandidate.setYearsOfExperience(createCandidateDto.getYearsOfExperience());
        newCandidate.setVacancy(vacancy);

        Candidate candidate = candidateService.create(newCandidate);
        return candidateMapper.toReadCandidateDto(candidate);
    }

    @Override
    public ReadCandidateDto update(Long id, MultipartFile resume,
                                   UpdateCandidateDto updateCandidateDto) throws IOException {
        Candidate updateCandidate = new Candidate();
        Vacancy vacancy = vacancyService.getById(updateCandidateDto.getVacancyId());

        updateCandidate.setFirstName(updateCandidateDto.getFirstName());
        updateCandidate.setLastName(updateCandidateDto.getLastName());
        updateCandidate.setPhoneNumber(updateCandidateDto.getPhoneNumber());
        updateCandidate.setDegree(updateCandidateDto.getDegree());
        updateCandidate.setDepartment(updateCandidateDto.getDepartment());
        updateCandidate.setDiscord(updateCandidateDto.getDiscord());
        updateCandidate.setEmail(updateCandidateDto.getEmail());
        updateCandidate.setYearsOfExperience(updateCandidateDto.getYearsOfExperience());
        updateCandidate.setVacancy(vacancy);

        Candidate candidate = candidateService.update(id, resume, updateCandidate);
        return candidateMapper.toReadCandidateDto(candidate);
    }

    @Override
    public ReadCandidateDto getById(Long id) {
        Candidate candidate = candidateService.getById(id);
        return candidateMapper.toReadCandidateDto(candidate);
    }

    @Override
    public Page<ReadCandidateDto> getAll(Pageable pageable) {
        Page<Candidate> candidates = candidateService.getAll(pageable);
        return candidateMapper.toReadCandidateDtoList(candidates);
    }

    @Override
    public void delete(String fileName, Long id) {
        s3FileUploadService.deleteFile(fileName);
        candidateService.delete(id);
    }
}
