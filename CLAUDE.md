# Claude Global Rules

## 项目背景

本项目为货运平台系统，技术栈：

- Frontend: Vue3 + Vite
- Backend: Spring Boot
- DB: MySQL 8

## 组件配置参数

- mysql
  host: 127.0.0.1
  user: root
  password: 123456

- jenkins
  host: 127.0.0.1
  user: chris
  password: alex993

## 统一规则

1.  所有数据库字段变更必须输出完整表结构
2.  不允许只给增量 SQL
3.  所有接口必须返回统一结构：
    {
    code: number,
    message: string,
    data: object
    }
4.  前端接口路径统一 /api/\*
5.  所有变更必须在 sprint 目录内记录

## 多会话协作规则

- FE 只修改 fe-task.md
- BE 只修改 be-task.md
- 架构师只修改 db-change.md
- 所有结构必须以 architecture.md 为准
