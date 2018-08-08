/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */

package moe.studio.feya.greeting

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskState
import org.gradle.util.Clock

class GreetingPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.gradle.addListener(new TimeListener())

        project.task('hello') << {
            println "Hey, dude!"
        }
        project.extensions.create("greeting", GreetingPluginExtension)
        project.greeting.extensions.create("to", GreetingPluginNestedExtension)
        project.task("helloWithName", type: GreetingTask)
    }
}

class GreetingPluginExtension {
    String from
    String message
}

class GreetingPluginNestedExtension {
    String to
}

class GreetingTask extends DefaultTask {

    @TaskAction
    void greet() {
        println "sent from ${project.greeting.from} to ${project.greeting.to.to}"
        println "${project.greeting.message}"
    }
}

class TimeListener implements TaskExecutionListener, BuildListener {

    private Clock mClock
    private times = []


    @Override
    void buildStarted(Gradle gradle) {
    }

    @Override
    void settingsEvaluated(Settings settings) {
    }

    @Override
    void projectsLoaded(Gradle gradle) {
    }

    @Override
    void projectsEvaluated(Gradle gradle) {
    }

    @Override
    void buildFinished(BuildResult result) {
        println "Task consumed time : "
        for (time in times) {
            if (time[0] >= 50) {
                printf "%7sms  %s\n", time
            }
        }
    }

    @Override
    void beforeExecute(Task task) {
        mClock = new org.gradle.util.Clock()
    }

    @Override
    void afterExecute(Task task, TaskState state) {
        def ms = mClock.timeInMs
        times.add([ms, task.path])
        task.project.logger.warn "${task.path} consumed ${ms} ms"
    }
}