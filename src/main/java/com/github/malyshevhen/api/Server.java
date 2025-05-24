package com.github.malyshevhen.api;

import com.github.malyshevhen.proxy.PDUHandler;
import io.javalin.Javalin;
import io.javalin.http.Handler;

public class Server {

  private static final String ALLOW_METHODS = "GET, POST, PUT, DELETE, OPTIONS";
  private static final Long TIMEOUT = 10_000L;

  private final Javalin app;
  private final Controller controller;

  public Server(PDUHandler pduHandler) {
    Javalin javalin =
        Javalin.create(
            config -> {
              config.useVirtualThreads = true;
              config.http.asyncTimeout = TIMEOUT;
            });

    javalin.before(ctx -> ctx.header("Access-Control-Allow-Origin", "*"));
    javalin.before(ctx -> ctx.header("Access-Control-Allow-Methods", ALLOW_METHODS));

    this.app = javalin;
    this.controller = new Controller(pduHandler);
  }

  public void start(int port) {
    app.start(port);

    app.get("/api/v1/messages/short-messages", handleGetSingleMessages());
    app.get("/api/v1/messages/multi-messages", handleGetMultiMessages());
    app.get("/api/v1/bind-requests", handleGetBindRequests());
    app.post("/api/v1/clean", handleCleanMessages());
  }

  private Handler handleGetMultiMessages() {
    return ctx -> ctx.json(controller.getSubmitMultiSMs());
  }

  private Handler handleGetSingleMessages() {
    return ctx -> ctx.json(controller.getSubmitSMs());
  }

  private Handler handleGetBindRequests() {
    return ctx -> ctx.json(controller.getBindRequests());
  }

  private Handler handleCleanMessages() {
    return ctx -> {
      controller.clean();
      ctx.status(204);
    };
  }

  public void stop() {
    app.stop();
  }

  public void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
  }
}
