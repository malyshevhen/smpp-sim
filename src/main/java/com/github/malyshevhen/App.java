package com.github.malyshevhen;

import com.github.malyshevhen.api.Server;
import com.github.malyshevhen.proxy.PDUHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

  private static final int API_SERVER_PORT = 8080;
  private static final int SMPP_SERVER_PORT = 2775;

  public static void main(String[] args) {
    int apiServerPort = args.length > 0 ? Integer.parseInt(args[0]) : API_SERVER_PORT;
    int smppPort = args.length > 1 ? Integer.parseInt(args[1]) : SMPP_SERVER_PORT;
    String usersFileName = args.length > 2 ? args[2] : null;

    PDUHandler pduHandler = new PDUHandler();

    log.info("Starting SMPP simulator with port {} and users file {}", smppPort, usersFileName);
    SmppSim simulator = new SmppSim(pduHandler, smppPort, usersFileName);
    simulator.addShutdownHook();
    simulator.start();
    log.info("SMPP simulator started.");

    log.info("Starting API server...");
    Server server = new Server(pduHandler);
    server.addShutdownHook();
    server.start(apiServerPort);
    log.info("API server started on port {}.", apiServerPort);

    // Wait for interrupt signal to exit from the system.
    synchronized (App.class) {
      try {
        log.info("Waiting for SMPP simulator to exit...");
        App.class.wait();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.info("Interrupted. Stopping SMPP simulator...");
      }
    }

    log.info("SMPP simulator stopped.");
  }
}
