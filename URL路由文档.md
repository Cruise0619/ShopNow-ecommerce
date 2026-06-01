# ShopNow 电商平台 — 完整 URL 路由文档

> 生成日期: 2026-05-17

---

## 一、前端页面路由（Vue SPA）

开发服务器: `http://localhost:5173`

### 1.1 客户端页面

| 页面 | URL | 组件文件 | 需要登录 |
|------|-----|---------|:---:|
| 首页 | `/` | `src/pages/client/Home.vue` | |
| 登录 | `/login` | `src/pages/client/Login.vue` | |
| 注册 | `/register` | `src/pages/client/Register.vue` | |
| 忘记密码 | `/forgot-password` | `src/pages/client/ForgotPassword.vue` | |
| 商品列表 | `/products` | `src/pages/client/Products.vue` | |
| 商品详情 | `/products/:id` | `src/pages/client/ProductDetail.vue` | |
| 购物车 | `/cart` | `src/pages/client/Cart.vue` | 是 |
| 结算 | `/checkout` | `src/pages/client/Checkout.vue` | 是 |
| 订单列表 | `/orders` | `src/pages/client/Orders.vue` | 是 |
| 订单详情 | `/orders/:id` | `src/pages/client/OrderDetail.vue` | 是 |
| 收藏夹 | `/favorites` | `src/pages/client/Favorites.vue` | 是 |
| 个人中心 | `/profile` | `src/pages/client/Profile.vue` | 是 |
| 收货地址 | `/addresses` | `src/pages/client/Addresses.vue` | 是 |
| 消息中心 | `/messages` | `src/pages/client/Messages.vue` | 是 |

### 1.2 管理后台页面

| 页面 | URL | 组件文件 | 需要管理员 |
|------|-----|---------|:---:|
| 管理员登录 | `/admin/login` | `src/pages/admin/AdminLogin.vue` | |
| 仪表盘 | `/admin/dashboard` | `src/pages/admin/Dashboard.vue` | 是 |
| 用户管理 | `/admin/users` | `src/pages/admin/Users.vue` | 是 |
| 商品管理 | `/admin/products` | `src/pages/admin/Products.vue` | 是 |
| 订单管理 | `/admin/orders` | `src/pages/admin/Orders.vue` | 是 |
| 订单详情 | `/admin/orders/:id` | `src/pages/admin/OrderDetail.vue` | 是 |
| 分类管理 | `/admin/categories` | `src/pages/admin/Categories.vue` | 是 |
| 评论管理 | `/admin/reviews` | `src/pages/admin/Reviews.vue` | 是 |
| Banner管理 | `/admin/banners` | `src/pages/admin/Banners.vue` | 是 |
| 公告管理 | `/admin/announcements` | `src/pages/admin/Announcements.vue` | 是 |
| 优惠券管理 | `/admin/coupons` | `src/pages/admin/Coupons.vue` | 是 |
| 反馈管理 | `/admin/feedbacks` | `src/pages/admin/Feedbacks.vue` | 是 |
| 管理员资料 | `/admin/profile` | `src/pages/admin/AdminProfile.vue` | 是 |

---

## 二、后端 API 接口

基础地址: `http://localhost:3000/api`

### 2.1 认证 — `/api/auth`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| POST | `/api/auth/register` | 用户注册 | |
| POST | `/api/auth/login` | 用户登录 | |
| POST | `/api/auth/send-code` | 发送验证码 | |
| POST | `/api/auth/forgot-password` | 忘记密码 | |
| POST | `/api/auth/reset-password` | 重置密码 | |

### 2.2 商品 — `/api/products`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/products` | 商品列表 | |
| GET | `/api/products/:id` | 商品详情 | |

### 2.3 购物车 — `/api/cart`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/cart` | 获取购物车 | 是 |
| POST | `/api/cart` | 添加商品 | 是 |
| PUT | `/api/cart/:id` | 更新数量 | 是 |
| DELETE | `/api/cart/:id` | 删除商品 | 是 |
| PUT | `/api/cart/select-all` | 全选/取消全选 | 是 |

### 2.4 订单 — `/api/orders`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/orders` | 订单列表 | 是 |
| POST | `/api/orders` | 创建订单 | 是 |
| GET | `/api/orders/:id` | 订单详情 | 是 |
| PUT | `/api/orders/:id/cancel` | 取消订单 | 是 |
| PUT | `/api/orders/:id/confirm` | 确认收货 | 是 |
| PUT | `/api/orders/:id/pay` | 支付订单 | 是 |
| PUT | `/api/orders/:id/refund` | 申请退款 | 是 |

