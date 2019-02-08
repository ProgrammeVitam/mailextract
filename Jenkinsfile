pipeline {
    agent {
        label 'slaves'
    }

    environment {
        MVN_BASE = "/usr/local/maven/bin/mvn"
        MVN_COMMAND = "${MVN_BASE} --settings ${pwd()}/.ci/settings.xml --show-version --batch-mode --errors --fail-at-end -DinstallAtEnd=true -DdeployAtEnd=true "
        DEPLOY_GOAL = " " // Deploy goal used by maven ; typically "deploy" for master* branches & "" (nothing) for everything else (we don't deploy) ; keep a space so can work in other branches than develop
        CI = credentials("app-jenkins")
        SERVICE_SONAR_URL = credentials("service-sonar-url")
        SERVICE_NEXUS_URL = credentials("service-nexus-url")
        SERVICE_CHECKMARX_URL = credentials("service-checkmarx-url")
        SERVICE_REPO_SSHURL = credentials("repository-connection-string")
        SERVICE_GIT_URL = credentials("service-gitlab-url")
        SERVICE_PROXY_HOST = credentials("http-proxy-host")
        SERVICE_PROXY_PORT = credentials("http-proxy-port")
        MAILEXTRACT_GIT_URL=credentials("mailextract-gitlab-url")
    }

    triggers {
        upstream(upstreamProjects: 'build-droid', threshold: hudson.model.Result.SUCCESS)
    }

   stages {

       stage("Tools configuration") {
           steps {
               echo "Workspace location : ${env.WORKSPACE}"
               echo "Branch : ${env.GIT_BRANCH}"
               // default behavior
               writeFile file: 'deploy_goal.txt', text: "${env.DEPLOY_GOAL}"
           }
       }

        // Override the default maven deploy target when on master (publish on nexus)
        stage("Computing maven target") {
            when {
                anyOf {
                    branch "master"
                    tag pattern: "^[1-9]+\\.[0-9]+\\.[0-9]+-?[0-9]*\$", comparator: "REGEXP"                    
                }
            }
            environment {
                DEPLOY_GOAL = "deploy"
                MASTER_BRANCH = "true"
            }
            steps {
                script {
                    // overwrite file content with one more goal
                    writeFile file: 'deploy_goal.txt', text: "${env.DEPLOY_GOAL}"
                    writeFile file: 'master_branch.txt', text: "${env.MASTER_BRANCH}"
                 }
                echo "We are on master branch (${env.GIT_BRANCH}) ; deploy goal is \"${env.DEPLOY_GOAL}\""
            }
        }

        stage("Build mailextract") {
            environment {
                DEPLOY_GOAL = readFile("deploy_goal.txt")
            }
            steps {
                    sh '$MVN_COMMAND -f pom.xml clean install -DskipTests -Dmaven.skip.tests=true $DEPLOY_GOAL'
                    // skipTests as windres is not installed
            }
        }
    }
}
