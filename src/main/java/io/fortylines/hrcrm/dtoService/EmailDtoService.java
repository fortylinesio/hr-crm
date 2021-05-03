package io.fortylines.hrcrm.dtoService;

import io.fortylines.hrcrm.dto.CreateMailDto;
import io.fortylines.hrcrm.dto.CreateMailSendingDto;
import io.fortylines.hrcrm.dto.ReadEmailDto;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface EmailDtoService {
    List<ReadEmailDto> getAllMessages() throws MessagingException, IOException;
    List<ReadEmailDto> getUnreadMessages() throws IOException, MessagingException;
    void mailSending(CreateMailSendingDto createMailSendingDto);
    void sendMessage(CreateMailDto createMailDto);
    void delete(Long id, String fileName) throws MessagingException;
}
