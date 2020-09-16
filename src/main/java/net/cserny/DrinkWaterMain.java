package net.cserny;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DrinkWaterMain {

    private static final Logger LOGGER = Logger.getLogger(DrinkWaterMain.class.getSimpleName());
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final AtomicBoolean paused = new AtomicBoolean();

    public static void main(String[] args) {
        initLogger();
        new DrinkWaterMain().run();
    }

    private static void initLogger() {
        try {
            FileHandler fileLogHandler = new FileHandler("drinkwater.log");
            fileLogHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileLogHandler);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void run() {
        LOGGER.info("Starting application");
        TrayIcon trayIcon = produceDrinkWaterTrayIcon();
        addToSystem(trayIcon);
        executorService.scheduleAtFixedRate(new DrinkWaterThread(trayIcon, this::shutdownApplication, paused),
                0, 10, TimeUnit.SECONDS);
    }

    private void addToSystem(TrayIcon trayIcon) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            tray.add(trayIcon);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    private TrayIcon produceDrinkWaterTrayIcon() {
        Image icon = Toolkit.getDefaultToolkit().getImage(
                Thread.currentThread().getContextClassLoader().getResource("glass.png"));
        TrayIcon trayIcon = new TrayIcon(icon, "Drink More Water");
        trayIcon.setImageAutoSize(true);
        PopupMenu menu = new PopupMenu();
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.addActionListener(e -> shutdownApplication());
        menu.add(exitMenuItem);
        CheckboxMenuItem pauseMenuItem = new CheckboxMenuItem("Pause");
        pauseMenuItem.addActionListener(e -> pauseNotifications(pauseMenuItem));
        menu.add(pauseMenuItem);
        trayIcon.setPopupMenu(menu);
        return trayIcon;
    }

    private void pauseNotifications(CheckboxMenuItem pauseMenuItem) {
        boolean initialEnabledState = pauseMenuItem.getState();
        if (initialEnabledState) {
            paused.set(false);
            pauseMenuItem.setState(false);
        } else {
            paused.set(true);
            pauseMenuItem.setState(true);
        }
    }

    private void shutdownApplication() {
        LOGGER.info("Exiting application");
        System.exit(1);
    }

    private static class DrinkWaterThread implements Runnable {

        private final TrayIcon trayIcon;
        private final Runnable shutdownApplication;
        private final AtomicBoolean paused;
        private LocalDateTime prevHour = LocalDateTime.now();

        public DrinkWaterThread(TrayIcon trayIcon, Runnable shutdownApplication, AtomicBoolean paused) {
            this.trayIcon = trayIcon;
            this.shutdownApplication = shutdownApplication;
            this.paused = paused;
        }

        @Override
        public void run() {
            try {
                if (paused.get()) {
                    return;
                }

                LocalDateTime current = LocalDateTime.now();
                if (ChronoUnit.HOURS.between(prevHour, current) >= 1) {
                    prevHour = current;
                    trayIcon.displayMessage("Drink Water Notification",
                            "An hour has passed, you need to drink some water!", TrayIcon.MessageType.INFO);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                shutdownApplication.run();
            }
        }
    }
}
