/*
 * Copyright (c) 2018. Kaede <kidhaibara@gmail.com>.
 *
 */

package com.kaedea.gradle.requires

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies

class RequiresPlugin implements Plugin<Project> {
    private static final String PLUGIN_EXT_CONFIG = "needs"

    @Override
    void apply(Project project) {
        project.extensions.create(PLUGIN_EXT_CONFIG, RequiresPluginExtension.class)

        project.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            void beforeResolve(ResolvableDependencies resolvableDependencies) {
                project.logger.info "========================"
                project.logger.info "Requires plugin DependencyResolutionListener#beforeResolve"
                project.logger.info "Dumping deps info, = ${project.extensions[PLUGIN_EXT_CONFIG].deps}"
                project.extensions[PLUGIN_EXT_CONFIG].deps.each {
                    addDependency(project, it)
                }
                project.getGradle().removeListener(this)
            }

            @Override
            void afterResolve(ResolvableDependencies resolvableDependencies) {}
        })
    }

    private void addDependency(def project, def dep) {
        // Dependency for compilation.
        // Hide transitive dependencies from client.
        project.configurations['compile'].dependencies.add(project.dependencies.create(dep) {
            transitive = false
        })
        // Dependency for package.
        // Need all transitive dependencies in runtime.
        project.configurations['runtimeOnly'].dependencies.add(project.dependencies.create(dep) {
            transitive = true
        })
    }
}

class RequiresPluginExtension {
    Set<String> deps = new HashSet<>()
    public void compile(def dep) {
        deps << dep
    }
}