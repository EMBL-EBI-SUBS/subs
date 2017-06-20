package uk.ac.ebi.subs.data.dto;

import uk.ac.ebi.subs.data.component.Contact;
import uk.ac.ebi.subs.data.component.ProjectRef;
import uk.ac.ebi.subs.data.component.ProtocolRef;
import uk.ac.ebi.subs.data.component.Publication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by rolando on 14/06/2017.
 */
public class StudyDto extends BaseSubmittableDto {
    private List<Publication> publications = new ArrayList<>();
    private List<Contact> contacts = new ArrayList<>();

    private List<ProtocolRef> protocolRefs = new ArrayList<>();
    private ProjectRef projectRef = new ProjectRef();

    private Date releaseDate;

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<ProtocolRef> getProtocolRefs() {
        return protocolRefs;
    }

    public void setProtocolRefs(List<ProtocolRef> protocolRefs) {
        this.protocolRefs = protocolRefs;
    }

    public ProjectRef getProjectRef() {
        return projectRef;
    }

    public void setProjectRef(ProjectRef projectRef) {
        this.projectRef = projectRef;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
}
