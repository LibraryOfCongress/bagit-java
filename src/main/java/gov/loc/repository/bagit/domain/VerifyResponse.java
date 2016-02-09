package gov.loc.repository.bagit.domain;

import java.util.ArrayList;
import java.util.List;

public class VerifyResponse {
  private boolean errored = false;
  private List<String> errorMessages = new ArrayList<>();
  
  public boolean hasError() {
    return errored;
  }
  public void setErrored(boolean hasError) {
    this.errored = hasError;
  }
  public List<String> getErrorMessages() {
    return errorMessages;
  }
  public void setErrorMessages(List<String> messages) {
    this.errorMessages = messages;
  }
}
