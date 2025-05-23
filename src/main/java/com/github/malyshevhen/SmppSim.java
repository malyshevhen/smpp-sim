package com.github.malyshevhen;

import com.github.malyshevhen.api.Controller;
import com.github.malyshevhen.proxy.PDUHandler;
import com.github.malyshevhen.proxy.PDUProcessorFactoryProxy;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.smpp.smscsim.DeliveryInfoSender;
import org.smpp.smscsim.PDUProcessorFactory;
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

  private static final String DEFAULT_USERS_FILE = "users.cfg";

  private final String usersFileName;
  private final SMSCListener smscListener;
  private final PDUProcessorGroup processors;
  private final ShortMessageStore messageStore;
  private final DeliveryInfoSender deliveryInfoSender;
  private final PDUHandler pduHandler;

  public SmppSim(PDUHandler pduHandler, int port, String usersFileName) {
    this.usersFileName = usersFileName;
    this.pduHandler = pduHandler;
    this.smscListener = new SMSCListenerImpl(port, true);
    this.processors = new PDUProcessorGroup();
    this.messageStore = new ShortMessageStore();
    this.deliveryInfoSender = new DeliveryInfoSender();
  }

  protected void start() throws IOException {
    log.info("Start SMPP simulator.");

    deliveryInfoSender.start();
    Table users = getUsers(usersFileName);
    PDUProcessorFactory factory =
        new PDUProcessorFactoryProxy(
            new SimulatorPDUProcessorFactory(processors, messageStore, deliveryInfoSender, users),
            pduHandler);
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

  /**
   * Loads the users table from the given file name or from the classpath if not provided.
   *
   * @param usersFileName the file path or null/empty to load from classpath
   * @return Table of users
   */
  private static Table getUsers(String usersFileName) {
    try {
      if (usersFileName != null && !usersFileName.isBlank()) {
        return loadUsersFromFile(usersFileName);
      }
      return loadUsersFromClasspath();
    } catch (IOException e) {
      log.error("Error reading users file: {}", usersFileName, e);
      throw new RuntimeException("Failed to load users file: " + usersFileName, e);
    }
  }

  private static Table loadUsersFromFile(String fileName) throws IOException {
    log.info("Reading users file from file system: {}", fileName);
    return new Table(fileName);
  }

  private static Table loadUsersFromClasspath() throws IOException {
    try (InputStream input = App.class.getClassLoader().getResourceAsStream(DEFAULT_USERS_FILE)) {
      if (input == null) {
        log.error("Users file not found in classpath: {}", DEFAULT_USERS_FILE);
        throw new RuntimeException("Users file not found: " + DEFAULT_USERS_FILE);
      }

      log.info("Reading users file from classpath: {}", DEFAULT_USERS_FILE);
      Table table = new Table();
      table.read(input);
      return table;
    }
  }
}
