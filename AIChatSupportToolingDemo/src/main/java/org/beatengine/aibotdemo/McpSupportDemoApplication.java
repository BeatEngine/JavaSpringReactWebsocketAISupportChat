package org.beatengine.aibotdemo;

import org.beatengine.aibotdemo.aiintegration.ChatClientConfigurations;
import org.beatengine.aibotdemo.entity.Product;
import org.beatengine.aibotdemo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SpringBootApplication
public class McpSupportDemoApplication {

    private static boolean loadedRootProjectEnvironment = false;
    public static boolean loadRootProjectEnvironmentVariables()
    {
        if(loadedRootProjectEnvironment)
        {
            return true;
        }
        Properties props = new Properties();
        Path envFile = Paths.get(GLOBAL_DEFINITIONS.ROOT_ENVIRONMENT_PATH);
        try (var inputStream = Files.newInputStream(envFile)) {
            props.load(inputStream);
            int c = 0;
            for(Map.Entry<Object,Object> e : props.entrySet())
            {
                System.setProperty(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                c++;
            }
            loadedRootProjectEnvironment = true;
            System.getLogger(McpSupportDemoApplication.class.getSimpleName()).log(System.Logger.Level.INFO,
                    "Loading root environment adding " + c + " properties from " + envFile.getFileName().toString());
            return true;
        }
        catch (IOException ioe)
        {
            System.getLogger(McpSupportDemoApplication.class.getSimpleName()).log(System.Logger.Level.ERROR,
                    "Environment definition load error: " + GLOBAL_DEFINITIONS.ROOT_ENVIRONMENT_PATH +
                            " --> " + envFile.toString() + " " + ioe.getMessage());

        }
        return false;
    }

    public static void main(String[] args) {
        if(loadRootProjectEnvironmentVariables()) {
            final SpringApplication app = new SpringApplication(McpSupportDemoApplication.class);
            app.setDefaultProperties(ChatClientConfigurations.getAiConfiguration()); //Set all Spring Properties mapping from environment.
            ConfigurableApplicationContext applicationContext = app.run(McpSupportDemoApplication.class, args);
        }
        else
        {
            System.exit(1);
        }
    }



    @Bean
    CommandLineRunner initDatabase(ProductRepository repository) {
        final System.Logger log = System.getLogger(McpSupportDemoApplication.class.getSimpleName());
        return args -> {
            log.log(System.Logger.Level.WARNING,"Clear Products ");
            repository.deleteAll();
            log.log(System.Logger.Level.WARNING,"Preloading " + repository.save(new Product("Engine-A2DED", "Runs the vehicle.", 699.99f)));
            log.log(System.Logger.Level.WARNING,"Preloading " + repository.save(new Product("Light-EFEE4", "Is shining.", 26.70f)));
            log.log(System.Logger.Level.WARNING,"Preloading " + repository.save(new Product("Infotainment-63FFE2", "Infos and atmosphere.", 211.0f)));
            log.log(System.Logger.Level.WARNING,"Preloading " + repository.save(new Product("Battery-63w3E2", "80 kWh.", 7850.74f)));
        };
    }

}
