package org.folio.entlinks.service.consortium.propagation;

import java.util.function.BiConsumer;

public interface PropagationEventPublisherI<T> {
  default void setUpdatePublishConsumer(BiConsumer<T, T> consumer) {}
}
