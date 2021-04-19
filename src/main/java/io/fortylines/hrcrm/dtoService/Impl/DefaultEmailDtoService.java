package io.fortylines.hrcrm.dtoService.Impl;

import io.fortylines.hrcrm.dto.ReadEmailDto;
import io.fortylines.hrcrm.dtoService.EmailDtoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class DefaultEmailDtoService implements EmailDtoService {

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private int port;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;

    @Override
    public void sendMessage(String to, String subject, String text) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.yandex.ru");
        properties.put("mail.smtp.auth", true);
        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getDefaultInstance(properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(text);
        Transport.send(message);
    }

    @Override
    public List<ReadEmailDto> getMails() throws MessagingException, IOException {
        Properties properties = new Properties();
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        Session session = Session.getDefaultInstance(properties);
        Store store = null;
        try {
            store = session.getStore("imap");
            store.connect(host, port, username, password);
            Folder inbox = null;
            try {
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                Message[] messages = inbox.getMessages();

                return readMessages(messages);
            } finally {
                if (inbox != null) {
                    inbox.close(false);
                }
            }
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    private List<ReadEmailDto> readMessages(Message[] messages) throws MessagingException, IOException {
        List<ReadEmailDto> emailDtoList = new ArrayList<>();
        for (Message message : messages) {
            ReadEmailDto readEmailDto = new ReadEmailDto();
            if (hasAttachments(message)) {
                Multipart multipart = (Multipart) message.getContent();
                int partCount = multipart.getCount();
                for (int i = 0; i < partCount; i++) {
                    MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(i);
                    readEmailDto.setFileName(part.getFileName());
                    readEmailDto.setSize(part.getSize());
                }
            }
            Address[] froms = message.getFrom();
            String email = froms == null ? null : ((InternetAddress) froms[0]).getAddress();
            readEmailDto.setFrom(email);
            readEmailDto.setMsgId((long) message.getMessageNumber());
            readEmailDto.setSubject(message.getSubject());
            readEmailDto.setText(getTextFromMessage(message));
            readEmailDto.setReceivedAt(message.getReceivedDate());
            emailDtoList.add(readEmailDto);
        }
        return emailDtoList;
    }

    private boolean hasAttachments(Message msg) throws MessagingException, IOException {
        if (msg.isMimeType("multipart/mixed")) {
            Multipart mp = (Multipart) msg.getContent();
            return mp.getCount() > 1;
        }
        return false;
    }

    private String getTextFromMessage(Message message) throws IOException, MessagingException {
        String text = "";
        if (message.isMimeType("text/plain")) {
            text = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            text = getTextFromMimeMultipart(mimeMultipart);
        }
        return text;
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws IOException, MessagingException {
        int count = mimeMultipart.getCount();
        boolean multipartAlt = new ContentType(mimeMultipart.getContentType()).match("multipart/alternative");

        if (count == 0) {
            throw new MessagingException("Multipart with no body parts not supported.");
        }
        if (multipartAlt) {
            return getTextFromBodyPart(mimeMultipart.getBodyPart(count - 1));
        }
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            text.append(getTextFromBodyPart(bodyPart));
        }
        return text.toString();
    }

    private String getTextFromBodyPart(BodyPart bodyPart) throws IOException, MessagingException {
        String text = "";
        if (bodyPart.isMimeType("text/plain")) {
            text = (String) bodyPart.getContent();
        } else if (bodyPart.isMimeType("text/html")) {
            String html = (String) bodyPart.getContent();
            text = org.jsoup.Jsoup.parse(html).text();
        } else if (bodyPart.getContent() instanceof MimeMultipart) {
            text = getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
        }
        return text;
    }
}