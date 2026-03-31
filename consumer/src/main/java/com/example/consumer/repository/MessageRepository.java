package com.example.consumer.repository;

import com.example.consumer.document.MessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageDocument, String> {

    List<MessageDocument> findByDestination(String destination);

    long countByDestinationAndCreatedDateAfterAndErrorIsNull(String destination, Instant after);
}
