package com.ray.plugins

import com.ray.plugins.bean.TempHouse
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.component.ProjectComponentSelector

class ProjectDependencyResolvePlugin implements Plugin<Project> {

    @Override
    void apply(Project applyProject) {

        def rootModuleName = applyProject.rootProject.name

        applyProject.configurations.all { Configuration configuration ->

            configuration.resolutionStrategy {

                //处理项目依赖和远程依赖错误问题
                dependencySubstitution {

                    all {
                        if (it.requested instanceof ModuleComponentSelector) {
                            def requested = it.requested as ModuleComponentSelector
                            def group = requested.group
                            def name = requested.module
                            def version = requested.version
                            //采用远程依赖
                            if (moduleFromLocalProject(rootModuleName, group, name, version)) {
                                def config = TempHouse.projectConfigMap[name]
                                if (config != null) {
                                    if (config.sourceComply()) {
                                        //use local project
                                        it.useTarget(applyProject.findProject(":${name}"))
                                    } else {
                                        //use remote module
                                        def publish = config.publishConfig
                                        it.useTarget("${publish.groupId}:${publish.artifactId}:${publish.version}")
                                    }
                                }
                            }
                        }
                    }

                    all {
                        if (it.requested instanceof ProjectComponentSelector) {
                            def requested = it.requested as ProjectComponentSelector
                            def config = TempHouse.projectConfigMap[requested.buildName]
                            if (config != null && !config.sourceComply()) {
                                def publish = config.publishConfig
                                dependency.useTarget("${publish.groupId}:${publish.artifactId}:${publish.version}")
                            }
                        }
                    }
                }
                // cache dynamic versions for 10 minutes
                cacheDynamicVersionsFor 30 * 60, 'seconds'
                // don't cache changing modules at all
                cacheChangingModulesFor 0, 'seconds'
            }
        }
    }


    private static boolean moduleFromLocalProject(String rootProjectName, String group, String name, String version) {
        return fromProjectInCompleteRemoteDependency(rootProjectName, group, name, version) || fromProjectCompleteRemoteDependency(rootProjectName, group, name, version)
    }


    private static boolean fromProjectInCompleteRemoteDependency(String rootProjectName, String group, String name, String version) {
        return group == rootProjectName && TempHouse.projectConfigMap.containsKey(name)
    }

    private static boolean fromProjectCompleteRemoteDependency(String rootProjectName, String group, String name, String version) {
        def localRecord = TempHouse.projectConfigMap[name]
        if (localRecord == null) {
            return false
        }
        def publishInfo = localRecord.publishConfig
        return publishInfo != null && (group == publishInfo.groupId && name == publishInfo.artifactId && version == publishInfo.version)
    }
}