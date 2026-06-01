# ShopNow 电商平台

基于 Vue3 + Element Plus 前端 / Node.js + Express 后端 / MySQL 数据库的全栈电商平台。

## 快速启动

### 1. 数据库准备
确保 MySQL 已运行。在 `.env` 中配置数据库连接（默认数据库名：`ecommerce`）。

创建数据库：
```sql
CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARSET utf8mb4;
```

### 2. 后端启动
```bash
cd ecommerce-backend
npm install
npm run seed    # 初始化数据库 + 种子数据（~300条记录）
npm run dev     # 启动开发服务器 http://localhost:3000
```

### 3. 前端启动
```bash
cd ecommerce-frontend
npm install
npm run dev     # 启动开发服务器 http://localhost:5173
```

### 4. 测试
```bash
# 后端测试 (~150 API测试用例)
cd ecommerce-backend && npm test

# 前端测试 (~150 单元测试用例)
cd ecommerce-frontend && npm test
```

## 测试账号

| 角色 | 用户名 | 密码 | 访问地址 |
|------|--------|------|----------|
| 管理员 | admin | admin123 | http://localhost:5173/admin/login |
| 普通用户 | user1 | 123456 | http://localhost:5173/login |

## 项目结构

```
├── ecommerce-backend/          # 后端 Node.js/Express
│   ├── src/
│   │   ├── config/             # 数据库、JWT 配置
│   │   ├── models/             # 14 个数据模型
│   │   ├── controllers/        # 请求处理
│   │   ├── routes/             # API 路由
│   │   ├── middleware/         # 认证、上传、错误处理
│   │   └── app.js              # 入口文件
│   ├── seed/                   # 种子数据脚本
│   └── tests/                  # Jest 测试
├── ecommerce-frontend/         # 前端 Vue3 + Element Plus
│   ├── src/
│   │   ├── api/                # API 封装
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── pages/client/       # 客户端页面（15页）
│   │   ├── pages/admin/        # 管理后台页面（13页）
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   └── components/         # 公共组件
│   └── vitest.config.js        # Vitest 配置
└── ecommerce.code-workspace    # VS Code 工作区
```

## 技术栈

- **前端**: Vue 3, Element Plus, Pinia, Vue Router, Axios, ECharts
- **后端**: Node.js, Express, Sequelize, JWT, bcrypt, Multer
- **数据库**: MySQL (14 tables)
- **测试**: Jest + Supertest (后端), Vitest (前端)

## 功能覆盖

### 客户端
- 用户注册/登录/找回密码
- 首页轮播 + 分类导航 + 推荐商品
- 商品搜索/筛选/排序/分页
- 商品详情 + 规格选择 + 评价
- 购物车增删改全选
- 订单创建/支付/取消/确认收货
- 收藏管理
- 收货地址管理
- 优惠券领取
- 个人信息修改

### 管理后台
- 数据看板（统计 + ECharts图表）
- 用户管理（列表/搜索/启禁用）
- 商品管理（CRUD + 图片上传）
- 分类管理（树形结构 CRUD）
- 订单管理（查看/发货/取消/退款/导出Excel）
- 评价管理（查看/删除）
- 轮播/公告/优惠券/反馈管理
