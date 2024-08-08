package dev.borjessons.helidon.react.template.email;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.borjessons.helidon.react.template.data.model.Passcode;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

public class EmailSender {
  private static final String BODY_HTML = "<html><head></head><body><h1>Hello!</h1><p>Here is your login passcode: %REPLACE_ME%</p></body></html>";
  private static final String FROM = "robin.b@outlook.com";
  private static final String SUBJECT = "Passcode for borjessons.dev";
  private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

  private final SesClient sesClient;

  public EmailSender(SesClient sesClient) {
    Objects.requireNonNull(sesClient, "sesClient must not be null");

    this.sesClient = sesClient;
  }

  public EmailSender() {
    sesClient = SesClient.create();
  }

  public void sendEmail(String to, Passcode passcode) {
    Destination destination = Destination.builder()
        .toAddresses(to)
        .build();

    Content content = Content.builder()
        .data(BODY_HTML.replace("%REPLACE_ME%", passcode.value()))
        .build();

    Content sub = Content.builder()
        .data(SUBJECT)
        .build();

    Body body = Body.builder()
        .html(content)
        .build();

    Message msg = Message.builder()
        .subject(sub)
        .body(body)
        .build();

    SendEmailRequest emailRequest = SendEmailRequest.builder()
        .destination(destination)
        .message(msg)
        .source(FROM)
        .build();

    try {
      logger.debug("Sending email to {}", to);
      sesClient.sendEmail(emailRequest);
    } catch (SesException e) {
      logger.error("Failed to send email to {}", to, e);
    }
  }
}
