package com.github.malyshevhen.api;

import org.smpp.pdu.SubmitSM;
import org.smpp.pdu.SubmitSMResp;

/**
 * Represents a short message information. This class is used to store and validate the information
 * of a short message. It includes the message ID, message content, sender's name, source address,
 * and destination address.
 */
public record ShortMessageInfo(
    Long id, String message, String sourceAddress, String destinationAddress) {
  public ShortMessageInfo {
    if (id == null || id < 0) {
      throw new IllegalArgumentException("ID must be a positive number");
    }

    if (message == null || message.isEmpty()) {
      throw new IllegalArgumentException("Message cannot be null or empty");
    }

    if (sourceAddress == null || sourceAddress.isEmpty()) {
      throw new IllegalArgumentException("Source address cannot be null or empty");
    }

    if (destinationAddress == null || destinationAddress.isEmpty()) {
      throw new IllegalArgumentException("Destination address cannot be null or empty");
    }
  }

  public static ShortMessageInfo from(SubmitSMResp response) {
    SubmitSM request = (SubmitSM) response.getOriginalRequest();

    long id = request.getSequenceNumber();
    String message = request.getShortMessage();
    String sourceAddress = request.getSourceAddr().getAddress();
    String destinationAddress = request.getDestAddr().getAddress();

    return new ShortMessageInfo(id, message, sourceAddress, destinationAddress);
  }
}
