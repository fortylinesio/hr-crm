package io.fortylines.hrcrm.dtoService;

import io.fortylines.hrcrm.dto.ReadEmailDto;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface EmailDtoService {
    List<ReadEmailDto> getMails() throws MessagingException, IOException;
    void sendMessage(String to, String subject, String text) throws MessagingException;
}
