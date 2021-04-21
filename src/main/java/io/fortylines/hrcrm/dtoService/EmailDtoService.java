package io.fortylines.hrcrm.dtoService;

import io.fortylines.hrcrm.dto.ReadEmailDto;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface EmailDtoService {
    List<ReadEmailDto> getAllMessages() throws MessagingException, IOException;
    List<ReadEmailDto> getUnreadMessages() throws IOException, MessagingException;
    void sendMessage(String to, String subject, String text) throws MessagingException;
    void delete(Integer id, String fileName) throws MessagingException;
}
