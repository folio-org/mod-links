package org.folio.support;

import static java.util.UUID.randomUUID;
import static org.folio.support.base.TestConstants.TENANT_ID;
import static org.springframework.util.ResourceUtils.getFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.folio.entlinks.client.UsersClient;
import org.folio.entlinks.domain.dto.AuthorityDataStatActionDto;
import org.folio.entlinks.domain.dto.AuthorityDataStatDto;
import org.folio.entlinks.domain.dto.AuthorityInventoryRecord;
import org.folio.entlinks.domain.dto.InstanceLinkDto;
import org.folio.entlinks.domain.dto.InstanceLinkDtoCollection;
import org.folio.entlinks.domain.dto.InventoryEvent;
import org.folio.entlinks.domain.dto.Metadata;
import org.folio.entlinks.domain.entity.AuthorityData;
import org.folio.entlinks.domain.entity.AuthorityDataStat;
import org.folio.entlinks.domain.entity.AuthorityDataStatAction;
import org.folio.entlinks.domain.entity.InstanceAuthorityLink;
import org.folio.spring.tools.model.ResultList;

public class TestUtils {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

  @SneakyThrows
  public static String asJson(Object value) {
    return OBJECT_MAPPER.writeValueAsString(value);
  }

  public static InventoryEvent inventoryEvent(String resource, String type,
    AuthorityInventoryRecord n, AuthorityInventoryRecord o) {
    return new InventoryEvent().type(type).resourceName(resource).tenant(TENANT_ID)._new(n).old(o);
  }

  public static InventoryEvent authorityEvent(String type, AuthorityInventoryRecord n, AuthorityInventoryRecord o) {
    return inventoryEvent("authority", type, n, o);
  }

  public static List<InstanceLinkDto> linksDto(UUID instanceId, Link... links) {
    return Arrays.stream(links).map(link -> link.toDto(instanceId)).toList();
  }

  public static InstanceLinkDtoCollection linksDtoCollection(List<InstanceLinkDto> links) {
    return new InstanceLinkDtoCollection().links(links).totalRecords(links.size());
  }

  public static List<InstanceAuthorityLink> links(UUID instanceId, Link... links) {
    return Arrays.stream(links).map(link -> link.toEntity(instanceId)).toList();
  }

  @SneakyThrows
  public static String readFile(String filePath) {
    return new String(Files.readAllBytes(getFile(filePath).toPath()));
  }

  public static List<AuthorityDataStat> dataStatList(UUID userId1, UUID userId2) {
    return List.of(
      AuthorityDataStat.builder()
        .id(randomUUID())
        .action(AuthorityDataStatAction.UPDATE_HEADING)
        .authorityData(AuthorityData.builder()
          .id(UUID.randomUUID())
          .deleted(false)
          .build())
        .authorityNaturalIdOld("naturalIdOld2")
        .authorityNaturalIdNew("naturalIdNew2")
        .authoritySourceFileNew(UUID.randomUUID())
        .authoritySourceFileOld(UUID.randomUUID())
        .headingNew("headingNew")
        .headingOld("headingOld")
        .headingTypeNew("headingTypeNew")
        .headingTypeOld("headingTypeOld")
        .lbUpdated(2)
        .lbFailed(1)
        .lbTotal(5)
        .startedByUserId(userId1)
        .build(),
      AuthorityDataStat.builder()
        .id(UUID.randomUUID())
        .action(AuthorityDataStatAction.UPDATE_HEADING)
        .authorityData(AuthorityData.builder()
          .id(UUID.randomUUID())
          .deleted(false)
          .build())
        .authorityNaturalIdOld("naturalIdOld2")
        .authorityNaturalIdNew("naturalIdNew2")
        .authoritySourceFileNew(UUID.randomUUID())
        .authoritySourceFileOld(UUID.randomUUID())
        .headingNew("headingNew2")
        .headingOld("headingOld2")
        .headingTypeNew("headingTypeNew2")
        .headingTypeOld("headingTypeOld2")
        .lbUpdated(2)
        .lbFailed(1)
        .lbTotal(5)
        .startedByUserId(userId2)
        .build()
    );
  }

