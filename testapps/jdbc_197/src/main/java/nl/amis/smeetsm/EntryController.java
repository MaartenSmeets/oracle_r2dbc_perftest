package nl.amis.smeetsm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/entry")
public class EntryController {

    @Autowired
    EntryRepository entryRepository;

    @GetMapping("/{id}")
    private Optional<Entry> getEntryById(@PathVariable Long id) {
        return entryRepository.findById(id);
    }

    @GetMapping
    private Iterable<Entry> getAllEntries() {
        return entryRepository.findAll();
    }

    @PostMapping
    public Entry save(@RequestBody Entry entry) {
        return entryRepository.save(entry);
    }

    @DeleteMapping("/{id}")
    public void findById(@PathVariable Long id) {
        entryRepository.deleteById(id);
        return;
    }
}
