package io.helidon.examples.quickstart.se.security;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.security.AuthenticationResponse;
import io.helidon.security.ProviderRequest;
import io.helidon.security.SecurityResponse;
import io.helidon.security.spi.AuthenticationProvider;

public class AtnProvider implements AuthenticationProvider {
  private static final Logger logger = LoggerFactory.getLogger(AtnProvider.class);

  private static AuthenticationResponse createFailureResponse() {
    return AuthenticationResponse.builder()
        .description("authentication failed")
        .throwable(null)
        .status(SecurityResponse.SecurityStatus.FAILURE)
        .responseHeaders(Map.of("WWW-Authenticate", List.of("")))
        .build();
  }

  @Override
  public AuthenticationResponse authenticate(ProviderRequest providerRequest) {
    logger.debug("ENTERED AUTH PROVIDER");
    return AuthenticationResponse.success(null, null);
    //return createFailureResponse();
  }
}
