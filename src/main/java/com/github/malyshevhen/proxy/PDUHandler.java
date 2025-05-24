package com.github.malyshevhen.proxy;

import java.util.List;

import org.smpp.pdu.BindRequest;
import org.smpp.pdu.PDU;
import org.smpp.pdu.SubmitMultiSMResp;
import org.smpp.pdu.SubmitSMResp;

public interface PDUHandler {

  List<BindRequest> getBindResponses();

  List<SubmitSMResp> getSubmitSMResponses();

  List<SubmitMultiSMResp> getSubmitMultiSMResponses();

  void handleClientRequest(PDU pdu);
}
