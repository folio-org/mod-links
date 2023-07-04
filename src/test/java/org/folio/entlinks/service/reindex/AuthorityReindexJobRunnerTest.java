package org.folio.entlinks.service.reindex;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mockrunner.mock.jdbc.MockResultSet;
import java.util.UUID;
import org.folio.entlinks.domain.dto.Authority;
import org.folio.entlinks.domain.entity.ReindexJob;
import org.folio.entlinks.domain.entity.ReindexJobResource;
import org.folio.entlinks.integration.kafka.EventProducer;
import org.folio.entlinks.service.reindex.event.DomainEvent;
import org.folio.spring.FolioExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class AuthorityReindexJobRunnerTest {

  private @Mock JdbcTemplate jdbcTemplate;
  private @Mock EventProducer<DomainEvent<?>> eventProducer;
  private @Mock FolioExecutionContext folioExecutionContext;
  private @Mock ReindexService reindexService;

  private @InjectMocks AuthorityReindexJobRunner jobRunner;

  @Test
  void name() {
    MockResultSet mockResultSet = new MockResultSet("1");
    mockResultSet.addColumn("jsonb", new String[]{"1"});
    when(jdbcTemplate.queryForObject(any(), any(Class.class))).thenReturn(1);
    when(jdbcTemplate.queryForStream(anyString(), any())).thenAnswer(invocationOnMock -> {
      var rowMapper = (RowMapper<Authority>) invocationOnMock.getArguments()[1];
      return rowMapper.mapRow(mockResultSet, 1);
    });

    jobRunner.startReindex(new ReindexJob().withResourceName(ReindexJobResource.AUTHORITY).withId(UUID.randomUUID()));
  }
}
