package org.folio.entlinks.service.authority;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class AuthoritiesBulkContext {

  private final String initialFilePath;
  private final String initialFileLocation;
  private final String failedEntitiesFilePath;
  private final String errorsFilePath;
  private final File localFailedEntitiesFile;
  private final File localErrorsFile;

  public AuthoritiesBulkContext(String initialFilePath) throws IOException {
    this.initialFilePath = StringUtils.removeStart(initialFilePath, "/");
    var inititalPath = Paths.get(initialFilePath);
    if (inititalPath.getParent() != null) {
      this.initialFileLocation = inititalPath.getParent().toString();
    } else {
      this.initialFileLocation = "";
    }
    this.failedEntitiesFilePath = this.initialFilePath + "_failedEntities";
    this.errorsFilePath = this.initialFilePath + "_errors";
    this.localFailedEntitiesFile = initLocalFailedEntitiesFile();
    this.localErrorsFile = initLocalErrorsFile();
  }

  public String getFailedEntitiesFileName() {
    return StringUtils.removeStart(failedEntitiesFilePath, initialFilePath);
  }

  public String getErrorsFileName() {
    return StringUtils.removeStart(errorsFilePath, initialFilePath);
  }

  public String getLocalFailedEntitiesFilePath() {
    return "temp/" + failedEntitiesFilePath;
  }

  public String getLocalErrorsFilePath() {
    return "temp/" + errorsFilePath;
  }

  public void deleteLocalFiles() {
    localErrorsFile.delete();
    localFailedEntitiesFile.delete();
  }

  private File initLocalFailedEntitiesFile() throws IOException {
    Path path = Paths.get(getLocalFailedEntitiesFilePath());
    Files.createDirectories(path.getParent());
    return path.toFile();
  }

  private File initLocalErrorsFile() throws IOException {
    Path path = Paths.get(getLocalErrorsFilePath());
    Files.createDirectories(path.getParent());
    return path.toFile();
  }
}
