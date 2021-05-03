package io.fortylines.hrcrm.dtoService.Impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import io.fortylines.hrcrm.dto.CreateMailDto;
import io.fortylines.hrcrm.dto.CreateMailSendingDto;
import io.fortylines.hrcrm.dto.ReadEmailDto;
import io.fortylines.hrcrm.dtoService.EmailDtoService;
import io.fortylines.hrcrm.property.AmazonSesProperty;
import io.fortylines.hrcrm.service.S3FileUploadService;
import io.fortylines.hrcrm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.FlagTerm;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class DefaultEmailDtoService implements EmailDtoService {

    @Value("${spring.mail.host}")
    private String HOST;
    @Value("${spring.mail.port}")
    private int PORT;
    @Value("${spring.mail.username}")
    private String USERNAME;
    @Value("${spring.mail.password}")
    private String PASSWORD;
    @Value("${attachment.upload.dir}")
    private String FILE_DIRECTORY;

    private final UserService userService;
    private final AmazonSesProperty amazonSesProperty;
    private final S3FileUploadService s3FileUploadService;

    @Autowired
    public DefaultEmailDtoService(UserService userService, AmazonSesProperty amazonSesProperty,
                                  S3FileUploadService s3FileUploadService) {
        this.userService = userService;
        this.amazonSesProperty = amazonSesProperty;
        this.s3FileUploadService = s3FileUploadService;
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return properties;
    }

    @Override
    public List<ReadEmailDto> getUnreadMessages() throws IOException, MessagingException {
        Properties properties = getProperties();
        Session session = Session.getDefaultInstance(properties);
        Store store = null;
        try {
            store = session.getStore("imap");
            store.connect(HOST, PORT, USERNAME, PASSWORD);
            Folder inbox = null;
            try {
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);

                FlagTerm flag = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                Message[] unreadMessage = inbox.search(flag);
                inbox.setFlags(unreadMessage, new Flags(Flags.Flag.SEEN), true);

                return readMessages(unreadMessage);
            } finally {
                if (inbox != null) {
                    inbox.close();
                }
            }
        } finally {
            if (store != null) {
                store.close();
            }
        }
    }

    @Override
    public List<ReadEmailDto> getAllMessages() throws MessagingException, IOException {
        Properties properties = getProperties();
        Session session = Session.getDefaultInstance(properties);
        Store store = null;
        try {
            store = session.getStore("imap");
            store.connect(HOST, PORT, USERNAME, PASSWORD);
            Folder inbox = null;
            try {
                inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                Message[] messages = inbox.getMessages();

                return readMessages(messages);
            } finally {
                if (inbox != null) {
                    inbox.close();
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
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        String decoded = MimeUtility.decodeText(part.getFileName());
                        String fileName = message.getReceivedDate().toString().replaceAll(
                                "[ ]|[:]", "_") + "_" + decoded;
                        part.saveFile(FILE_DIRECTORY + fileName);
                        File file = ResourceUtils.getFile(FILE_DIRECTORY + fileName);
                        s3FileUploadService.uploadFile(fileName, file);
                        findFileAndDelete(fileName, new File(FILE_DIRECTORY));
                        readEmailDto.setFileName(fileName);
                        readEmailDto.setSize(part.getSize());
                    }
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

    @Override
    public void mailSending(CreateMailSendingDto createMailSendingDto) {
        List<String> emailsList = userService.getAllEmailsByRoleId(createMailSendingDto.getRoleId());
        int size = 0;
        if (emailsList != null) {
            size = emailsList.size();
        }

        for (int i = 0; i < size; i++) {
            CreateMailDto createMailDto = new CreateMailDto();
            createMailDto.setToAddress(emailsList.get(i));
            createMailDto.setSubject(createMailSendingDto.getSubject());
            createMailDto.setText(createMailSendingDto.getText());
            sendMessage(createMailDto);
        }
    }

    @Override
    public void sendMessage(CreateMailDto createMailDto) {
        try {
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(
                    amazonSesProperty.getAccessKey(), amazonSesProperty.getSecretKey());
            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion(Regions.EU_CENTRAL_1)
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .build();

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(createMailDto.getToAddress()))
                    .withMessage(new com.amazonaws.services.simpleemail.model.Message()
                            .withBody(new Body()
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(createMailDto.getText())))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(createMailDto.getSubject())))
                    .withSource(amazonSesProperty.getMailFrom());
            client.sendEmail(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Long id, String fileName) throws MessagingException {
        int messageId = (int) (long) id;
        Properties properties = getProperties();
        Session session = Session.getDefaultInstance(properties);
        Store store = session.getStore("imap");
        store.connect(HOST, PORT, USERNAME, PASSWORD);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        Message message = inbox.getMessage(Math.toIntExact(messageId));
        try {
            message.setFlag(Flags.Flag.DELETED, true);
            findFileAndDelete(fileName, new File(FILE_DIRECTORY));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findFileAndDelete(String fileName, File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File tempFile : files) {
                if (tempFile.isDirectory()) {
                    findFileAndDelete(fileName, file);
                } else if (fileName.equalsIgnoreCase(tempFile.getName())) {
                    tempFile.delete();
                }
            }
        }
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