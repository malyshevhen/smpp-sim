package com.github.malyshevhen.api;

import java.util.ArrayList;
import java.util.List;
import org.smpp.pdu.SubmitMultiSM;

public record MultiShortMessageInfo(
    Long id, String message, String name, String sourceAddress, List<String> destinationAddresses) {
  public MultiShortMessageInfo {
    if (id == null || id < 0) {
      throw new IllegalArgumentException("ID must be a positive number");
    }

    if (message == null || message.isEmpty()) {
      throw new IllegalArgumentException("Message cannot be null or empty");
    }

    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }

    if (sourceAddress == null || sourceAddress.isEmpty()) {
      throw new IllegalArgumentException("Source address cannot be null or empty");
    }

    if (destinationAddresses == null || destinationAddresses.isEmpty()) {
      throw new IllegalArgumentException("Destination addresses cannot be null or empty");
    }
  }

  public static MultiShortMessageInfo from(SubmitMultiSM submitMultiSM) {
    Long id = Long.valueOf(submitMultiSM.getSequenceNumber());
    String message = submitMultiSM.getShortMessage();
    String name = submitMultiSM.getSourceAddr().debugString(); // TODO: use real name
    String sourceAddress = submitMultiSM.getSourceAddr().getAddress();
    int count = submitMultiSM.getNumberOfDests();

    List<String> destinationAddresses = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      String address = submitMultiSM.getDestAddress(i).getAddress().getAddress();
      destinationAddresses.add(address);
    }

    return new MultiShortMessageInfo(id, message, name, sourceAddress, destinationAddresses);
  }
}
