package com.github.malyshevhen;

import com.github.malyshevhen.api.Controller;
import com.github.malyshevhen.proxy.PDUHandler;
import io.javalin.Javalin;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

  private static final int API_SERVER_PORT = 8080;
  private static final String ALLOW_METHODS = "GET, POST, PUT, DELETE, OPTIONS";

  public static void main(String[] args) {
    int port = args.length > 0 ? Integer.parseInt(args[0]) : 2775;
    String usersFileName = args.length > 1 ? args[1] : null;

    PDUHandler pduHandler = new PDUHandler();

    log.info("Starting SMPP simulator with port {} and users file {}", port, usersFileName);
    SmppSim smppSim = new SmppSim(pduHandler, port, usersFileName);

    try {
      log.info("Starting SMPP simulator...");
      smppSim.start();
    } catch (IOException e) {
      log.error("Error starting SMPP simulator.", e);
      e.printStackTrace();
    }
    log.info("SMPP simulator started.");

    // Create a controller instance and pass the PDUHandler to it.
    log.info("Starting API server...");
    Controller controller = new Controller(pduHandler);
    Javalin app = runApiServer(controller);
    app.start(API_SERVER_PORT);
    log.info("API server started on port {}.", API_SERVER_PORT);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Shutdown signal received. Stopping SMPP simulator...");
                  try {
                    smppSim.stop();
                    app.stop();
                  } catch (IOException ioEx) {
                    log.error("Error stopping SMPP simulator during shutdown.", ioEx);
                  }
                  log.info("SMPP simulator stopped (shutdown hook).");
                }));

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

  private static Javalin runApiServer(Controller controller) {
    Javalin app =
        Javalin.create(
            config -> {
              config.useVirtualThreads = true;
              config.http.asyncTimeout = 10_000L;
            });

    app.before(ctx -> ctx.header("Access-Control-Allow-Origin", "*"));
    app.before(ctx -> ctx.header("Access-Control-Allow-Methods", ALLOW_METHODS));

    app.get("/api/v1/requests/single_sm", ctx -> ctx.json(controller.getSubmitSMs()));
    app.get("/api/v1/requests/multi_sm", ctx -> ctx.json(controller.getSubmitMultiSMs()));
    app.get("/api/v1/requests/bind", ctx -> ctx.json(controller.getBindRequests()));

    app.get("/api/v1/responses/single_sm", ctx -> ctx.json(controller.getSubmitSMResponses()));
    app.get("/api/v1/responses/multi_sm", ctx -> ctx.json(controller.getSubmitMultiSMResponses()));

    return app;
  }
}
