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
    public List<ReadEmailDto> getAllMessages() throws MessagingException, IOException {
        return emailDtoService.getAllMessages();
    }

    @GetMapping("/unread-messages")
    public List<ReadEmailDto> getUnreadMessages() throws IOException, MessagingException {
        return emailDtoService.getUnreadMessages();
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam Integer id, @RequestParam String fileName) throws MessagingException {
        emailDtoService.delete(id, fileName);
    }

    @PostMapping("/mail-sending")
    public void mailSending(@RequestParam String subject,
                            @RequestParam String text,
                            @RequestParam Long roleId) throws MessagingException {
        emailDtoService.mailSending(subject, text, roleId);
    }

    @PostMapping("/send-email")
    public void sendMessage(@RequestParam String toAddress,
                            @RequestParam String subject,
                            @RequestParam String text) throws MessagingException {
        emailDtoService.sendMessage(toAddress, subject, text);
    }
}
