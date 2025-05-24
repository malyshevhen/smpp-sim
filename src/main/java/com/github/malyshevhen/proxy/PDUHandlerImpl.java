package com.github.malyshevhen.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;
import org.smpp.pdu.BindRequest;
import org.smpp.pdu.BindTranscieverResp;
import org.smpp.pdu.BindTransmitterResp;
import org.smpp.pdu.PDU;
import org.smpp.pdu.SubmitMultiSMResp;
import org.smpp.pdu.SubmitSMResp;

@Slf4j
public class PDUHandlerImpl implements PDUHandler {
  private final List<BindRequest> bindTransmitterResponses = new CopyOnWriteArrayList<>();
  private final List<SubmitSMResp> submitSMResponses = new CopyOnWriteArrayList<>();
  private final List<SubmitMultiSMResp> submitMultiSMResponses = new CopyOnWriteArrayList<>();

  @Override
  public List<BindRequest> getBindResponses() {
    return new ArrayList<>(bindTransmitterResponses);
  }

  @Override
  public List<SubmitSMResp> getSubmitSMResponses() {
    return new ArrayList<>(submitSMResponses);
  }

  @Override
  public List<SubmitMultiSMResp> getSubmitMultiSMResponses() {
    return new ArrayList<>(submitMultiSMResponses);
  }

  @Override
  public void handleClientRequest(PDU pdu) {
    logPDU(pdu);

    switch (pdu) {
      case BindTransmitterResp request ->
          bindTransmitterResponses.add((BindRequest) request.getOriginalRequest());
      case BindTranscieverResp response ->
          bindTransmitterResponses.add((BindRequest) response.getOriginalRequest());
      case BindRequest request -> bindTransmitterResponses.add(request);
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
