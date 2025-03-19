package dev.maheshbabu11.htmxwithjava;

import java.awt.EventQueue;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class MyApp {
    
    public static void main(String[] args) {
        var ctx = new SpringApplicationBuilder(MyApp.class)
                .headless(false).web(WebApplicationType.NONE).run(args);

        EventQueue.invokeLater(() -> {
            var ex = ctx.getBean(TextAreaExample.class);
            ex.setVisible(true);
        });
        
    }
    
}