### 2.5 收藏 — `/api/favorites`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/favorites` | 收藏列表 | 是 |
| POST | `/api/favorites/:productId` | 切换收藏 | 是 |

### 2.6 评论 — `/api/reviews`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/reviews` | 评论列表 | |
| POST | `/api/reviews` | 发表评论 | 是 |

### 2.7 收货地址 — `/api/addresses`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/addresses` | 地址列表 | 是 |
| POST | `/api/addresses` | 添加地址 | 是 |
| PUT | `/api/addresses/:id` | 更新地址 | 是 |
| DELETE | `/api/addresses/:id` | 删除地址 | 是 |
| PUT | `/api/addresses/:id/default` | 设为默认地址 | 是 |

### 2.8 优惠券 — `/api/coupons`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/coupons` | 优惠券列表 | |
| POST | `/api/coupons/:id/claim` | 领取优惠券 | 是 |

### 2.9 个人资料 — `/api/profile`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/profile` | 获取资料 | 是 |
| PUT | `/api/profile` | 更新资料 | 是 |
| PUT | `/api/profile/password` | 修改密码 | 是 |

### 2.10 Banner — `/api/banners`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/banners` | Banner 列表 | |

### 2.11 公告 — `/api/announcements`

| 方法 | URL | 说明 | 需要登录 |
|------|-----|------|:---:|
| GET | `/api/announcements` | 公告列表 | |

### 2.12 文件静态访问

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/uploads/*` | 上传的图片等静态文件 |

---

## 三、管理员 API — `/api/admin`

所有接口均需管理员权限。

| 方法 | URL | 说明 |
|------|-----|------|
| GET | `/api/admin/dashboard` | 仪表盘数据 |
| GET | `/api/admin/users` | 用户列表 |
| POST | `/api/admin/users` | 创建用户 |
| GET | `/api/admin/users/:id` | 用户详情 |
| PUT | `/api/admin/users/:id` | 更新用户 |
| DELETE | `/api/admin/users/:id` | 删除用户 |
| PUT | `/api/admin/users/:id/status` | 修改用户状态 |
| GET | `/api/admin/categories` | 分类列表 |
| POST | `/api/admin/categories` | 创建分类 |
| PUT | `/api/admin/categories/:id` | 更新分类 |
| DELETE | `/api/admin/categories/:id` | 删除分类 |
| GET | `/api/admin/products` | 商品列表 |
| POST | `/api/admin/products` | 创建商品 |
| PUT | `/api/admin/products/:id` | 更新商品 |
| DELETE | `/api/admin/products/:id` | 删除商品 |
| GET | `/api/admin/orders` | 订单列表 |
| GET | `/api/admin/orders/:id` | 订单详情 |
| PUT | `/api/admin/orders/:id/ship` | 发货 |
| PUT | `/api/admin/orders/:id/cancel` | 取消订单 |
| PUT | `/api/admin/orders/:id/refund` | 处理退款 |
| GET | `/api/admin/orders/export` | 导出订单 |
| GET | `/api/admin/reviews` | 评论列表 |
| DELETE | `/api/admin/reviews/:id` | 删除评论 |
| GET | `/api/admin/banners` | Banner 列表 |
| POST | `/api/admin/banners` | 创建 Banner |
| PUT | `/api/admin/banners/:id` | 更新 Banner |
| DELETE | `/api/admin/banners/:id` | 删除 Banner |
| GET | `/api/admin/announcements` | 公告列表 |
| POST | `/api/admin/announcements` | 创建公告 |
| PUT | `/api/admin/announcements/:id` | 更新公告 |
| DELETE | `/api/admin/announcements/:id` | 删除公告 |
| GET | `/api/admin/coupons` | 优惠券列表 |
| POST | `/api/admin/coupons` | 创建优惠券 |
| PUT | `/api/admin/coupons/:id` | 更新优惠券 |
| DELETE | `/api/admin/coupons/:id` | 删除优惠券 |
| GET | `/api/admin/feedbacks` | 反馈列表 |
| PUT | `/api/admin/feedbacks/:id/reply` | 回复反馈 |

---

## 汇总

| 类别 | 数量 |
|------|:---:|
| 前端客户端页面 | 14 |
| 前端管理员页面 | 13 |
| 公开 API | 12 |
| 需登录 API | 19 |
| 管理员 API | 38 |
| **API 总计** | **69** |
| **前端页面总计** | **27** |
