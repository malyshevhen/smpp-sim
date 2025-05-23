package com.github.malyshevhen.proxy;

import com.github.malyshevhen.api.ShortMessageInfo;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.Request;
import org.smpp.pdu.Response;

@Slf4j
public class PDUProxyHandler {
  private final ConcurrentMap<String, BindRequest> bindRequestsBySender = new ConcurrentHashMap<>();
  private final ConcurrentMap<String, ShortMessageInfo> messagesBySender =
      new ConcurrentHashMap<>();

  public BindRequest getBindRequest(String sender) {
    return bindRequestsBySender.get(sender);
  }

  public ShortMessageInfo getMessage(String sender) {
    return messagesBySender.get(sender);
  }

  public void handleClientRequest(Request request) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleClientRequest'");
  }

  public void handleClientResponse(Response response) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleClientResponse'");
  }

  public void handleServerRequest(Request request) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleServerRequest'");
  }

  public void handleServerResponse(Response response) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'handleServerResponse'");
  }

  public void clean() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'clean'");
  }
}
