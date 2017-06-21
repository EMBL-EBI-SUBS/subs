package uk.ac.ebi.subs.data.submittable;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import uk.ac.ebi.subs.data.component.*;

import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
public abstract class BaseSubmittable<T extends BaseSubmittable> implements Submittable, Attributes {

    private String id;
    private String accession;
    private String alias;

    private Archive archive;
    private Team team;

    private String title;
    private String description;
    private List<Attribute> attributes = new ArrayList<>();

    public AbstractSubsRef<T> asRef(){
        AbstractSubsRef<T> subsLink = newRef();

        subsLink.setAccession(this.accession);
        subsLink.setTeam(this.getTeam().getName());
        subsLink.setAlias(this.alias);

        if (this.archive != null){
            subsLink.setArchive(this.archive.name());
        }
        if (this.team != null){
            subsLink.setTeam(this.getTeam().getName());
        }

        return subsLink;
    }

    protected abstract AbstractSubsRef<T> newRef();


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getAccession() {
        return accession;
    }

    @Override
    public void setAccession(String accession) {
        this.accession = accession;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public Archive getArchive() {
        return archive;
    }

    @Override
    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<Attribute> getAttributes() {

        return attributes;
    }

    @Override
    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

}
