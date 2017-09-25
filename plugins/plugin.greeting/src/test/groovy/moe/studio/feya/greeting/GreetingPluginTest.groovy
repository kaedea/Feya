/*
 * Copyright (c) 2017. Kaede (kidhaibara@gmail.com) All Rights Reserved.
 */
package moe.studio.feya.greeting

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.gradle.internal.impldep.junit.framework.TestCase.assertNotNull
import static org.gradle.internal.impldep.junit.framework.TestCase.assertTrue

class GreetingPluginTest {

    private Project project

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    public void testApplyPlugin() {
        project.pluginManager.apply 'moe.studio.feya.greeting'

        assertNotNull(project.tasks.hello)
        assertTrue(project.tasks.helloWithName instanceof GreetingTask)
    }
}