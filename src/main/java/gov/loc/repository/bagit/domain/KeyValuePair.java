package gov.loc.repository.bagit.domain;

import javafx.util.Pair;

public class KeyValuePair extends Pair<String, String> {
  private static final long serialVersionUID = 1L;

  public KeyValuePair(String key, String value) {
    super(key, value);
  }

}
