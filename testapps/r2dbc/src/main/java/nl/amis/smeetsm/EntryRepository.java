package nl.amis.smeetsm;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EntryRepository extends ReactiveCrudRepository<Entry, Long> {
}
