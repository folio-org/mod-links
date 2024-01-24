package org.folio.entlinks.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

public class EntitiesErrorFileWriter implements AutoCloseable {

  private final BufferedWriter errorEntitiesFileWriter;
  private final BufferedWriter errorsFileWriter;
  private final ObjectMapper objectMapper;

  public EntitiesErrorFileWriter(String errorEntitiesFileName, String errorsFileName, ObjectMapper objectMapper)
    throws IOException {
    errorEntitiesFileWriter = new BufferedWriter(new FileWriter(getFile(errorEntitiesFileName)));
    errorsFileWriter = new BufferedWriter(new FileWriter(getFile(errorsFileName)));
    this.objectMapper = objectMapper;
  }

  public <T> void write(T entity, Exception ex, Function<T, String> entityIdentifierGetter) throws IOException {
    errorEntitiesFileWriter.write(objectMapper.writeValueAsString(entity));
    errorEntitiesFileWriter.newLine();
    errorsFileWriter.write(entityIdentifierGetter.apply(entity) + ","
                           + ex.getMessage().replaceAll(System.lineSeparator(), " "));
    errorsFileWriter.newLine();
  }

  @Override
  public void close() throws Exception {
    errorEntitiesFileWriter.close();
    errorsFileWriter.close();
  }

  private File getFile(String errorEntitiesFileName) throws IOException {
    Path path = Paths.get(errorEntitiesFileName);
    Files.createDirectories(path.getParent());
    return path.toFile();
  }
}
