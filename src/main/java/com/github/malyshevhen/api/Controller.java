package com.github.malyshevhen.api;

import com.github.malyshevhen.proxy.PDUHandler;
import java.util.List;

public class Controller {

  private final PDUHandler pduHandler;

  public Controller(PDUHandler pduHandler) {
    this.pduHandler = pduHandler;
  }

  public List<ShortMessageInfo> getSubmitSMs() {
    return pduHandler.getSubmitSMResponses().stream().map(ShortMessageInfo::from).toList();
  }

  public List<MultiShortMessageInfo> getSubmitMultiSMs() {
    return pduHandler.getSubmitMultiSMResponses().stream()
        .map(MultiShortMessageInfo::from)
        .toList();
  }

  public List<BindRequestInfo> getBindRequests() {
    return pduHandler.getBindResponses().stream().map(BindRequestInfo::from).toList();
  }

  public void clean() {
    pduHandler.clean();
  }
}
