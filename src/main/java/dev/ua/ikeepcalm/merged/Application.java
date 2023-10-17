
package dev.ua.ikeepcalm.merged;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

@EnableScheduling
@SpringBootApplication
@ComponentScan(basePackages={"dev.ua.ikeepcalm.merged", "org.telegram.telegrambots"})
public class Application implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Enter a command (type 'stop' to exit): ");
            String input = scanner.nextLine();

            String[] parts = input.split(" ", 2);
            String command = parts[0].toLowerCase();
            String arguments = parts.length > 1 ? parts[1] : null;

            if (command.equals("stop")) {
                System.out.println("Shutting down the application...");
                SpringApplication.exit(applicationContext);
                break;
            }else {
                System.out.println("Unknown command. Please, try again.");
            }
        } scanner.close();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
