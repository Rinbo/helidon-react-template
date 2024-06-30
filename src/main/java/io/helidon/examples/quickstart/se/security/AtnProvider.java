package io.helidon.examples.quickstart.se.security;

import io.helidon.security.AuthenticationResponse;
import io.helidon.security.ProviderRequest;
import io.helidon.security.spi.AuthenticationProvider;

public class AtnProvider implements AuthenticationProvider {
  @Override
  public AuthenticationResponse authenticate(ProviderRequest providerRequest) {
    System.out.println("======================");
    System.out.println("HELLO WORLD");
    System.out.println("======================");
    return AuthenticationResponse.success(null, null);
  }

}
