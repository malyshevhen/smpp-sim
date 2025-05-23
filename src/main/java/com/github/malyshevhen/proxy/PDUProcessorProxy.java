package com.github.malyshevhen.proxy;

import java.io.IOException;
import org.smpp.pdu.PDUException;
import org.smpp.pdu.Request;
import org.smpp.pdu.Response;
import org.smpp.smscsim.PDUProcessor;
import org.smpp.smscsim.PDUProcessorGroup;

public class PDUProcessorProxy extends PDUProcessor {

  private final PDUProcessor delegate;
  private final PDUProxyHandler proxyHandler;

  public PDUProcessorProxy(PDUProcessorGroup group, PDUProcessor delegate) {
    super(group);
    this.delegate = delegate;
    this.proxyHandler = new PDUProxyHandler();
  }

  @Override
  public void clientRequest(Request request) {
    proxyHandler.handleClientRequest(request);
    delegate.clientRequest(request);
  }

  @Override
  public void clientResponse(Response response) {
    proxyHandler.handleClientResponse(response);
    delegate.clientResponse(response);
  }

  @Override
  public void serverRequest(Request request) throws IOException, PDUException {
    proxyHandler.handleServerRequest(request);
    delegate.serverRequest(request);
  }

  @Override
  public void serverResponse(Response response) throws IOException, PDUException {
    proxyHandler.handleServerResponse(response);
    delegate.serverResponse(response);
  }

  @Override
  public void stop() {
    proxyHandler.clean();
    delegate.stop();
  }
}
