package com.github.malyshevhen.api;

import org.smpp.pdu.BindRequest;

/**
 * Represents a bind request. This class is used to store and validate the information of a bind
 * request. It includes the ID, system ID, password, CP, name, ton, and npi.
 */
public record BindRequestInfo(
    Long id, String systemId, String password, String cp, String name, int ton, int npi) {
  public BindRequestInfo {
    if (id == null || id < 0) {
      throw new IllegalArgumentException("ID must be a positive number");
    }
    if (systemId == null || systemId.isEmpty()) {
      throw new IllegalArgumentException("System ID cannot be null or empty");
    }
    if (password == null || password.isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }
    if (cp == null || cp.isEmpty()) {
      throw new IllegalArgumentException("CP cannot be null or empty");
    }
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
  }

  public static BindRequestInfo from(BindRequest bindRequest) {
    Long id = Long.valueOf(bindRequest.getSequenceNumber());
    String systemId = bindRequest.getSystemId();
    String password = bindRequest.getPassword();
    String cp = bindRequest.getSystemType();
    String name = bindRequest.getSystemId(); // TODO: use real name
    int ton = bindRequest.getAddressRange().getTon();
    int npi = bindRequest.getAddressRange().getNpi();

    return new BindRequestInfo(id, systemId, password, cp, name, ton, npi);
  }
}
