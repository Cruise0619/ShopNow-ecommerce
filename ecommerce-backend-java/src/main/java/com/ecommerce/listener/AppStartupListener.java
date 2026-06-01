package com.ecommerce.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        File uploadDir = new File(System.getProperty("user.dir"), "uploads");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        sce.getServletContext().log("Ecommerce application started. Upload directory ensured.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().log("Ecommerce application stopped.");
    }
}
