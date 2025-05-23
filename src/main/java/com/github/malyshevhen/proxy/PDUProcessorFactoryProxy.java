package com.github.malyshevhen.proxy;

import org.smpp.smscsim.PDUProcessor;
import org.smpp.smscsim.PDUProcessorFactory;
import org.smpp.smscsim.SMSCSession;

public class PDUProcessorFactoryProxy implements PDUProcessorFactory {

  private final PDUProcessorFactory delegate;
  private final PDUHandler pduHandler;

  public PDUProcessorFactoryProxy(PDUProcessorFactory delegate, PDUHandler proxyHandler) {
    this.delegate = delegate;
    this.pduHandler = proxyHandler;
  }

  @Override
  public PDUProcessor createPDUProcessor(SMSCSession session) {
    return delegate.createPDUProcessor(new SMSCSessionProxy(session, pduHandler));
  }
}
