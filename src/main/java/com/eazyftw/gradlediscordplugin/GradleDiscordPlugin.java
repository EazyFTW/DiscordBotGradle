package com.eazyftw.gradlediscordplugin;

import com.eazyftw.gradlediscordplugin.extensions.MetaExtension;
import com.eazyftw.gradlediscordplugin.manager.DeploymentManager;
import com.eazyftw.gradlediscordplugin.manager.ResourceManager;
import com.eazyftw.gradlediscordplugin.util.Color;
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GradleDiscordPlugin implements Plugin<Project> {

    private MetaExtension meta;

    @Override
    public void apply(Project project) {
        DeploymentManager deploymentFile = new DeploymentManager(project);

        this.meta = project.getExtensions().create("meta", MetaExtension.class);

        try {
            ResourceManager.createGitIgnore(project);
            ResourceManager.createWorkflow(project);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Setting up Shadow Plugin
        project.getPlugins().apply("com.github.johnrengelman.shadow");
        getShadowJar(project).getArchiveFileName().set(project.getName() + ".jar");
        getShadowJar(project).setProperty("destinationDir", project.file("build"));

        project.getTasks().getByName("build").dependsOn("shadowJar");
        project.getTasks().getByName("build").doLast((task) -> uploadToRemotes(task, deploymentFile));

        // Add onProjectEvaluation hook
        project.afterEvaluate(this::onProjectEvaluation);
    }

    private void onProjectEvaluation(Project project) {
        log("Configuring Gradle Project - Build Settings...");
        log();
        log("Name: " + meta.name + " (" + project.getName() + ")");
        log("Version: " + meta.version);
        log("JDA Version: " + meta.jdaVersion);
        log();

        if (meta.validate())
            return;

        log(Color.RESET.toString());

        // Setting properties
        project.setProperty("version", meta.version);
        project.setProperty("sourceCompatibility", "1.8");
        project.setProperty("targetCompatibility", "1.8");

        Map<String, String> maps = new HashMap<>();
        maps.put("Main-Class", meta.mainClass);
        getShadowJar(project).manifest((m) -> m.attributes(maps));

        // Setting up repositories
        project.getRepositories().jcenter();
        project.getRepositories().mavenLocal();
        project.getRepositories().mavenCentral();

        if(meta.repositories != null)
            Arrays.stream(meta.repositories).forEach(url -> project.getRepositories().maven((maven) -> maven.setUrl(url)));

        List<String> dependencies = new ArrayList<>();
        dependencies.add("implementation#net.dv8tion:JDA:" + meta.jdaVersion);
        if(meta.dependencies != null)
            dependencies.addAll(Arrays.asList(meta.dependencies));

        dependencies.stream().filter(entry -> entry.contains("#")).map(entry -> entry.split("#")).forEach(confAndUrl -> project.getDependencies().add(confAndUrl[0], confAndUrl[1]));

        if(meta.relocations != null)
            Arrays.stream(meta.relocations).filter(entry -> entry.contains("#")).map(entry -> entry.split("#")).forEach(fromTo -> getShadowJar(project).relocate(fromTo[0], fromTo[1].replace("%", project.getName())));
    }

    private void uploadToRemotes(Task buildTask, DeploymentManager deploymentFile) {
        File file = new File("build/" + buildTask.getProject().getName() + ".jar");

        deploymentFile.getRemotes().stream().filter(DeploymentManager.Remote::isEnabled).forEach(all -> all.uploadFile(file));
    }

    private ShadowJar getShadowJar(Project project) {
        return (ShadowJar) project.getTasks().getByName("shadowJar");
    }

    public static void log(String message) {
        System.out.println(Color.RESET + message);
    }

    public static void log() {
        System.out.println();
    }
}
