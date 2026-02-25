"""
货运平台 - 订单 API 测试配置
"""
import pytest

BASE_URL = "http://freight-backend:8080"
FRONTEND_URL = "http://freight-frontend:80"


@pytest.fixture(scope="session")
def api_base():
    return BASE_URL


@pytest.fixture(scope="session")
def frontend_base():
    return FRONTEND_URL
