package com.github.malyshevhen.api;

import com.github.malyshevhen.proxy.PDUHandler;
import io.javalin.Javalin;

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

    app.get("/api/v1/requests/single_sm", ctx -> ctx.json(controller.getSubmitSMs()));
    app.get("/api/v1/requests/multi_sm", ctx -> ctx.json(controller.getSubmitMultiSMs()));
    app.get("/api/v1/requests/bind", ctx -> ctx.json(controller.getBindRequests()));
  }

  public void stop() {
    app.stop();
  }

  public void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
  }
}
