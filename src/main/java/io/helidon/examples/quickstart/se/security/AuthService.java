package io.helidon.examples.quickstart.se.security;

import io.helidon.examples.quickstart.se.dto.LoginForm;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class AuthService implements HttpService {

  @Override
  public void routing(HttpRules rules) {
    rules.post("/web/login", this::login);
  }

  private void login(ServerRequest request, ServerResponse response) {
    LoginForm loginForm = request.content().as(LoginForm.class);

  }

}
