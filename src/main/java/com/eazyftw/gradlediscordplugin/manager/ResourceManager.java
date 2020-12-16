package com.eazyftw.gradlediscordplugin.manager;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ResourceManager {

    public static void createGitIgnore(Project project) throws IOException {
        InputStream src = ResourceManager.class.getResourceAsStream("/gitignore.file");
        Files.copy(src, Paths.get(new File(project.getProjectDir().getAbsolutePath()+"/.gitignore").toURI()), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void createWorkflow(Project project) throws IOException {
        File destination = new File(project.getProjectDir().getAbsolutePath()+"/.github/workflows/build.yml");
        destination.mkdirs();

        InputStream src = ResourceManager.class.getResourceAsStream("/workflows/build.yml");
        Files.copy(src, Paths.get(destination.toURI()), StandardCopyOption.REPLACE_EXISTING);
    }
}
