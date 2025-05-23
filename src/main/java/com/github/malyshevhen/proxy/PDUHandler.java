package com.github.malyshevhen.proxy;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.PDU;
import org.smpp.pdu.SubmitMultiSM;
import org.smpp.pdu.SubmitMultiSMResp;
import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;

@Slf4j
public class PDUHandler {
  private final List<BindRequest> bindRequests = new ArrayList<>();
  private final List<SubmitSM> submitSMs = new ArrayList<>();
  private final List<SubmitSMResp> submitSMResponses = new ArrayList<>();
  private final List<SubmitMultiSM> submitMultiSMs = new ArrayList<>();
  private final List<SubmitMultiSMResp> submitMultiSMResponses = new ArrayList<>();

  public synchronized List<BindRequest> getBindRequests() {
    return new ArrayList<>(bindRequests);
  }

  public synchronized List<SubmitSM> getSubmitSMs() {
    return new ArrayList<>(submitSMs);
  }

  public synchronized List<SubmitSMResp> getSubmitSMResponses() {
    return new ArrayList<>(submitSMResponses);
  }

  public synchronized List<SubmitMultiSM> getSubmitMultiSMs() {
    return new ArrayList<>(submitMultiSMs);
  }

  public synchronized List<SubmitMultiSMResp> getSubmitMultiSMResponses() {
    return new ArrayList<>(submitMultiSMResponses);
  }

  public synchronized void handleClientRequest(PDU pdu) {
    logPDU(pdu);

    switch (pdu) {
      case BindRequest request -> bindRequests.add(request);
      case SubmitSM request -> submitSMs.add(request);
      case SubmitSMResp response -> submitSMResponses.add(response);
      case SubmitMultiSM request -> submitMultiSMs.add(request);
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
