package com.github.malyshevhen.api;

import java.util.ArrayList;
import java.util.List;
import org.smpp.pdu.SubmitMultiSM;
import org.smpp.pdu.SubmitMultiSMResp;

public record MultiShortMessageInfo(
    Long id, String message, String sourceAddress, List<String> destinationAddresses) {
  public MultiShortMessageInfo {
    if (id == null || id < 0) {
      throw new IllegalArgumentException("ID must be a positive number");
    }

    if (message == null || message.isEmpty()) {
      throw new IllegalArgumentException("Message cannot be null or empty");
    }

    if (sourceAddress == null || sourceAddress.isEmpty()) {
      throw new IllegalArgumentException("Source address cannot be null or empty");
    }

    if (destinationAddresses == null || destinationAddresses.isEmpty()) {
      throw new IllegalArgumentException("Destination addresses cannot be null or empty");
    }
  }

  public static MultiShortMessageInfo from(SubmitMultiSMResp response) {
    SubmitMultiSM request = (SubmitMultiSM) response.getOriginalRequest();

    Long id = Long.valueOf(request.getSequenceNumber());
    String message = request.getShortMessage();
    String sourceAddress = request.getSourceAddr().getAddress();
    int count = request.getNumberOfDests();

    List<String> destinationAddresses = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      String address = request.getDestAddress(i).getAddress().getAddress();
      destinationAddresses.add(address);
    }

    return new MultiShortMessageInfo(id, message, sourceAddress, destinationAddresses);
  }
}
