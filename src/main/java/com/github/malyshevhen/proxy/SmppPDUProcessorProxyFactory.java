package com.github.malyshevhen.proxy;

import org.smpp.smscsim.PDUProcessor;
import org.smpp.smscsim.PDUProcessorFactory;
import org.smpp.smscsim.SMSCSession;

public class SmppPDUProcessorProxyFactory implements PDUProcessorFactory {

  private final PDUProcessorFactory delegate;

  public SmppPDUProcessorProxyFactory(PDUProcessorFactory delegate) {
    this.delegate = delegate;
  }

  @Override
  public PDUProcessor createPDUProcessor(SMSCSession session) {
    PDUProcessor processor = delegate.createPDUProcessor(session);
    return new PDUProcessorProxy(processor.getGroup(), processor);
  }
}
