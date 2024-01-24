package org.folio.support.base;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.MinIOContainer;

public class MinioContainerExtension implements BeforeAllCallback, AfterAllCallback {
  private static final String URL_PROPERTY_NAME = "folio.remote-storage.endpoint";
  private static final String REGION_PROPERTY_NAME = "folio.remote-storage.region";
  private static final String BUCKET_PROPERTY_NAME = "folio.remote-storage.bucket";
  private static final String ACCESS_KEY_PROPERTY_NAME = "folio.remote-storage.accessKey";
  private static final String SECRET_KEY_PROPERTY_NAME = "folio.remote-storage.secretKey";
  private static final String MINIO_IMAGE = "minio/minio:RELEASE.2024-01-18T22-51-28Z";
  private static final MinIOContainer CONTAINER = new MinIOContainer(MINIO_IMAGE);

  public void beforeAll(ExtensionContext context) {
    if (!CONTAINER.isRunning()) {
      CONTAINER.start();
    }

    System.setProperty(URL_PROPERTY_NAME, CONTAINER.getS3URL());
    System.setProperty(ACCESS_KEY_PROPERTY_NAME, CONTAINER.getUserName());
    System.setProperty(SECRET_KEY_PROPERTY_NAME, CONTAINER.getPassword());
    System.setProperty(REGION_PROPERTY_NAME, "region");
    System.setProperty(BUCKET_PROPERTY_NAME, "test-bucket");
  }

  public void afterAll(ExtensionContext context) {
    System.clearProperty(URL_PROPERTY_NAME);
  }
}
