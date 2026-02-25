"""
货运平台 - 订单 API 接口测试

覆盖:
  - 订单列表查询 (分页/筛选)
  - 订单详情查询
  - 订单状态更新
  - 仓库列表
"""
import requests

BASE_URL = "http://freight-backend:8080"
FRONTEND_URL = "http://freight-frontend:80"


class TestHealthCheck:

    def test_backend_reachable(self):
        resp = requests.get(f"{BASE_URL}/api/order/list", timeout=10)
        assert resp.status_code == 200

    def test_frontend_reachable(self):
        resp = requests.get(f"{FRONTEND_URL}/", timeout=10)
        assert resp.status_code == 200


class TestOrderList:

    def test_default_pagination(self):
        resp = requests.get(f"{BASE_URL}/api/order/list")
        data = resp.json()
        assert data["code"] == 200
        assert "total" in data["data"]
        assert "list" in data["data"]

    def test_custom_page_size(self):
        resp = requests.get(f"{BASE_URL}/api/order/list", params={"page": 1, "size": 5})
        data = resp.json()
        assert data["code"] == 200
        assert isinstance(data["data"]["list"], list)

    def test_filter_by_status(self):
        resp = requests.get(f"{BASE_URL}/api/order/list", params={"status": "PENDING"})
        data = resp.json()
        assert data["code"] == 200
        for order in data["data"]["list"]:
            assert order["status"] == "PENDING"


class TestOrderDetail:

    def test_order_not_found(self):
        resp = requests.get(f"{BASE_URL}/api/order/999999")
        data = resp.json()
        assert data["code"] == 404

    def test_get_existing_order(self):
        list_resp = requests.get(f"{BASE_URL}/api/order/list")
        list_data = list_resp.json()
        if list_data["data"]["list"]:
            order_id = list_data["data"]["list"][0]["id"]
            resp = requests.get(f"{BASE_URL}/api/order/{order_id}")
            data = resp.json()
            assert data["code"] == 200
            assert data["data"]["id"] == order_id


class TestOrderStatus:

    def test_update_nonexistent_order(self):
        resp = requests.put(f"{BASE_URL}/api/order/999999/status", json={"status": "PAID"})
        data = resp.json()
        assert data["code"] == 400


class TestWarehouse:

    def test_list_warehouses(self):
        resp = requests.get(f"{BASE_URL}/api/warehouse/list")
        data = resp.json()
        assert data["code"] == 200
        assert isinstance(data["data"], list)