  public static ResultList<UsersClient.User> usersList(List<UUID> userIds) {
    return ResultList.of(2, List.of(
      new UsersClient.User(
        userIds.get(0).toString(),
        "john_doe",
        true,
        new UsersClient.User.Personal("John", "Doe")
      ),
      new UsersClient.User(
        userIds.get(1).toString(),
        "quick_fox",
        true,
        new UsersClient.User.Personal("Quick", "Brown")
      )
    ));
  }

  public static AuthorityDataStatDto getStatDataDto(AuthorityDataStat dataStat, UsersClient.User user) {
    AuthorityDataStatDto dto = new AuthorityDataStatDto();
    dto.setId(dataStat.getId());
    dto.setAuthorityId(dataStat.getAuthorityData().getId());
    dto.setAction(AuthorityDataStatActionDto.fromValue(dataStat.getAction().name()));
    dto.setHeadingNew(dataStat.getHeadingNew());
    dto.setHeadingOld(dataStat.getHeadingOld());
    dto.setHeadingTypeNew(dataStat.getHeadingTypeNew());
    dto.setHeadingTypeOld(dataStat.getHeadingTypeOld());
    dto.setLbUpdated(dataStat.getLbUpdated());
    dto.setLbFailed(dataStat.getLbFailed());
    dto.setLbTotal(dataStat.getLbTotal());
    dto.setNaturalIdNew(dataStat.getAuthorityNaturalIdNew());
    dto.setNaturalIdOld(dataStat.getAuthorityNaturalIdOld());
    Metadata metadata = new Metadata();
    metadata.setStartedByUserId(dataStat.getStartedByUserId());
    metadata.setStartedByUserFirstName(user.personal().firstName());
    metadata.setStartedByUserLastName(user.personal().lastName());
    dto.setMetadata(metadata);
    dto.setSourceFileNew(dataStat.getAuthoritySourceFileNew().toString());
    dto.setSourceFileOld(dataStat.getAuthoritySourceFileOld().toString());
    return dto;
  }

  public record Link(UUID authorityId, String tag, String naturalId,
                     char[] subfields) {

    public static final UUID[] AUTH_IDS = new UUID[]{randomUUID(), randomUUID(), randomUUID(), randomUUID()};
    public static final String[] TAGS = new String[]{"100", "101", "700", "710"};

    public Link(UUID authorityId, String tag) {
      this(authorityId, tag, authorityId.toString(), new char[]{'a', 'b'});
    }

    public static Link of(int authIdNum, int tagNum) {
      return new Link(AUTH_IDS[authIdNum], TAGS[tagNum]);
    }

    public static Link of(int authIdNum, int tagNum, String naturalId, char[] subfields) {
      return new Link(AUTH_IDS[authIdNum], TAGS[tagNum], naturalId, subfields);
    }

    public InstanceLinkDto toDto(UUID instanceId) {
      return new InstanceLinkDto()
        .instanceId(instanceId)
        .authorityId(authorityId)
        .authorityNaturalId(naturalId)
        .bibRecordSubfields(toStringList(subfields))
        .bibRecordTag(tag);
    }

    private List<String> toStringList(char[] subfields) {
      List<String> result = new ArrayList<>();
      for (char subfield : subfields) {
        result.add(Character.toString(subfield));
      }
      return result;
    }

    public InstanceAuthorityLink toEntity(UUID instanceId) {
      return InstanceAuthorityLink.builder()
        .instanceId(instanceId)
        .authorityData(AuthorityData.builder()
          .id(authorityId)
          .naturalId(naturalId)
          .build())
        .bibRecordSubfields(subfields)
        .bibRecordTag(tag)
        .build();
    }
  }
}
