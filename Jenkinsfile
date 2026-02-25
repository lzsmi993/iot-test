pipeline {
    agent any

    environment {
        // 数据库配置
        DB_HOST     = 'host.docker.internal'
        DB_PORT     = '3306'
        DB_USER     = 'root'
        DB_PASS     = '123456'
        // Docker 镜像
        BACKEND_IMAGE  = 'freight-backend'
        FRONTEND_IMAGE = 'freight-frontend'
        IMAGE_TAG      = "${BUILD_NUMBER}"
    }

    stages {

        stage('拉取代码') {
            steps {
                echo '📥 拉取代码...'
                git branch: 'main',
                    url: 'https://github.com/lzsmi993/iot-test.git'
            }
        }

        stage('后端编译') {
            steps {
                echo '☕ Maven 编译后端...'
                dir('backend') {
                    sh 'mvn clean package -DskipTests -B'
                }
            }
        }

        stage('前端构建') {
            steps {
                echo '🎨 构建前端...'
                dir('frontend') {
                    sh 'npm install --registry=https://registry.npmmirror.com'
                    sh 'npm run build'
                }
            }
        }

        stage('构建 Docker 镜像') {
            parallel {
                stage('后端镜像') {
                    steps {
                        echo '🐳 构建后端镜像...'
                        dir('backend') {
                            sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} -t ${BACKEND_IMAGE}:latest ."
                        }
                    }
                }
                stage('前端镜像') {
                    steps {
                        echo '🐳 构建前端镜像...'
                        dir('frontend') {
                            sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} -t ${FRONTEND_IMAGE}:latest ."
                        }
                    }
                }
            }
        }

        stage('部署测试环境') {
            steps {
                echo '🚀 部署到测试环境...'
                script {
                    // 停掉旧容器
                    sh '''
                        docker stop freight-backend freight-frontend 2>/dev/null || true
                        docker rm freight-backend freight-frontend 2>/dev/null || true
                    '''

                    // 启动后端
                    sh """
                        docker run -d --name freight-backend \
                            -e DB_HOST=${DB_HOST} \
                            -e DB_PORT=${DB_PORT} \
                            -e DB_USER=${DB_USER} \
                            -e DB_PASS=${DB_PASS} \
                            -p 8081:8080 \
                            --restart unless-stopped \
                            ${BACKEND_IMAGE}:${IMAGE_TAG}
                    """

                    // 等后端启动
                    sh 'sleep 15'

                    // 启动前端
                    sh """
                        docker run -d --name freight-frontend \
                            --link freight-backend:freight-backend \
                            -p 8088:80 \
                            --restart unless-stopped \
                            ${FRONTEND_IMAGE}:${IMAGE_TAG}
                    """
                }
            }
        }

        stage('健康检查') {
            steps {
                echo '🏥 健康检查...'
                script {
                    sh 'sleep 10'

                    // 检查后端 API
                    def backendStatus = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:8081/api/order/list",
                        returnStdout: true
                    ).trim()
                    echo "后端 API 状态码: ${backendStatus}"

                    if (backendStatus != '200') {
                        error "❌ 后端健康检查失败! HTTP ${backendStatus}"
                    }

                    // 检查前端
                    def frontendStatus = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:8088/",
                        returnStdout: true
                    ).trim()
                    echo "前端页面状态码: ${frontendStatus}"

                    if (frontendStatus != '200') {
                        error "❌ 前端健康检查失败! HTTP ${frontendStatus}"
                    }

                    echo '✅ 所有服务健康检查通过!'
                }
            }
        }
    }

    post {
        success {
            echo """
            ✅ 部署成功!
            ━━━━━━━━━━━━━━━━━━━━━━━
            前端: http://localhost:8088
            后端: http://localhost:8081/api/order/list
            镜像: ${BACKEND_IMAGE}:${IMAGE_TAG}, ${FRONTEND_IMAGE}:${IMAGE_TAG}
            ━━━━━━━━━━━━━━━━━━━━━━━
            """
        }
        failure {
            echo '❌ 构建/部署失败，请检查日志'
        }
        always {
            // 清理 workspace
            cleanWs(cleanWhenNotBuilt: false)
        }
    }
}
