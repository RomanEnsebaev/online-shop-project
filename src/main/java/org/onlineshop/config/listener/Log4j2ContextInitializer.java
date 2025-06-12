package org.onlineshop.config.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.InputStream;
import java.net.URL;

@WebListener
public class Log4j2ContextInitializer implements ServletContextListener {

    private LoggerContext ctx;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            URL configUrl = getClass().getClassLoader().getResource("log4j2.xml");
            try (InputStream in = configUrl.openStream()) {
                ConfigurationSource source = new ConfigurationSource(in, configUrl);
                ctx = Configurator.initialize(null, source);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (ctx != null) {
            ctx.close();
        }
    }
}
