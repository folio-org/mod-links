package org.folio.entlinks.service.messaging.authority.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.folio.entlinks.domain.dto.InventoryEvent;
import org.jetbrains.annotations.NotNull;

@Value
public class AuthorityChangeHolder {

  @Getter(value = AccessLevel.PRIVATE)
  @NotNull InventoryEvent event;
  @Getter(value = AccessLevel.PRIVATE)
  @NotNull List<AuthorityChange> changes;

  boolean isNaturalIdChanged;
  boolean isOnlyNaturalIdChanged;
  AuthorityChange fieldChange;

  public AuthorityChangeHolder(@NotNull InventoryEvent event,
                               @NotNull List<AuthorityChange> changes) {
    this.event = event;
    this.changes = changes;
    this.isNaturalIdChanged = changes.contains(AuthorityChange.NATURAL_ID);
    this.isOnlyNaturalIdChanged = isOnlyNaturalIdChanged(changes);
    this.fieldChange = getFieldChange(changes, isOnlyNaturalIdChanged);
  }

  private AuthorityChange getFieldChange(List<AuthorityChange> changes, boolean isOnlyNaturalIdChanged) {
    if (isOnlyNaturalIdChanged) {
      return null;
    } else {
      var authorityChanges = new ArrayList<>(changes);
      authorityChanges.remove(AuthorityChange.NATURAL_ID);
      return authorityChanges.get(0);
    }
  }

  private boolean isOnlyNaturalIdChanged(@NotNull List<AuthorityChange> changes) {
    return isNaturalIdChanged && changes.size() == 1;
  }

  public UUID getAuthorityId() {
    return event.getId();
  }

  public String getNewNaturalId() {
    return event.getNew().getNaturalId();
  }

  public UUID getNewSourceFileId() {
    return event.getNew().getSourceFileId();
  }
}
