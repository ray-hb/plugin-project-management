package com.ray.plugins

import com.ray.plugins.bean.*
import org.codehaus.groovy.util.ListHashMap
import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle

class SettingPlugins implements Plugin<Settings> {

    private static final String BUILD_FILE_NAME = "build.gradle"

    private static final String KOTLIN_BUILD_FILE_NAME = "build.gradle.kts"

    private static final String APP_DIR = "app"

    private static final List<String> EXCLUDE_DIR = ['build', '.gradle', 'src', 'libs', 'buildSrc']

    @Override
    void apply(Settings settings) {

        // get repository setting
        def xmlRepository = new File(settings.rootDir, "repository.xml")
        if (!xmlRepository.exists()) {
            println("please set repository file")
            return
        }

        //fit config
        parseXmlConfigs(xmlRepository)

        // 设置maven依赖
        settings.dependencyResolutionManagement.repositories {
            maven { MavenArtifactRepository mavenRepos ->

                if (TempHouse.repositoryConfig.isLocalRepository()) {
                    mavenRepos.setUrl(new File(TempHouse.repositoryConfig.url).toURI())
                } else {
                    mavenRepos.setUrl(URI.create(TempHouse.repositoryConfig.url))
                }

                if (TempHouse.repositoryConfig.credentials != null) {
                    def credential = TempHouse.repositoryConfig.credentials
                    if (!credential.userName.isBlank() && !credential.password.isBlank()) {
                        mavenRepos.credentials { PasswordCredentials credentials ->
                            credentials.username = credential.userName
                            credentials.password = credential.password
                        }
                    }
                }
            }
        }

        //include project
        List<File> projects = []
        settings.rootDir.listFiles().each {
            projects.addAll(getProject(it))
        }

        projects.each {
            def projectConfig = TempHouse.projectConfigMap[it.name]
            if ((projectConfig != null && projectConfig.sourceComply()) || it.name == APP_DIR) {
                settings.include(it.name)
                settings.project(":${it.name}").projectDir = it
                println("project:${it.name}")
            }
        }

        settings.gradle.addBuildListener(new BuildListener() {
            @Override
            void settingsEvaluated(Settings setting) {

            }

            @Override
            void projectsLoaded(Gradle gradle) {
                convertSelfProjectDependency(gradle)
                gradle.rootProject.subprojects { Project project ->
                    project.ext {
                        Projects = TempHouse.projectDependency
                    }
                    project.beforeEvaluate {
                        it.plugins.apply(ProjectDependencyResolvePlugin)
                        it.plugins.apply(ProjectMavenPlugins)
                    }
                }
            }

            @Override
            void projectsEvaluated(Gradle gradle) {
                List<Task> childPublishingTask = []
                gradle.rootProject.subprojects { Project project ->
                    def publishingTask = project.tasks.findByName("publish")
                    if (publishingTask != null) {
                        gradle.rootProject.tasks.create(name: "publish_${project.name}", group: "publishManage", dependsOn: [publishingTask])
                        childPublishingTask.add(publishingTask)
                    }
                }

                gradle.rootProject.tasks.create(name: "publishAll", group: "publishManage", dependsOn: childPublishingTask) {

                }
            }

            @Override
            void buildFinished(BuildResult buildResult) {

            }
        })
    }

    private static Map<String, Object> convertSelfProjectDependency(Gradle gradle) {
        def allProjects = gradle.rootProject.allprojects
        TempHouse.projectConfigMap.each {
            if (it.value.sourceComply()) {
                def project = allProjects.find { Project project -> project.name == it.key
                }
                TempHouse.projectDependency[it.key] = project
            } else {
                TempHouse.projectDependency[it.key] = "${it.value.publishConfig.groupId}:${it.value.publishConfig.artifactId}:${it.value.publishConfig.version}"
            }
        }
    }


    private static boolean isBuildModel(File file) {
        if (file == null || !file.isDirectory()) return false
        if (EXCLUDE_DIR.contains(file.name)) return false
        def buildScript = file.absolutePath + "/" + BUILD_FILE_NAME
        def kotlinBuildScript = file.absolutePath + "/" + KOTLIN_BUILD_FILE_NAME
        return new File(buildScript).exists() || new File(kotlinBuildScript).exists()
    }

    private static List<File> getProject(File file) {
        def projects = []
        if (file == null || !file.isDirectory()) return projects
        if (isBuildModel(file)) {
            projects.add(file)
        } else {
            file.listFiles().each {
                projects.addAll(getProject(it))
            }
        }
        return projects
    }

    private static parseXmlConfigs(File configFile) {

        def repository = new XmlParser().parse(configFile)

        //parse repository config
        def target = repository.repositorys_config[0].attributes().get("target")
        ListHashMap<String, RepositoryConfig> repositoryConfigMap = new LinkedHashMap<String, RepositoryConfig>()
        repository.repositorys_config.config.each { config ->

            def type = config.type[0].text()
            def url = config.url[0].text()

            def userName = config.credentials[0].username[0].text()
            def password = config.credentials[0].password[0].text()

            def credentials = new Credentials(userName, password)
            def repositoryConfig = new RepositoryConfig(type, url, credentials)
            repositoryConfigMap[type] = repositoryConfig
        }
        def targetRepositoryConfig = repositoryConfigMap[target]
        //default to set repository local
        if (targetRepositoryConfig == null) {
            targetRepositoryConfig = new RepositoryConfig("local", "LocalMaven", null)
        }
        if (targetRepositoryConfig.isLocalRepository()) {
            def file = new File(configFile.getParentFile().getAbsolutePath() + "/" + targetRepositoryConfig.url)
            if (!file.exists()) {
                file.mkdirs()
            }
            targetRepositoryConfig.url = file.absolutePath
        }
        //save into TemHouse
        TempHouse.repositoryConfig = targetRepositoryConfig

        //parse project config,save into TemHouse
        repository.projects_config.project.each { project ->
            def name = project.name[0].text()
            def useSource = project.use_source[0].text()
            def groupId = project.maven_config.groupId[0].text()
            def artifactId = project.maven_config.artifactId[0].text()
            def version = project.maven_config.version[0].text()
            def publishConfig = new PublishConfig(groupId, artifactId, version)
            def projectConfig = new ProjectConfig(name, publishConfig, useSource)
            TempHouse.projectConfigMap[name] = projectConfig
        }
    }
}