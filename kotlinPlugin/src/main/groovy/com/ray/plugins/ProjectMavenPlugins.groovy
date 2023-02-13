package com.ray.plugins

import com.ray.plugins.bean.ProjectConfig
import com.ray.plugins.bean.PublishConfig
import com.ray.plugins.bean.RepositoryConfig
import com.ray.plugins.bean.TempHouse
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar

class ProjectMavenPlugins implements Plugin<Project> {

    static Task generateSourcesJar(Project project) {
        return project.tasks.create(name: "generateSourcesJar", type: Jar) {
            from project.android.sourceSets.main.java.srcDirs
            classifier 'sources'
        }
    }

    @Override
    void apply(Project project) {
        println("apply project name:${project.name}")
        if (project == project.rootProject || project.plugins.hasPlugin("com.android.application") || project.name == "app") {
            project.logger.warn("only the library module will apply the plugin:MavenPublishPlugin ")
            return
        }
        project.plugins.apply(MavenPublishPlugin)

        project.afterEvaluate {

            RepositoryConfig repositoryConfig = TempHouse.repositoryConfig
            ProjectConfig projectConfig = TempHouse.projectConfigMap[project.name]

            if (projectConfig == null) return
            PublishConfig config = projectConfig.publishConfig
            PublishingExtension publish = project.extensions.findByName("publishing")
            publish.publications.create("paidDebug", MavenPublication) {
                groupId = config.groupId
                artifactId = config.artifactId
                version = config.version
                project.afterEvaluate { artifact(project.tasks.getByName("bundleReleaseAar")) }
                artifact generateSourcesJar(project)
                pom.withXml {
                    def dependenciesNode = asNode().appendNode('dependencies')
                    project.configurations.implementation.allDependencies.each {
                        // 避免出现空节点或 artifactId=unspecified 的节点
                        if (it.group != null && (it.name != null && "unspecified" != it.name) && it.version != null) {
                            println it.toString()
                            def dependencyNode = dependenciesNode.appendNode('dependency')
                            dependencyNode.appendNode('groupId', it.group)
                            dependencyNode.appendNode('artifactId', it.name)
                            dependencyNode.appendNode('version', it.version)
                            dependencyNode.appendNode('scope', 'implementation')
                        }
                    }
                }
            }
            publish.repositories {
                maven { MavenArtifactRepository repository ->
                    repository.setUrl(project.uri(repositoryConfig.url))
                    def credential = repositoryConfig.credentials
                    if (credential != null) {
                        if (!credential.userName.isBlank() && !credential.password.isBlank()) {
                            repository.credentials { PasswordCredentials credentials ->
                                credentials.username = credential.userName
                                credentials.password = credential.password
                            }
                        }
                    }
                }
            }
        }
    }
}