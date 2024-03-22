package com.svenruppert;

import io.javalin.Javalin;

public class RestService {

  public static final int     DEFAULT_PORT = 7070;
  private final       Javalin service;
  private             int     port;


  public RestService() {
    this.port = DEFAULT_PORT;
    service   = initService();
  }

  public RestService(int port) {
    this.port = port;
    service   = initService();
  }

  public Javalin getService() {
    return service;
  }

  public Javalin startService() {
    return service.start(port);
  }

  public Javalin startService(int port) {
    return service.start(port);
  }

  private Javalin initService() {
    return Javalin.create(/*config*/)
                  .get("/", ctx -> ctx.result("Hello World"))
                  .get("/upper/{value}/{name}", ctx -> {
                    String value = ctx.pathParam("value");
                    String name  = ctx.pathParam("name");
                    ctx.result(new UpperCaseService().toUpperCase(value + "-" + name));
                  });
  }


}
