package com.github.malyshevhen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.malyshevhen.proxy.PDUHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
class SmppSimTest {

  private static final String TEST_SYSTEM_ID = "boromir";
  private static final String TEST_PASSWORD = "dfsew";
  private static final String SOURCE_ADDR = "1234";
  private static final String DESTINATION_ADDR = "5678";
  private static final String TEST_MESSAGE = "Hello, SMPP world!";

  private static int PORT;
  private static SmppSim smppSim;
  private static String tempUsersFile;

  @BeforeAll
  static void setUp() throws IOException {
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
    PORT = getFreePort();
    PDUHandler pduHeader = new PDUHandler();
    smppSim = new SmppSim(pduHeader, PORT, tempUsersFile);
    smppSim.start();

    // Give it a moment to initialize
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @AfterAll
  static void tearDown() throws IOException {
    // Stop the SMPP simulator
    if (smppSim != null) {
      smppSim.stop();
    }

    // Clean up temporary file
    Files.deleteIfExists(Paths.get(tempUsersFile));
  }

  @Test
  void testBindTransmitter() throws Exception {
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
  void testBindReceiver() throws Exception {
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
  void testSendMessage() throws Exception {
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

      String unicodeMessage = TEST_MESSAGE;

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
      submitSM.setShortMessage(Arrays.toString(bb.getBuffer()));

      SubmitSMResp submitResp = session.submit(submitSM);
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
  void testStopAndRestartSimulator() throws Exception {
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

    assertTrue(connectionFailed);

    // Restart the simulator
    smppSim.start();

    // Give it a moment to initialize
    try {
      Thread.sleep(5);
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

  @Test
  void testBindWithInvalidCredentials() throws Exception {
    TCPIPConnection connection = new TCPIPConnection("localhost", PORT);
    connection.setReceiveTimeout(5000);
    Session session = new Session(connection);
    try {
      BindRequest bindRequest = new BindTransmitter();
      bindRequest.setSystemId(TEST_SYSTEM_ID);
      bindRequest.setPassword("wrong");
      bindRequest.setSystemType("");
      bindRequest.setInterfaceVersion((byte) 0x34);
      bindRequest.setAddressRange(new AddressRange());
      BindResponse bindResponse = session.bind(bindRequest);
      // Should fail
      assertTrue(bindResponse.getCommandStatus() != Data.ESME_ROK);
    } finally {
      session.close();
    }
  }

  // Skip this test for now
  @Test
  @Disabled
  void testSendAndReceiveMessage() throws Exception {
    // Receiver session
    TCPIPConnection receiverConnection = new TCPIPConnection("localhost", PORT);
    receiverConnection.setReceiveTimeout(5000);
    Session receiverSession = new Session(receiverConnection);
    // Transmitter session
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
      // Bind transmitter
      BindRequest transmitterBindRequest = new BindTransmitter();
      transmitterBindRequest.setSystemId(TEST_SYSTEM_ID);
      transmitterBindRequest.setPassword(TEST_PASSWORD);
      transmitterBindRequest.setSystemType("");
      transmitterBindRequest.setInterfaceVersion((byte) 0x34);
      transmitterBindRequest.setAddressRange(new AddressRange());
      BindResponse transmitterBindResponse = transmitterSession.bind(transmitterBindRequest);
      assertEquals(Data.ESME_ROK, transmitterBindResponse.getCommandStatus());
      // Send message
      String message = "End-to-end test message";
      SubmitSM submitSM = new SubmitSM();
      submitSM.setSourceAddr(new Address((byte) 1, (byte) 1, SOURCE_ADDR));
      submitSM.setDestAddr(new Address((byte) 1, (byte) 1, DESTINATION_ADDR));
      submitSM.setShortMessage(message);
      SubmitSMResp submitResp = transmitterSession.submit(submitSM);
      assertEquals(Data.ESME_ROK, submitResp.getCommandStatus());
      // Try to receive
      PDU pdu = receiverSession.receive(5000);
      assertNotNull(pdu);
      assertTrue(pdu instanceof DeliverSM);
      DeliverSM deliverSM = (DeliverSM) pdu;
      String received = new String(deliverSM.getShortMessage());
      assertEquals(message, received);
      // Unbind
      transmitterSession.unbind();
      receiverSession.unbind();
    } finally {
      transmitterSession.close();
      receiverSession.close();
    }
  }

  @Test
  void testSendLongMessage() throws Exception {
    TCPIPConnection connection = new TCPIPConnection("localhost", PORT);
    connection.setReceiveTimeout(5000);
    Session session = new Session(connection);
    try {
      BindRequest bindRequest = new BindTransmitter();
      bindRequest.setSystemId(TEST_SYSTEM_ID);
      bindRequest.setPassword(TEST_PASSWORD);
      bindRequest.setSystemType("");
      bindRequest.setInterfaceVersion((byte) 0x34);
      bindRequest.setAddressRange(new AddressRange());
      BindResponse bindResponse = session.bind(bindRequest);
      assertEquals(Data.ESME_ROK, bindResponse.getCommandStatus());
      String longMessage = "A".repeat(200);
      SubmitSM submitSM = new SubmitSM();
      submitSM.setSourceAddr(new Address((byte) 1, (byte) 1, SOURCE_ADDR));
      submitSM.setDestAddr(new Address((byte) 1, (byte) 1, DESTINATION_ADDR));
      submitSM.setShortMessage(longMessage);
      SubmitSMResp submitResp = session.submit(submitSM);
      assertEquals(Data.ESME_ROK, submitResp.getCommandStatus());
      session.unbind();
    } finally {
      session.close();
    }
  }

  @Test
  void testSendSpecialCharacters() throws Exception {
    TCPIPConnection connection = new TCPIPConnection("localhost", PORT);
    connection.setReceiveTimeout(5000);
    Session session = new Session(connection);
    try {
      BindRequest bindRequest = new BindTransmitter();
      bindRequest.setSystemId(TEST_SYSTEM_ID);
      bindRequest.setPassword(TEST_PASSWORD);
      bindRequest.setSystemType("");
      bindRequest.setInterfaceVersion((byte) 0x34);
      bindRequest.setAddressRange(new AddressRange());
      BindResponse bindResponse = session.bind(bindRequest);
      assertEquals(Data.ESME_ROK, bindResponse.getCommandStatus());
      String specialMessage = "!@#$%^&*()_+-=~`[]{}|;':,./<>?";
      SubmitSM submitSM = new SubmitSM();
      submitSM.setSourceAddr(new Address((byte) 1, (byte) 1, SOURCE_ADDR));
      submitSM.setDestAddr(new Address((byte) 1, (byte) 1, DESTINATION_ADDR));
      submitSM.setShortMessage(specialMessage);
      SubmitSMResp submitResp = session.submit(submitSM);
      assertEquals(Data.ESME_ROK, submitResp.getCommandStatus());
      session.unbind();
    } finally {
      session.close();
    }
  }

  private static int getFreePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }
}
