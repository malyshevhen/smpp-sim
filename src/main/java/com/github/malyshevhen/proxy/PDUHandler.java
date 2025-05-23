package com.github.malyshevhen.proxy;

import java.util.List;
import org.smpp.pdu.BindTransmitterResp;
import org.smpp.pdu.PDU;
import org.smpp.pdu.SubmitMultiSMResp;
import org.smpp.pdu.SubmitSMResp;

public interface PDUHandler {

  List<BindTransmitterResp> getBindResponses();

  List<SubmitSMResp> getSubmitSMResponses();

  List<SubmitMultiSMResp> getSubmitMultiSMResponses();

  void handleClientRequest(PDU pdu);
}
