package com.softwareprojekt.teambuilder.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
//Author: Silas Weber

@Component
public class ServiceListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {

        event.getSource().addSessionInitListener(
                initEvent ->  {
                    LoggerFactory.getLogger(getClass()).info("A new Session has been initialized!");
                    VaadinSession.getCurrent().setErrorHandler(new CustomErrorHandler());
                });

        event.getSource().addUIInitListener(
                initEvent -> LoggerFactory.getLogger(getClass())
                        .info("A new UI has been initialized!"));
    }
}