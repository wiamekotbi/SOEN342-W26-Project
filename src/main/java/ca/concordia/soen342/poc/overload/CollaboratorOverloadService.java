package ca.concordia.soen342.poc.overload;

import ca.concordia.soen342.poc.model.Collaborator;
import ca.concordia.soen342.poc.model.CollaboratorCategory;
import ca.concordia.soen342.poc.model.Task;
import ca.concordia.soen342.poc.repository.TaskRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CollaboratorOverloadService {
    private final TaskRepository repo;

    public CollaboratorOverloadService(TaskRepository repo) {
        this.repo = repo;
    }

    public boolean hasValidPositiveLimits() {
        for (CollaboratorCategory category : CollaboratorCategory.values()) {
            if (category.getOpenTaskLimit() <= 0) {
                return false;
            }
        }
        return true;
    }

    public List<CollaboratorOverloadInfo> getOverloadedCollaborators() {
        Map<Integer, CollaboratorOverloadInfo> counts = new LinkedHashMap<>();

        for (Task task : repo.findAll()) {
            if (task == null || !task.isOpen()) {
                continue;
            }

            List<Collaborator> collaborators = task.getCollaborators();
            if (collaborators == null) {
                continue;
            }

            for (Collaborator collaborator : collaborators) {
                if (collaborator == null) {
                    continue;
                }

                CollaboratorOverloadInfo info = counts.get(collaborator.getCollaboratorId());
                if (info == null) {
                    info = new CollaboratorOverloadInfo(
                        collaborator.getCollaboratorId(),
                        collaborator.getName(),
                        collaborator.getCategory(),
                        0
                    );
                    counts.put(collaborator.getCollaboratorId(), info);
                }

                info.incrementOpenTaskCount();
            }
        }

        List<CollaboratorOverloadInfo> overloaded = new ArrayList<>();
        for (CollaboratorOverloadInfo info : counts.values()) {
            if (info.isOverloaded()) {
                overloaded.add(info);
            }
        }

        return overloaded;
    }
}