# 货运平台系统 - Sprint 01

## 项目概述

本项目为货运平台系统，实现了多仓库支持功能。

技术栈：
- Frontend: Vue3 + Vite
- Backend: Spring Boot 2.7.18
- Database: MySQL 8
- Build: Maven

## 项目结构

```
pl-3/
├── backend/                    # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/freight/
│   │   │   │   ├── controller/    # 控制器
│   │   │   │   ├── service/       # 业务逻辑
│   │   │   │   ├── mapper/        # MyBatis Mapper
│   │   │   │   ├── entity/        # 实体类
│   │   │   │   ├── dto/           # 数据传输对象
│   │   │   │   └── common/        # 通用类
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/                  # 单元测试
│   ├── sql/                       # 数据库脚本
│   └── pom.xml
├── frontend/                   # Vue3 前端
│   ├── src/
│   │   ├── api/               # API 接口
│   │   ├── views/             # 页面组件
│   │   └── router/            # 路由配置
│   ├── package.json
│   └── vite.config.js
└── sprint-01/                 # Sprint 01 任务文档
```

## 快速开始

### 数据库初始化

```bash
# 连接 MySQL
mysql -h 127.0.0.1 -u root -p123456

# 创建数据库
CREATE DATABASE freight;
USE freight;

# 执行初始化脚本
source backend/sql/V1__init_order.sql;
source backend/sql/V2__create_warehouse.sql;

# 插入测试数据
INSERT INTO warehouse (name, location) VALUES ('北京仓', '北京市朝阳区');
INSERT INTO warehouse (name, location) VALUES ('上海仓', '上海市浦东新区');
```

### 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:5173 启动

## API 接口

### 创建订单
```
POST /api/order/create
Content-Type: application/json

{
  "userId": 1,
  "totalPrice": 100.00,
  "warehouseId": 1
}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "totalPrice": 100.00,
    "warehouseId": 1
  }
}
```

### 获取仓库列表
```
GET /api/warehouse/list

Response:
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "北京仓",
      "location": "北京市朝阳区"
    }
  ]
}
```

## 测试

### 运行后端测试
```bash
cd backend
mvn test
```

测试覆盖：
- OrderServiceTest: 3 个测试用例
- OrderControllerTest: 3 个测试用例

测试场景：
1. 正常创建订单
2. warehouse_id 为空时报错
3. warehouse 不存在时报错

## Sprint 01 完成情况

✅ 数据库：创建 warehouse 表，order 表增加 warehouse_id
✅ 后端：实现订单创建接口，增加仓库校验逻辑
✅ 前端：订单创建页增加仓库下拉框
✅ 测试：6 个单元测试全部通过

## 配置说明

### 数据库配置
- Host: 127.0.0.1
- User: root
- Password: 123456
- Database: freight

### Jenkins 配置
- Host: 127.0.0.1
- User: chris
- Password: alex993

## 开发规范

1. 所有数据库字段变更必须输出完整表结构
2. 不允许只给增量 SQL
3. 所有接口必须返回统一结构：{code, message, data}
4. 前端接口路径统一 /api/*
5. 所有变更必须在 sprint 目录内记录
