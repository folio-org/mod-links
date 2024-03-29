package org.folio.entlinks.service.consortium.propagation.model;

import java.util.function.BiConsumer;
import org.folio.entlinks.domain.entity.AuthoritySourceFile;

public record AuthoritySourceFilePropagationData<T>(AuthoritySourceFile authoritySourceFile,
                                                    BiConsumer<T, T> publishConsumer) {
}
