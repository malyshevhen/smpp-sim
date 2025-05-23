package com.github.malyshevhen.proxy;

import java.io.IOException;
import org.smpp.Connection;
import org.smpp.pdu.PDU;
import org.smpp.pdu.PDUException;
import org.smpp.smscsim.PDUProcessor;
import org.smpp.smscsim.PDUProcessorFactory;
import org.smpp.smscsim.SMSCSession;

public class SMSCSessionProxy implements SMSCSession {

  private final SMSCSession delegate;
  private final PDUHandler pduHandler;

  public SMSCSessionProxy(SMSCSession delegate, PDUHandler pduHandler) {
    this.delegate = delegate;
    this.pduHandler = pduHandler;
  }

  @Override
  public void stop() {
    delegate.stop();
  }

  @Override
  public void run() {
    delegate.run();
  }

  @Override
  public void send(PDU pdu) throws IOException, PDUException {
    pduHandler.handleClientRequest(pdu);
    delegate.send(pdu);
  }

  @Override
  public void setPDUProcessor(PDUProcessor pduProcessor) {
    delegate.setPDUProcessor(pduProcessor);
  }

  @Override
  public void setPDUProcessorFactory(PDUProcessorFactory pduProcessorFactory) {
    delegate.setPDUProcessorFactory(pduProcessorFactory);
  }

  @Override
  public void setReceiveTimeout(long timeout) {
    delegate.setReceiveTimeout(timeout);
  }

  @Override
  public long getReceiveTimeout() {
    return delegate.getReceiveTimeout();
  }

  @Override
  public Object getAccount() {
    return delegate.getAccount();
  }

  @Override
  public void setAccount(Object account) {
    delegate.setAccount(account);
  }

  @Override
  public Connection getConnection() {
    return delegate.getConnection();
  }
}
