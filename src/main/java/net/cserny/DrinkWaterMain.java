package net.cserny;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DrinkWaterMain {

    private static final Logger LOGGER = Logger.getLogger(DrinkWaterMain.class.getSimpleName());
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private LocalDateTime prevHour = LocalDateTime.now();

    public static void main(String[] args) {
        try {
            FileHandler fileLogHandler = new FileHandler("drinkwater.log");
            fileLogHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileLogHandler);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }

        new DrinkWaterMain().run();
    }

    private void run() {
        LOGGER.info("Starting application");

        Image icon = Toolkit.getDefaultToolkit().getImage(
                Thread.currentThread().getContextClassLoader().getResource("icon.png"));
        TrayIcon trayIcon = new TrayIcon(icon, "Drink More Water");
        trayIcon.setImageAutoSize(true);

        try {
            SystemTray tray = SystemTray.getSystemTray();
            tray.add(trayIcon);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }

        executorService.scheduleAtFixedRate(() -> {
            try {
                LocalDateTime current = LocalDateTime.now();
                if (ChronoUnit.HOURS.between(prevHour, current) > 1) {
                    prevHour = current;
                    trayIcon.displayMessage("Drink Water Notification",
                            "An hour has passed, you need to drink more water!", TrayIcon.MessageType.INFO);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                executorService.shutdownNow();
                LOGGER.info("Exiting application");
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
