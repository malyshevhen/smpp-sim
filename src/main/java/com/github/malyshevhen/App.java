package com.github.malyshevhen;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
  public static void main(String[] args) {
    int port = args.length > 0 ? Integer.parseInt(args[0]) : 2775;
    String usersFileName = args.length > 1 ? args[1] : null;

    log.info("Starting SMPP simulator with port {} and users file {}", port, usersFileName);
    SmppSim smppSim = new SmppSim(port, usersFileName);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("Shutdown signal received. Stopping SMPP simulator...");
      try {
        smppSim.stop();
      } catch (IOException ioEx) {
        log.error("Error stopping SMPP simulator during shutdown.", ioEx);
      }
      log.info("SMPP simulator stopped (shutdown hook).");
    }));

    try {
      log.info("Starting SMPP simulator...");
      smppSim.start();
    } catch (IOException e) {
      log.error("Error starting SMPP simulator.", e);
      e.printStackTrace();
    }

    // Wait for interrupt signal to exit from the system.
    synchronized (App.class) {
      try {
        log.info("Waiting for SMPP simulator to exit...");
        App.class.wait();
      } catch (InterruptedException e) {
        log.info("Interrupted. Stopping SMPP simulator...");
      }
    }

    log.info("SMPP simulator stopped.");
  }
}
