package com.ray.plugins.bean

import org.codehaus.groovy.util.ListHashMap

import java.util.function.BiConsumer

class Credentials {

    String userName
    String password

    public Credentials(String userName, String password) {
        this.userName = userName
        this.password = password
    }
}

class RepositoryConfig {

    String type
    String url
    Credentials credentials

    RepositoryConfig(String type, String url, Credentials credentials) {
        this.type = type
        this.url = url
        this.credentials = credentials
    }

    boolean isLocalRepository() {
        return type.equals("local")
    }
}


class PublishConfig {

    String groupId
    String artifactId
    String version

    public PublishConfig(String groupId, String artifactId, String version) {
        this.groupId = groupId
        this.artifactId = artifactId
        this.version = version
    }
}

class ProjectConfig {

    String name
    PublishConfig publishConfig
    String useSource

    public ProjectConfig(String name, PublishConfig publishConfig, String useSource) {
        this.name = name
        this.publishConfig = publishConfig
        this.useSource = useSource
    }

    public boolean sourceComply() {
        return "1" == useSource
    }
}

class TempHouse {
    static LinkedHashMap<String, ProjectConfig> projectConfigMap = new LinkedHashMap<>()
    static RepositoryConfig repositoryConfig = new RepositoryConfig(null, null, null)
    static ListHashMap<String, Object> projectDependency = new LinkedHashMap<>()

    static print() {
        println("projectConfigs:")
        projectConfigMap.forEach(new BiConsumer<String, ProjectConfig>() {
            @Override
            void accept(String s, ProjectConfig projectConfig) {
                println("name = ${s},ProjectConfigName = " + projectConfig.name)
            }
        })
        println("repositoryConfig:")
        println("type = ${repositoryConfig.type} ,url = ${repositoryConfig.url} ,credential=${repositoryConfig.credentials}")

        println("projectDependency:")
        projectDependency.forEach { key, value -> println("name= $key , value=$value")
        }
    }
}



