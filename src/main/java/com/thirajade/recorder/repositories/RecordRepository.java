package com.thirajade.recorder.repositories;

import com.thirajade.recorder.entities.Record;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RecordRepository extends ElasticsearchRepository<Record, String> {
}
