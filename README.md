# plugin-project-management
辅助管理project插件

## 辅助管理项目插件

### 1.导入方式：
在setting文件中使用：apply plugin 'com.ray.easysettings'
maven地址："https://gitee.com/android-ray/self-maven/raw/master"
classpath 'com.ray.settings:easysettings:2.0.4'

### 2.配置

```
<repository>
    
    <repositorys_config target="local">

        <config>
            <type>snapshot</type>
            <url>""</url>
            <credentials>
                <username />
                <password />
            </credentials>
        </config>

        <config>
            <type>release</type>
            <url>""</url>
            <credentials>
                <username />
                <password />
            </credentials>
        </config>

        <config>
            <type>local</type>
            <url>LocalMaven</url>
            <credentials>
                <username />
                <password />
            </credentials>
        </config>

    </repositorys_config>

    <projects_config>

        <project>
            <name>commonUtils</name>
            <maven_config>
                <groupId>com.ray.settings</groupId>
                <artifactId>commonUtils</artifactId>
                <version>2.0.0</version>
            </maven_config>
            <use_source>0</use_source>
        </project>

        <project>
            <name>commonres</name>
            <maven_config>
                <groupId>com.ray.settings</groupId>
                <artifactId>commonres</artifactId>
                <version>2.0.0</version>
            </maven_config>
            <use_source>1</use_source>
        </project>

    </projects_config>

</repository>

```
* 该配置文件位于根目录
* repositorys_config配置maven的认证信息，target是选择哪个环境的maven仓库
* projects_config 配置项目各个模块maven上传组、名称、版本，name名称需要与模块名称保持一致
* use_source：1.表示是采用本地编译项目 2.表示采用maven仓库进行远程集成

#### 功能：
* 1.无须在setting文件中声明project，只需在配置文件中标明，就会自动寻找，加入构建中
* 2.子项目可以在任意目录下，无须和app保持同一文件目录
* 3.task任务中子模块快速执行maven提交的操作
* 4.模块化开发过程中可以自由切换各个模块的集成方式，采用本地源码集成还是远程仓库代码集成，多分支情况下有比较好的效果
         
