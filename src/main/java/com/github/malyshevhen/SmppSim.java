package com.github.malyshevhen;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.smpp.smscsim.DeliveryInfoSender;
import org.smpp.smscsim.PDUProcessorGroup;
import org.smpp.smscsim.SMSCListener;
import org.smpp.smscsim.SMSCListenerImpl;
import org.smpp.smscsim.SMSCSession;
import org.smpp.smscsim.ShortMessageStore;
import org.smpp.smscsim.SimulatorPDUProcessor;
import org.smpp.smscsim.SimulatorPDUProcessorFactory;
import org.smpp.smscsim.util.Table;

@Slf4j
public class SmppSim {

  private final String usersFileName;
  private final SMSCListener smscListener;
  private final PDUProcessorGroup processors;
  private final ShortMessageStore messageStore;
  private final DeliveryInfoSender deliveryInfoSender;

  public SmppSim(int port, String usersFileName) {
    this.usersFileName = usersFileName;
    this.smscListener = new SMSCListenerImpl(port, true);
    this.processors = new PDUProcessorGroup();
    this.messageStore = new ShortMessageStore();
    this.deliveryInfoSender = new DeliveryInfoSender();
  }

  protected void start() throws IOException {
    log.info("Start SMPP simulator.");

    deliveryInfoSender.start();
    Table users = new Table(usersFileName);
    SimulatorPDUProcessorFactory factory =
        new SimulatorPDUProcessorFactory(processors, messageStore, deliveryInfoSender, users);
    factory.setDisplayInfo(true);
    smscListener.setPDUProcessorFactory(factory);
    smscListener.start();

    log.info("started.");
  }

  protected void stop() throws IOException {
    log.info("Stopping listener...");

    synchronized (processors) {
      int procCount = processors.count();
      SimulatorPDUProcessor proc;
      SMSCSession session;
      for (int i = 0; i < procCount; i++) {
        proc = (SimulatorPDUProcessor) processors.get(i);
        session = proc.getSession();
        log.info("Stopping session {}: {} ...", i, proc.getSystemId());
        session.stop();
        log.info(" stopped.");
      }
    }
    smscListener.stop();

    log.info("SMPP simulator stopped.");
  }
}
