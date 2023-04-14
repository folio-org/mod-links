package org.folio.entlinks.service.links;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.folio.entlinks.config.constants.CacheNames.AUTHORITY_LINKING_RULES_CACHE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.folio.entlinks.domain.dto.SubfieldModification;
import org.folio.entlinks.domain.entity.InstanceAuthorityLinkingRule;
import org.folio.entlinks.domain.repository.LinkingRulesRepository;
import org.folio.entlinks.exception.LinkingRuleNotFoundException;
import org.folio.entlinks.service.CachingService;
import org.folio.spring.test.type.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@UnitTest
@ExtendWith(MockitoExtension.class)
class InstanceAuthorityLinkingRulesServiceTest {

  private @Mock LinkingRulesRepository repository;
  private @Mock CachingService cachingService;

  private @InjectMocks InstanceAuthorityLinkingRulesService service;

  @Test
  void getLinkingRules_positive() {
    var rule = InstanceAuthorityLinkingRule.builder()
      .id(1)
      .bibField("100")
      .authorityField("100")
      .authoritySubfields(new char[] {'a', 'b'})
      .subfieldsExistenceValidations(Map.of("a", true))
      .subfieldModifications(List.of(new SubfieldModification().target("a").source("b")))
      .build();

    when(repository.findAll(any(Sort.class))).thenReturn(List.of(rule));

    var actual = service.getLinkingRules();

    assertThat(actual)
      .containsExactlyInAnyOrder(rule);
  }

  @Test
  void getLinkingRulesByAuthorityField_positive() {
    var authorityField = "101";
    var rule = InstanceAuthorityLinkingRule.builder()
      .id(1)
      .bibField("100")
      .authorityField(authorityField)
      .authoritySubfields(new char[] {'a', 'b'})
      .subfieldsExistenceValidations(Map.of("a", true))
      .subfieldModifications(List.of(new SubfieldModification().target("a").source("b")))
      .build();

    when(repository.findByAuthorityField(authorityField)).thenReturn(List.of(rule));

    var actual = service.getLinkingRulesByAuthorityField(authorityField);

    assertThat(actual)
      .containsExactlyInAnyOrder(rule);
  }

  @Test
  void getLinkingRule_positive() {
    var ruleId = 1;
    var rule = InstanceAuthorityLinkingRule.builder()
      .id(ruleId)
      .bibField("100")
      .authorityField("101")
      .authoritySubfields(new char[] {'a', 'b'})
      .subfieldsExistenceValidations(Map.of("a", true))
      .subfieldModifications(List.of(new SubfieldModification().target("a").source("b")))
      .build();

    when(repository.findById(ruleId)).thenReturn(Optional.of(rule));

    var actual = service.getLinkingRule(ruleId);

    assertThat(actual).isEqualTo(rule);
  }

  @Test
  void getLinkingRule_negative_notFound() {
    var ruleId = 1;

    when(repository.findById(ruleId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getLinkingRule(ruleId))
      .isInstanceOf(LinkingRuleNotFoundException.class)
      .hasMessage(String.format("Linking rule with ID [%s] was not found", ruleId));
  }

  @Test
  void patchLinkingRule_positive_only() {
    var ruleId = 1;
    var existedRule = InstanceAuthorityLinkingRule.builder()
      .id(ruleId)
      .bibField("100")
      .authorityField("101")
      .authoritySubfields(new char[] {'a', 'b'})
      .subfieldsExistenceValidations(Map.of("a", true))
      .subfieldModifications(List.of(new SubfieldModification().target("a").source("b")))
      .autoLinkingEnabled(false)
      .build();

    when(repository.findById(ruleId)).thenReturn(Optional.of(existedRule));

    var linkingRulePatch = new InstanceAuthorityLinkingRule();
    linkingRulePatch.setBibField("123");
    linkingRulePatch.setAuthorityField("321");
    linkingRulePatch.setAuthoritySubfields(new char[0]);
    linkingRulePatch.setSubfieldModifications(Collections.emptyList());
    linkingRulePatch.setSubfieldsExistenceValidations(Collections.emptyMap());
    linkingRulePatch.setAutoLinkingEnabled(true);
    service.patchLinkingRule(ruleId, linkingRulePatch);

    var ruleUpdateCaptor = ArgumentCaptor.forClass(InstanceAuthorityLinkingRule.class);
    verify(cachingService).invalidateCache(AUTHORITY_LINKING_RULES_CACHE);
    verify(repository).save(ruleUpdateCaptor.capture());

    assertThat(ruleUpdateCaptor.getValue())
      .extracting(InstanceAuthorityLinkingRule::getId,
        InstanceAuthorityLinkingRule::getBibField,
        InstanceAuthorityLinkingRule::getAuthorityField,
        InstanceAuthorityLinkingRule::getAuthoritySubfields,
        InstanceAuthorityLinkingRule::getSubfieldModifications,
        InstanceAuthorityLinkingRule::getSubfieldsExistenceValidations,
        InstanceAuthorityLinkingRule::getAutoLinkingEnabled)
      .containsExactly(existedRule.getId(),
        existedRule.getBibField(),
        existedRule.getAuthorityField(),
        existedRule.getAuthoritySubfields(),
        existedRule.getSubfieldModifications(),
        existedRule.getSubfieldsExistenceValidations(),
        linkingRulePatch.getAutoLinkingEnabled());
  }

  @Test
  void patchLinkingRule_negative_notFound() {
    var ruleId = 1;

    when(repository.findById(ruleId)).thenReturn(Optional.empty());

    var linkingRulePatch = new InstanceAuthorityLinkingRule();
    assertThatThrownBy(() -> service.patchLinkingRule(ruleId, linkingRulePatch))
      .isInstanceOf(LinkingRuleNotFoundException.class)
      .hasMessage(String.format("Linking rule with ID [%s] was not found", ruleId));
  }
}
