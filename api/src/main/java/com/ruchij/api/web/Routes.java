package com.ruchij.api.web;

import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.api.services.user.UserService;
import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.routes.AuthenticationRoute;
import com.ruchij.api.web.routes.LinkedInRoute;
import com.ruchij.api.web.routes.UserRoute;
import com.ruchij.service.crawler.CrawlManager;
import com.ruchij.service.linkedin.LinkedInCredentialsService;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes implements EndpointGroup {
    private final UserRoute userRoute;
    private final LinkedInRoute linkedInRoute;
    private final AuthenticationRoute authenticationRoute;

    public Routes(
        CrawlManager crawlManager,
        UserService userService,
        AuthenticationService authenticationService,
        LinkedInCredentialsService linkedInCredentialsService
    ) {
        AuthenticationMiddleware authenticationMiddleware = new AuthenticationMiddleware(authenticationService);

        this.userRoute = new UserRoute(userService, authenticationMiddleware);
        this.authenticationRoute = new AuthenticationRoute(authenticationService, authenticationMiddleware);
        this.linkedInRoute = new LinkedInRoute(linkedInCredentialsService, crawlManager, authenticationMiddleware);
    }

    public Routes(UserRoute userRoute, LinkedInRoute linkedInRoute, AuthenticationRoute authenticationRoute) {
        this.userRoute = userRoute;
        this.linkedInRoute = linkedInRoute;
        this.authenticationRoute = authenticationRoute;
    }

    @Override
    public void addEndpoints() {
        path("user", userRoute);
        path("authentication", authenticationRoute);
        path("linkedIn", linkedInRoute);
    }
}
