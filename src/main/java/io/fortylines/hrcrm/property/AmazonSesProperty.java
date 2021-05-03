package io.fortylines.hrcrm.property;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class AmazonSesProperty {

    @Value("${aws.ses.email.access-key}")
    private String accessKey;

    @Value("${aws.ses.email.secret-key}")
    private String secretKey;

    @Value("${aws.ses.email.mail-from}")
    private String mailFrom;
}
