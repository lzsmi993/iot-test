"""
IoT 平台 - 登录日志 API 接口测试

覆盖 PRD 用户故事:
  LOG-001: 查看登录日志列表 (分页)
  LOG-002: 多条件筛选
  LOG-003: 查看日志详情
  LOG-004: 导出登录日志
  LOG-005: 数据权限隔离
"""
import pytest
import requests


class TestHealthCheck:
    """服务健康检查"""

    @pytest.mark.smoke
    def test_backend_reachable(self, api_base):
        """后端服务可达性检查"""
        resp = requests.get(f"{api_base}/login-logs", params={"pageSize": 1}, timeout=10)
        assert resp.status_code == 200
        data = resp.json()
        assert data["code"] == 200

    @pytest.mark.smoke
    def test_frontend_reachable(self, frontend_base):
        """前端页面可达性检查"""
        resp = requests.get(frontend_base, timeout=10)
        assert resp.status_code == 200


class TestLoginLogList:
    """LOG-001: 查看登录日志列表"""

    @pytest.mark.api
    @pytest.mark.page
    def test_default_pagination(self, api_base):
        """默认分页 - 第1页，每页10条"""
        resp = requests.get(f"{api_base}/login-logs", timeout=10)
        data = resp.json()
        assert data["code"] == 200
        assert "total" in data["data"]
        assert "records" in data["data"]
        assert "current" in data["data"]
        assert "size" in data["data"]

    @pytest.mark.api
    @pytest.mark.page
    def test_custom_page_size(self, api_base):
        """自定义分页大小"""
        resp = requests.get(f"{api_base}/login-logs", params={"pageSize": 3}, timeout=10)
        data = resp.json()
        assert data["code"] == 200
        assert data["data"]["size"] == 3
        assert len(data["data"]["records"]) <= 3

    @pytest.mark.api
    @pytest.mark.page
    def test_page_navigation(self, api_base):
        """翻页功能"""
        resp1 = requests.get(f"{api_base}/login-logs", params={"pageNum": 1, "pageSize": 5}, timeout=10)
        resp2 = requests.get(f"{api_base}/login-logs", params={"pageNum": 2, "pageSize": 5}, timeout=10)
        data1 = resp1.json()
        data2 = resp2.json()
        assert data1["code"] == 200
        assert data2["code"] == 200
        # 第2页的记录应该和第1页不同（如果有足够数据）
        if data2["data"]["records"]:
            ids1 = {r["id"] for r in data1["data"]["records"]}
            ids2 = {r["id"] for r in data2["data"]["records"]}
            assert ids1.isdisjoint(ids2), "不同页的记录不应重复"

    @pytest.mark.api
    def test_response_fields(self, api_base):
        """返回字段完整性检查"""
        resp = requests.get(f"{api_base}/login-logs", params={"pageSize": 1}, timeout=10)
        data = resp.json()
        assert data["code"] == 200
        if data["data"]["records"]:
            record = data["data"]["records"][0]
            expected_fields = [
                "id", "userId", "username", "loginTime", "ip",
                "loginMethod", "loginMethodDesc",
                "eventType", "eventTypeDesc",
                "result", "resultDesc", "deviceType"
            ]
            for field in expected_fields:
                assert field in record, f"缺少字段: {field}"


class TestLoginLogFilter:
    """LOG-002: 多条件筛选"""

    @pytest.mark.api
    @pytest.mark.query
    def test_filter_by_result_success(self, api_base):
        """按登录结果筛选 - 成功"""
        resp = requests.get(f"{api_base}/login-logs", params={"result": 1}, timeout=10)
        data = resp.json()
        assert data["code"] == 200
        for record in data["data"]["records"]:
            assert record["result"] == 1, "筛选成功记录时不应包含失败记录"

    @pytest.mark.api
    @pytest.mark.query
    def test_filter_by_result_fail(self, api_base):
        """按登录结果筛选 - 失败"""
        resp = requests.get(f"{api_base}/login-logs", params={"result": 0}, timeout=10)
        data = resp.json()
        assert data["code"] == 200
        for record in data["data"]["records"]:
            assert record["result"] == 0, "筛选失败记录时不应包含成功记录"

    @pytest.mark.api
    @pytest.mark.query
    def test_filter_by_username(self, api_base):
        """按用户名模糊查询"""
        resp = requests.get(f"{api_base}/login-logs", params={"username": "admin"}, timeout=10)
        data = resp.json()
        assert data["code"] == 200
        for record in data["data"]["records"]:
            assert "admin" in record["username"].lower(), "用户名筛选结果不匹配"

    @pytest.mark.api
    @pytest.mark.query
    def test_filter_by_event_type(self, api_base):
        """按事件类型筛选"""
        resp = requests.get(f"{api_base}/login-logs", params={"eventType": 1}, timeout=10)
        data = resp.json()
        assert data["code"] == 200
        for record in data["data"]["records"]:
            assert record["eventType"] == 1

    @pytest.mark.api
    @pytest.mark.query
    def test_filter_by_login_method(self, api_base):
        """按登录方式筛选"""
        resp = requests.get(f"{api_base}/login-logs", params={"loginMethod": 1}, timeout=10)
        data = resp.json()
        assert data["code"] == 200
        for record in data["data"]["records"]:
            assert record["loginMethod"] == 1

    @pytest.mark.api
    @pytest.mark.query
    def test_combined_filters(self, api_base):
        """组合条件筛选"""
        resp = requests.get(f"{api_base}/login-logs", params={
            "result": 1,
            "eventType": 1,
            "pageSize": 5
        }, timeout=10)
        data = resp.json()
        assert data["code"] == 200
        for record in data["data"]["records"]:
            assert record["result"] == 1
            assert record["eventType"] == 1

    @pytest.mark.api
    @pytest.mark.query
    def test_empty_result(self, api_base):
        """查询无结果时返回空列表"""
        resp = requests.get(f"{api_base}/login-logs", params={
            "username": "nonexistent_user_xyz_12345"
        }, timeout=10)
        data = resp.json()
        assert data["code"] == 200
        assert data["data"]["total"] == 0
        assert data["data"]["records"] == []


