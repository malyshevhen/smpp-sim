package com.github.malyshevhen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smpp.Data;
import org.smpp.Session;
import org.smpp.TCPIPConnection;
import org.smpp.pdu.Address;
import org.smpp.pdu.AddressRange;
import org.smpp.pdu.BindReceiver;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindResponse;
import org.smpp.pdu.BindTransmitter;
import org.smpp.pdu.DeliverSM;
import org.smpp.pdu.PDU;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;
import org.smpp.pdu.UnbindResp;
import org.smpp.util.ByteBuffer;

@Slf4j
public class SmppSimTest {

  private static final int PORT = 2776;
  private static final String TEST_SYSTEM_ID = "pavel";
  private static final String TEST_PASSWORD = "dfsew";
  private static final String SOURCE_ADDR = "1234";
  private static final String DESTINATION_ADDR = "5678";
  private static final String TEST_MESSAGE = "Hello, SMPP world!";

  private SmppSim smppSim;
  private String tempUsersFile;

  @Before
  public void setUp() throws IOException {
    // Create a temporary users file with test credentials
    Path tempDir = Files.createTempDirectory("smpp-test");
    tempUsersFile = tempDir.resolve("test-users.txt").toString();

    String usersContent =
        "name="
            + TEST_SYSTEM_ID
            + "\n"
            + "password="
            + TEST_PASSWORD
            + "\n"
            + "timeout=unlimited\n";

    Files.write(Paths.get(tempUsersFile), usersContent.getBytes());

    // Start the SMPP simulator
    smppSim = new SmppSim(PORT, tempUsersFile);
    smppSim.start();

    // Give it a moment to initialize
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @After
  public void tearDown() throws IOException {
    // Stop the SMPP simulator
    if (smppSim != null) {
      smppSim.stop();
    }

    // Clean up temporary file
    Files.deleteIfExists(Paths.get(tempUsersFile));
  }

  @Test
  public void testBindTransmitter() throws Exception {
    // Create and set connection
    TCPIPConnection connection = new TCPIPConnection("localhost", PORT);
    connection.setReceiveTimeout(5000);
    Session session = new Session(connection);

    try {
      // Create transmitter bind request
      BindRequest bindRequest = new BindTransmitter();
      bindRequest.setSystemId(TEST_SYSTEM_ID);
      bindRequest.setPassword(TEST_PASSWORD);
      bindRequest.setSystemType("");
      bindRequest.setInterfaceVersion((byte) 0x34);
      bindRequest.setAddressRange(new AddressRange());

      // Send bind request
      BindResponse bindResponse = session.bind(bindRequest);

      // Verify bind success
      assertEquals(Data.ESME_ROK, bindResponse.getCommandStatus());
      assertNotNull(bindResponse.getSystemId());

      log.info("Successfully bound as transmitter");

      // Unbind and close session
      UnbindResp unbindResp = session.unbind();
      assertEquals(Data.ESME_ROK, unbindResp.getCommandStatus());
    } finally {
      session.close();
    }
  }

  @Test
  public void testBindReceiver() throws Exception {
    // Create and set connection
    TCPIPConnection connection = new TCPIPConnection("localhost", PORT);
    connection.setReceiveTimeout(5000);
    Session session = new Session(connection);

    try {
      // Create receiver bind request
      BindRequest bindRequest = new BindReceiver();
      bindRequest.setSystemId(TEST_SYSTEM_ID);
      bindRequest.setPassword(TEST_PASSWORD);
      bindRequest.setSystemType("");
      bindRequest.setInterfaceVersion((byte) 0x34);
      bindRequest.setAddressRange(new AddressRange());

      // Send bind request
      BindResponse bindResponse = session.bind(bindRequest);

      // Verify bind success
      assertEquals(Data.ESME_ROK, bindResponse.getCommandStatus());
      assertNotNull(bindResponse.getSystemId());

      log.info("Successfully bound as receiver");

      // Unbind and close session
      UnbindResp unbindResp = session.unbind();
      assertEquals(Data.ESME_ROK, unbindResp.getCommandStatus());
    } finally {
      session.close();
    }
  }

  @Test
  public void testSendAndReceiveMessage() throws Exception {
    // This test requires two sessions: one for sending and one for receiving

    // Create receiver session
    TCPIPConnection receiverConnection = new TCPIPConnection("localhost", PORT);
    receiverConnection.setReceiveTimeout(5000);
    Session receiverSession = new Session(receiverConnection);

    // Create transmitter session
    TCPIPConnection transmitterConnection = new TCPIPConnection("localhost", PORT);
    transmitterConnection.setReceiveTimeout(5000);
    Session transmitterSession = new Session(transmitterConnection);

    try {
      // Bind receiver
      BindRequest receiverBindRequest = new BindReceiver();
      receiverBindRequest.setSystemId(TEST_SYSTEM_ID);
      receiverBindRequest.setPassword(TEST_PASSWORD);
      receiverBindRequest.setSystemType("");
      receiverBindRequest.setInterfaceVersion((byte) 0x34);
      AddressRange addressRange = new AddressRange();
      addressRange.setTon((byte) 1);
      addressRange.setNpi((byte) 1);
      addressRange.setAddressRange(DESTINATION_ADDR);
      receiverBindRequest.setAddressRange(addressRange);

      BindResponse receiverBindResponse = receiverSession.bind(receiverBindRequest);
      assertEquals(Data.ESME_ROK, receiverBindResponse.getCommandStatus());
      log.info("Receiver bound successfully");

      // Bind transmitter
      BindRequest transmitterBindRequest = new BindTransmitter();
      transmitterBindRequest.setSystemId(TEST_SYSTEM_ID);
      transmitterBindRequest.setPassword(TEST_PASSWORD);
      transmitterBindRequest.setSystemType("");
      transmitterBindRequest.setInterfaceVersion((byte) 0x34);
      transmitterBindRequest.setAddressRange(new AddressRange());

      BindResponse transmitterBindResponse = transmitterSession.bind(transmitterBindRequest);
      assertEquals(Data.ESME_ROK, transmitterBindResponse.getCommandStatus());
      log.info("Transmitter bound successfully");

      // Create a latch to wait for the message
      CountDownLatch messageLatch = new CountDownLatch(1);
      AtomicBoolean messageReceived = new AtomicBoolean(false);
      AtomicReference<String> receivedMessageContent = new AtomicReference<>();

      // Start a thread to listen for incoming messages
      Thread receiverThread =
          new Thread(
              () -> {
                try {
                  PDU pdu;
                  while ((pdu = receiverSession.receive()) != null) {
                    if (pdu instanceof DeliverSM) {
                      DeliverSM deliverSM = (DeliverSM) pdu;
                      String message = new String(deliverSM.getShortMessage());
                      log.info("Received message: {}", message);
                      receivedMessageContent.set(message);
                      messageReceived.set(true);
                      messageLatch.countDown();
                      break;
                    }
                  }
                } catch (Exception e) {
                  log.error("Error in receiver thread", e);
                }
              });
      receiverThread.start();

      // Send a message
      SubmitSM submitSM = new SubmitSM();
      submitSM.setSourceAddr(new Address((byte) 1, (byte) 1, SOURCE_ADDR));
      submitSM.setDestAddr(new Address((byte) 1, (byte) 1, DESTINATION_ADDR));
      submitSM.setShortMessage(TEST_MESSAGE);

      SubmitSMResp submitResp = (SubmitSMResp) transmitterSession.send(submitSM);
      assertEquals(Data.ESME_ROK, submitResp.getCommandStatus());
      log.info("Message sent successfully");

      // Wait for the message to be received
      boolean received = messageLatch.await(10, TimeUnit.SECONDS);
      assertTrue("Message should be received", received);
      assertTrue("Message content should be verified", messageReceived.get());
      assertEquals("Message content should match", TEST_MESSAGE, receivedMessageContent.get());

      // Unbind sessions
      UnbindResp transmitterUnbindResp = transmitterSession.unbind();
      assertEquals(Data.ESME_ROK, transmitterUnbindResp.getCommandStatus());

      receiverThread.interrupt();
      UnbindResp receiverUnbindResp = receiverSession.unbind();
      assertEquals(Data.ESME_ROK, receiverUnbindResp.getCommandStatus());

    } finally {
      transmitterSession.close();
      receiverSession.close();
    }
  }

  @Test
  public void testMessageWithUnicodeContent() throws Exception {
    // Create transmitter session
    TCPIPConnection connection = new TCPIPConnection("localhost", PORT);
    connection.setReceiveTimeout(5000);
    Session session = new Session(connection);

    try {
      // Bind transmitter
      BindRequest bindRequest = new BindTransmitter();
      bindRequest.setSystemId(TEST_SYSTEM_ID);
      bindRequest.setPassword(TEST_PASSWORD);
      bindRequest.setSystemType("");
      bindRequest.setInterfaceVersion((byte) 0x34);
      bindRequest.setAddressRange(new AddressRange());

      BindResponse bindResponse = session.bind(bindRequest);
      assertEquals(Data.ESME_ROK, bindResponse.getCommandStatus());

      // Send a message with Unicode content
      String unicodeMessage = "Hello SMPP! Привет! 你好!";

      SubmitSM submitSM = new SubmitSM();
      submitSM.setSourceAddr(new Address((byte) 1, (byte) 1, SOURCE_ADDR));
      submitSM.setDestAddr(new Address((byte) 1, (byte) 1, DESTINATION_ADDR));

      // Set data coding to UCS2 (Unicode)
      submitSM.setDataCoding((byte) 0x08);

      // Convert the message to UCS2 bytes
      ByteBuffer bb = new ByteBuffer();
      for (int i = 0; i < unicodeMessage.length(); i++) {
        char c = unicodeMessage.charAt(i);
        bb.appendByte((byte) (c >> 8)); // High byte
        bb.appendByte((byte) (c & 0xff)); // Low byte
      }
      submitSM.setShortMessage(bb.getBuffer().toString());

      SubmitSMResp submitResp = (SubmitSMResp) session.send(submitSM);
      assertEquals(Data.ESME_ROK, submitResp.getCommandStatus());
      log.info("Unicode message sent successfully");

      // Unbind and close session
      UnbindResp unbindResp = session.unbind();
      assertEquals(Data.ESME_ROK, unbindResp.getCommandStatus());
    } finally {
      session.close();
    }
  }

  @Test
  public void testStopAndRestartSimulator() throws Exception {
    // Test that we can stop and restart the simulator

    // First stop it
    smppSim.stop();

    // Try to connect - should fail
    TCPIPConnection connection = new TCPIPConnection("localhost", PORT);
    connection.setReceiveTimeout(2000);
    Session session = new Session(connection);

    boolean connectionFailed = false;
    try {
      connection.open();
    } catch (IOException e) {
      connectionFailed = true;
    } finally {
      try {
        session.close();
      } catch (Exception e) {
        // Ignore
      }
    }

    assertTrue("Connection should fail when simulator is stopped", connectionFailed);

    // Restart the simulator
    smppSim.start();

    // Give it a moment to initialize
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Try to connect again - should succeed
    connection = new TCPIPConnection("localhost", PORT);
    connection.setReceiveTimeout(5000);
    session = new Session(connection);

    try {
      // Create transmitter bind request
      BindRequest bindRequest = new BindTransmitter();
      bindRequest.setSystemId(TEST_SYSTEM_ID);
      bindRequest.setPassword(TEST_PASSWORD);
      bindRequest.setSystemType("");
      bindRequest.setInterfaceVersion((byte) 0x34);
      bindRequest.setAddressRange(new AddressRange());

      // Send bind request
      BindResponse bindResponse = session.bind(bindRequest);

      // Verify bind success
      assertEquals(Data.ESME_ROK, bindResponse.getCommandStatus());

      // Unbind and close session
      UnbindResp unbindResp = session.unbind();
      assertEquals(Data.ESME_ROK, unbindResp.getCommandStatus());
    } finally {
      session.close();
    }
  }
}
