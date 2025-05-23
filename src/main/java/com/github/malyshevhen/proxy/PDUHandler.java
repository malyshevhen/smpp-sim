package com.github.malyshevhen.proxy;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.smpp.pdu.BindTransmitterResp;
import org.smpp.pdu.PDU;
import org.smpp.pdu.SubmitMultiSMResp;
import org.smpp.pdu.SubmitSMResp;

@Slf4j
public class PDUHandler {
  private final List<BindTransmitterResp> bindTransmitterResponses = new ArrayList<>();
  private final List<SubmitSMResp> submitSMResponses = new ArrayList<>();
  private final List<SubmitMultiSMResp> submitMultiSMResponses = new ArrayList<>();

  public synchronized List<BindTransmitterResp> getBindResponses() {
    return new ArrayList<>(bindTransmitterResponses);
  }

  public synchronized List<SubmitSMResp> getSubmitSMResponses() {
    return new ArrayList<>(submitSMResponses);
  }

  public synchronized List<SubmitMultiSMResp> getSubmitMultiSMResponses() {
    return new ArrayList<>(submitMultiSMResponses);
  }

  public synchronized void handleClientRequest(PDU pdu) {
    logPDU(pdu);

    switch (pdu) {
      case BindTransmitterResp request -> bindTransmitterResponses.add(request);
      case SubmitSMResp response -> submitSMResponses.add(response);
      case SubmitMultiSMResp response -> submitMultiSMResponses.add(response);
      default -> log.warn("Unknown PDU type: {}", pdu.getClass().getSimpleName());
    }
  }

  private void logPDU(PDU pdu) {
    log.info("Received PDU:");
    log.info("PDU type: {}", pdu.getClass().getSimpleName());
    log.info("PDU command ID: {}", pdu.getCommandId());
    log.info("PDU sequence number: {}", pdu.getSequenceNumber());
    log.info("PDU body: {}", pdu.debugString());
  }
}
