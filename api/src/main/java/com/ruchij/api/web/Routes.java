package com.ruchij.api.web;

import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.api.services.crawler.ExtendedCrawlManager;
import com.ruchij.api.services.health.HealthService;
import com.ruchij.api.services.user.UserService;
import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.routes.AuthenticationRoute;
import com.ruchij.api.web.routes.LinkedInRoute;
import com.ruchij.api.web.routes.ServiceRoute;
import com.ruchij.api.web.routes.UserRoute;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes implements EndpointGroup {
    private final UserRoute userRoute;
    private final LinkedInRoute linkedInRoute;
    private final AuthenticationRoute authenticationRoute;
    private final ServiceRoute serviceRoute;

    public Routes(
        ExtendedCrawlManager extendedCrawlManager,
        UserService userService,
        AuthenticationService authenticationService,
        LinkedInCredentialsService linkedInCredentialsService,
        HealthService healthService
    ) {
        AuthenticationMiddleware authenticationMiddleware = new AuthenticationMiddleware(authenticationService);

        this.userRoute = new UserRoute(userService);
        this.authenticationRoute = new AuthenticationRoute(authenticationService, authenticationMiddleware);
        this.linkedInRoute = new LinkedInRoute(linkedInCredentialsService, extendedCrawlManager, authenticationMiddleware);
        this.serviceRoute = new ServiceRoute(healthService);
    }

    @Override
    public void addEndpoints() {
        path("user", userRoute);
        path("authentication", authenticationRoute);
        path("linkedIn", linkedInRoute);
        path("service", serviceRoute);
    }
}
