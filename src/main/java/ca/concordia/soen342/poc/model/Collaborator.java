package ca.concordia.soen342.poc.model;

public class Collaborator {
    private int collaboratorId;
    private String name;
    private CollaboratorCategory category;

    public Collaborator() {
        this(0, "", CollaboratorCategory.JUNIOR);
    }

    public Collaborator(int collaboratorId, String name, CollaboratorCategory category) {
        this.collaboratorId = collaboratorId;
        this.name = name;
        this.category = category;
    }

    public Collaborator(Collaborator other) {
        this(other.collaboratorId, other.name, other.category);
    }

    public int getCollaboratorId() {
        return collaboratorId;
    }

    public void setCollaboratorId(int collaboratorId) {
        this.collaboratorId = collaboratorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CollaboratorCategory getCategory() {
        return category;
    }

    public void setCategory(CollaboratorCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Collaborator{" +
                "collaboratorId=" + collaboratorId +
                ", name='" + name + '\'' +
                ", category=" + category +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(obj instanceof Collaborator)) return false;

        Collaborator other = (Collaborator) obj;
        return collaboratorId == other.collaboratorId;
    }
}