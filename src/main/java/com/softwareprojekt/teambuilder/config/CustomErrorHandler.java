package com.softwareprojekt.teambuilder.config;

import com.softwareprojekt.teambuilder.views.ErrorPageView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.RouteNotFoundError;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
//Author: Silas Weber
@Component
public class CustomErrorHandler extends RouteNotFoundError implements ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorHandler.class);

    @Override
    public void error(ErrorEvent errorEvent) {
        logger.error("Something wrong happened", errorEvent.getThrowable());
        if(UI.getCurrent() != null) {
            UI.getCurrent().access(() -> {
                UI.getCurrent().navigate(ErrorPageView.class);
            });
        }
    }

    @Override
    public int setErrorParameter(final BeforeEnterEvent event, final ErrorParameter parameter) {
        event.forwardTo(ErrorPageView.class);
        return HttpServletResponse.SC_NOT_FOUND;
    }
}

