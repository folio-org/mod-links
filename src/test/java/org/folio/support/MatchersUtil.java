package org.folio.support;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.folio.entlinks.domain.entity.Authority;
import org.folio.entlinks.domain.entity.AuthoritySourceFile;
import org.mockito.ArgumentMatcher;

@UtilityClass
public class MatchersUtil {
  public static ArgumentMatcher<Authority> authorityMatch(Authority expected) {
    return actual -> {
      if (actual == null || expected == null) {
        return actual == expected;
      }
      return actual.getId().equals(expected.getId())
             && actual.getHeading().equals(expected.getHeading())
             && actual.getHeadingType().equals(expected.getHeadingType())
             && actual.getSource().equals(expected.getSource())
             && actual.getNaturalId().equals(expected.getNaturalId())
             && actual.getVersion() == actual.getVersion()
             && actual.getSaftHeadings().equals(expected.getSaftHeadings())
             && actual.getSftHeadings().equals(expected.getSftHeadings())
             && actual.getNotes().equals(expected.getNotes())
             && actual.getIdentifiers().equals(expected.getIdentifiers())
             && actual.getAuthoritySourceFile().equals(expected.getAuthoritySourceFile());
    };
  }

  public static ArgumentMatcher<AuthoritySourceFile> authoritySourceFileMatch(AuthoritySourceFile expected) {
    return actual -> {
      if (actual == null || expected == null) {
        return actual == expected;
      }
      return actual.getName().equals(expected.getName())
            && actual.getType().equals(expected.getType())
            && actual.getBaseUrlProtocol().equals(expected.getBaseUrlProtocol())
            && actual.getBaseUrl().equals(expected.getBaseUrl())
            && actual.getAuthoritySourceFileCodes().equals(expected.getAuthoritySourceFileCodes())
            && actual.isSelectable() == expected.isSelectable()
            && Objects.equals(actual.getHridStartNumber(), expected.getHridStartNumber())
            && actual.getSource().equals(expected.getSource())
            && actual.getSequenceName().equals(expected.getSequenceName());
    };
  }
}
