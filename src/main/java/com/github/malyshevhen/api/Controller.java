package com.github.malyshevhen.api;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Controller {

  private static final ConcurrentMap<String, ShortMessageInfo> MESSAGES = new ConcurrentHashMap<>();
  private static final ConcurrentMap<String, BindRequest> BIND_REQUESTS = new ConcurrentHashMap<>();
}
