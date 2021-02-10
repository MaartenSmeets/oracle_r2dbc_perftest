package nl.amis.smeetsm;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "TESTTABLE")
public class Entry {

    @Id
    @Column(name="id")
    private long id;

    @Column(name="ts")
    private Timestamp ts;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Timestamp getTs() {
        return ts;
    }

    public void setTs(Timestamp ts) {
        this.ts = ts;
    }
}