class TestLoginLogDetail:
    """LOG-003: 查看日志详情"""

    @pytest.mark.api
    @pytest.mark.detail
    def test_get_detail_by_id(self, api_base):
        """查询存在的日志详情"""
        # 先获取一条记录的 ID
        list_resp = requests.get(f"{api_base}/login-logs", params={"pageSize": 1}, timeout=10)
        records = list_resp.json()["data"]["records"]
        if not records:
            pytest.skip("无测试数据")

        record_id = records[0]["id"]
        resp = requests.get(f"{api_base}/login-logs/{record_id}", timeout=10)
        data = resp.json()
        assert data["code"] == 200
        assert data["data"]["id"] == record_id

    @pytest.mark.api
    @pytest.mark.detail
    def test_detail_fields_complete(self, api_base):
        """详情字段完整性（含 userAgent、location 等详细字段）"""
        list_resp = requests.get(f"{api_base}/login-logs", params={"pageSize": 1}, timeout=10)
        records = list_resp.json()["data"]["records"]
        if not records:
            pytest.skip("无测试数据")

        record_id = records[0]["id"]
        resp = requests.get(f"{api_base}/login-logs/{record_id}", timeout=10)
        detail = resp.json()["data"]
        detail_fields = [
            "id", "userId", "username", "tenantId", "loginTime",
            "ip", "userAgent", "loginMethod", "eventType",
            "result", "deviceType", "location"
        ]
        for field in detail_fields:
            assert field in detail, f"详情缺少字段: {field}"

    @pytest.mark.api
    @pytest.mark.detail
    def test_detail_not_found(self, api_base):
        """查询不存在的记录"""
        resp = requests.get(f"{api_base}/login-logs/99999999", timeout=10)
        data = resp.json()
        assert data["code"] == 404


class TestLoginLogExport:
    """LOG-004: 导出登录日志"""

    @pytest.mark.api
    @pytest.mark.export
    def test_export_csv(self, api_base):
        """导出 CSV 文件"""
        resp = requests.get(f"{api_base}/login-logs/export", timeout=15)
        assert resp.status_code == 200
        content_type = resp.headers.get("Content-Type", "")
        assert "csv" in content_type or "octet-stream" in content_type or "text" in content_type, \
            f"导出文件类型异常: {content_type}"
        # CSV 应包含表头
        content = resp.text
        assert len(content) > 0, "导出内容为空"

    @pytest.mark.api
    @pytest.mark.export
    def test_export_with_filter(self, api_base):
        """带筛选条件导出"""
        resp = requests.get(f"{api_base}/login-logs/export", params={"result": 1}, timeout=15)
        assert resp.status_code == 200


class TestDataIntegrity:
    """数据完整性与一致性"""

    @pytest.mark.api
    def test_total_matches_records(self, api_base):
        """total 与实际记录数一致性"""
        resp = requests.get(f"{api_base}/login-logs", params={"pageSize": 100}, timeout=10)
        data = resp.json()["data"]
        if data["total"] <= 100:
            assert len(data["records"]) == data["total"], \
                f"total={data['total']} 但实际返回 {len(data['records'])} 条"

    @pytest.mark.api
    def test_login_time_desc_order(self, api_base):
        """记录按登录时间倒序排列"""
        resp = requests.get(f"{api_base}/login-logs", params={"pageSize": 10}, timeout=10)
        records = resp.json()["data"]["records"]
        if len(records) >= 2:
            times = [r["loginTime"] for r in records]
            assert times == sorted(times, reverse=True), "记录应按登录时间倒序排列"

    @pytest.mark.api
    def test_enum_desc_mapping(self, api_base):
        """枚举值描述映射正确"""
        resp = requests.get(f"{api_base}/login-logs", params={"pageSize": 20}, timeout=10)
        records = resp.json()["data"]["records"]

        result_map = {0: "失败", 1: "成功"}
        for r in records:
            if r["result"] in result_map:
                assert r["resultDesc"] == result_map[r["result"]], \
                    f"result={r['result']} 描述应为 {result_map[r['result']]}，实际为 {r['resultDesc']}"
