package io.helidon.examples.quickstart.se.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.http.PathMatcher;
import io.helidon.http.PathMatchers;
import io.helidon.webserver.http.Filter;
import io.helidon.webserver.http.FilterChain;
import io.helidon.webserver.http.RoutingRequest;
import io.helidon.webserver.http.RoutingResponse;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

public class AuthFilter implements Filter {
  public static final PathMatcher AUTHENTICATE = PathMatchers.create("/auth/web/authenticate");
  public static final PathMatcher LOGIN = PathMatchers.create("/auth/web/login");
  public static final PathMatcher LOGOUT = PathMatchers.create("/auth/web/logout");
  public static final PathMatcher REGISTER = PathMatchers.create("/auth/web/register");
  public static final PathMatcher ROOT = PathMatchers.create("/");

  private static final PathMatcher ASSETS = PathMatchers.create("/assets/*");
  private static final PathMatcher FILES = PathMatchers.create("/{file}");

  private static final List<PathMatcher> EXCLUDED_PATHS = List.of(AUTHENTICATE, LOGIN, LOGOUT, REGISTER, ROOT, ASSETS, FILES);
  private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

  private AuthFilter() {
  }

  public static Filter create() {
    return new AuthFilter();
  }

  private static boolean shouldExclude(RoutingRequest req) {
    return EXCLUDED_PATHS.stream().anyMatch(path -> path.match(req.path()).accepted());
  }

  @Override
  public void filter(FilterChain chain, RoutingRequest req, RoutingResponse resp) {
    if (!shouldExclude(req)) applyFilter(req, resp);

    chain.proceed();
  }

  private void applyFilter(ServerRequest req, ServerResponse resp) {
    // do nothing for now
  }
}
