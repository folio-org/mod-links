package org.folio.entlinks.service.tenant;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.integration.kafka.EventProducer;
import org.folio.entlinks.service.links.InstanceAuthorityLinkingRulesService;
import org.folio.rspec.domain.dto.DefinitionType;
import org.folio.rspec.domain.dto.Family;
import org.folio.rspec.domain.dto.SubfieldUpdateRequestEvent;
import org.folio.rspec.domain.dto.UpdateRequestEvent;
import org.folio.spring.testing.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MarcSpecificationUpdateServiceTest {

  @Mock
  private InstanceAuthorityLinkingRulesService linkingRulesService;
  @Mock
  private EventProducer<UpdateRequestEvent> eventProducer;

  @InjectMocks
  private MarcSpecificationUpdateService service;

  @Test
  void sendSpecificationRequests_ShouldSendCorrectUpdateRequests() {
    // Arrange
    var rule1 = new InstanceAuthorityLinkingRule();
    rule1.setBibField("100");
    var rule2 = new InstanceAuthorityLinkingRule();
    rule2.setBibField("200");
    when(linkingRulesService.getLinkingRules()).thenReturn(List.of(rule1, rule2));

    // Act
    service.sendSpecificationRequests();

    // Assert
    verify(eventProducer).sendMessages(argThat(events -> {
      if (events.size() != 2) {
        return false;
      }
      boolean passed = true;
      for (UpdateRequestEvent event : events) {
        passed = event instanceof SubfieldUpdateRequestEvent requestEvent
                 && DefinitionType.SUBFIELD.equals(requestEvent.getDefinitionType())
                 && Family.MARC.equals(requestEvent.getFamily())
                 && ("100".equals(requestEvent.getTargetFieldTag()) || "200".equals(requestEvent.getTargetFieldTag()));
        if (!passed) {
          return false;
        }
      }
      return passed;
    }));
  }

  @Test
  void sendSpecificationRequests_ShouldFilterDuplicateRules() {
    // Arrange
    InstanceAuthorityLinkingRule rule1 = new InstanceAuthorityLinkingRule();
    rule1.setBibField("200");
    InstanceAuthorityLinkingRule rule2 = new InstanceAuthorityLinkingRule();
    rule2.setBibField("200");
    when(linkingRulesService.getLinkingRules()).thenReturn(List.of(rule1, rule2));

    // Act
    service.sendSpecificationRequests();

    // Assert
    verify(eventProducer).sendMessages(argThat(events -> events.size() == 1));
  }
}
