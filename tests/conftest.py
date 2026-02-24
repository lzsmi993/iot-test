"""
IoT 平台 - 登录日志 API 测试配置
"""
import pytest

# 后端 API 基础 URL（Jenkins 容器内通过容器名访问）
BASE_URL = "http://iot-backend:8080"
FRONTEND_URL = "http://iot-frontend:80"


@pytest.fixture(scope="session")
def api_base():
    """返回 API 基础 URL"""
    return f"{BASE_URL}/api/v1"


@pytest.fixture(scope="session")
def frontend_base():
    """返回前端基础 URL"""
    return FRONTEND_URL
