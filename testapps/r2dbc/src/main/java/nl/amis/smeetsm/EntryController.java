package nl.amis.smeetsm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//example: https://gitorko.github.io/2019/04/03/Spring-Webflux-R2DBC/
@RestController
@RequestMapping("/entry")
public class EntryController {

    @Autowired
    EntryRepository entryRepository;

    @GetMapping("/{id}")
    private Mono<Entry> getPersonById(@PathVariable Long id) {
        return entryRepository.findById(id);
    }

    @GetMapping
    private Flux<Entry> getAllPersons() {
        return entryRepository.findAll();
    }

    @PostMapping
    public Mono<Entry> save(@RequestBody Entry entry) {
        return entryRepository.save(entry);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> findById(@PathVariable Long id) {
        return entryRepository.deleteById(id);
    }
}