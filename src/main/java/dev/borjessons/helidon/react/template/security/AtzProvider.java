package dev.borjessons.helidon.react.template.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.security.AuthorizationResponse;
import io.helidon.security.Grant;
import io.helidon.security.ProviderRequest;
import io.helidon.security.Subject;
import io.helidon.security.spi.AuthorizationProvider;

public class AtzProvider implements AuthorizationProvider {
  private static final Logger logger = LoggerFactory.getLogger(AtzProvider.class);

  @Override
  public AuthorizationResponse authorize(ProviderRequest context) {
    logger.info("in atzProvider - context {}", context);

    return context.securityContext().isAuthorized() ? AuthorizationResponse.permit() : AuthorizationResponse.deny();
  }

  @Override
  public boolean isUserInRole(Subject subject, String role) {
    logger.info("in atzProvider - subject {}, role {}", subject, role);

    boolean authorized = subject.grantsByType("role").stream().map(Grant::getName).toList().contains(role);

    logger.info("in atzProvider - authorized {}", authorized);

    return authorized;
  }
}
