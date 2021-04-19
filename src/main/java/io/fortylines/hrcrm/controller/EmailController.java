package io.fortylines.hrcrm.controller;

import io.fortylines.hrcrm.dto.ReadEmailDto;
import io.fortylines.hrcrm.dtoService.EmailDtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v1/mail")
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_HR')")
public class EmailController {

    private final EmailDtoService emailDtoService;

    @Autowired
    public EmailController(EmailDtoService emailDtoService) {
        this.emailDtoService = emailDtoService;
    }

    @GetMapping
    public List<ReadEmailDto> getMails() throws MessagingException, IOException {
        return emailDtoService.getMails();
    }

    @PostMapping("/send-email")
    public void sendEmail(@RequestParam String to,
                          @RequestParam String subject,
                          @RequestParam String text) throws MessagingException {
        emailDtoService.sendMessage(to, subject, text);
    }
}