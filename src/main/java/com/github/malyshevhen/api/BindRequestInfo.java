package com.github.malyshevhen.api;

import org.smpp.pdu.BindRequest;

/**
 * Represents a bind request. This class is used to store and validate the information of a bind
 * request. It includes the ID, system ID, password, CP, name, ton, and npi.
 */
public record BindRequestInfo(
    Long id, String systemId, String password, String name, int ton, int npi) {
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

    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null or empty");
    }
  }

  public static BindRequestInfo from(BindRequest bindRequest) {
    long id = bindRequest.getSequenceNumber();
    String systemId = bindRequest.getSystemId();
    String password = bindRequest.getPassword();
    String name = bindRequest.getSystemId();
    int ton = bindRequest.getAddressRange().getTon();
    int npi = bindRequest.getAddressRange().getNpi();

    return new BindRequestInfo(id, systemId, password, name, ton, npi);
  }
}
