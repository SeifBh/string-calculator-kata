package com._4dconcept.evaluation.service;

import com._4dconcept.evaluation.dto.DeveloperView;
import com._4dconcept.evaluation.helper.ProjectsFileHelper;
import com._4dconcept.evaluation.manager.DeveloperManager;
import com._4dconcept.evaluation.model.Developer;
import com._4dconcept.evaluation.model.Projects;
import com._4dconcept.evaluation.repository.DeveloperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

import static com._4dconcept.evaluation.constant.Constants.DEVELOPER_STATUS_ACTIVE;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@Component
@RequiredArgsConstructor
public class DeveloperService implements DeveloperManager {

    private final DeveloperRepository developerRepository;

    @Value("${project.file.path}")
    private String projectFilePath;


    private Projects projects;

    @PostConstruct
    public void init() {
        this.projects = ProjectsFileHelper.loadProjects(projectFilePath);
        log.info("Projects loaded from file: {}", projectFilePath);
    }


    @Override
    public List<DeveloperView> getDevelopers(final Boolean hasProjects) {
        final List<Developer> developers;
        if (hasProjects) {
            developers = developerRepository.findAllByStatusIs(DEVELOPER_STATUS_ACTIVE);
        } else {
            developers = developerRepository.findByProjectIdIsNullAndStatusIs(DEVELOPER_STATUS_ACTIVE);
        }

        final Projects projects = ProjectsFileHelper.loadProjects(projectFilePath);

        return developers.stream()
                .map(developer -> toDeveloperView(developer, projects))
                .collect(toList());
    }

    private DeveloperView toDeveloperView(final Developer developer, final Projects projects) {
        final DeveloperView developerView = new DeveloperView(developer.getId(), developer.getName());

        if (isAssignedToProject(developer) && nonNull(projects)) {
            projects.getProjects().stream()
                    .filter(project -> project.getId().equals(developer.getProjectId()))
                    .findFirst()
                    .ifPresent(project -> developerView.setProjectName(project.getName()));
        }

        return developerView;
    }

    private boolean isAssignedToProject(final Developer developer) {
        return nonNull(developer.getProjectId());
    }

    @Override
    public void createDeveloper(final DeveloperView developerView) {
        final Developer developerToCreate = Developer.builder()
                .name(developerView.getName())
                .projectId(developerView.getProjectId())
                .build();

        developerRepository.save(developerToCreate);
    }
}
