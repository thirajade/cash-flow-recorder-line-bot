package com.thirajade.recorder.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "record", type = "details", shards = 1)
public class Record {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("action")
    private String action;

    @JsonProperty("type")
    private String type;

    @JsonProperty("income")
    private Double income;

    @JsonProperty("outcome")
    private Double outcome;

    @JsonProperty("bank")
    private String bank;

    @JsonProperty("payload")
    private String payload;

    @JsonProperty("transactionDate")
    @Field(type = FieldType.Date)
    private Date transactionDate;

    @JsonProperty("createdAt")
    @Field(type = FieldType.Date)
    private Date createdAt;
}
