package org.folio.entlinks.service.messaging.authority.handler;

import java.util.List;
import org.folio.qm.domain.dto.InventoryEvent;
import org.folio.qm.domain.dto.LinksEvent;

public interface AuthorityChangeHandler {

  List<LinksEvent> handle(List<InventoryEvent> events);
}
