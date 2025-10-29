# 宠物管理系统

一个基于Vue.js + Flask的全栈宠物信息管理系统，支持用户管理、宠物管理、数据统计等功能。

## 功能特性

### 🐱 宠物管理
- 宠物信息录入（名称、种类、品种、年龄、性别、颜色、体重、描述）
- 宠物信息编辑和删除
- 宠物列表展示和搜索
- 宠物分类统计（按种类统计）

### 👥 用户管理
- 用户注册和登录
- 管理员权限管理
- 用户列表管理
- 用户权限切换（管理员/普通用户）

### 📊 数据统计
- 用户总数统计
- 管理员数量统计
- 宠物总数统计
- 今日新增用户统计

### 🔒 安全特性
- JWT用户认证
- 密码哈希加密存储
- 用户权限验证
- 管理员后台管理

## 技术栈

### 前端
- Vue.js 3 + TypeScript
- Element Plus UI组件库
- Vue Router路由管理
- Axios HTTP客户端

### 后端
- Flask Web框架
- SQLAlchemy ORM
- MySQL数据库
- JWT认证
- CORS跨域支持

## 项目结构

```
pet-project/
├── src/
│   ├── api/                    # 后端API
│   │   ├── db.py              # 数据库配置
│   │   ├── models.py          # 数据模型
│   │   ├── login.py           # 用户认证API
│   │   ├── pets.py            # 宠物管理API
│   │   └── main.py            # 主应用文件
│   ├── views/                  # 前端页面
│   │   ├── welcome.vue        # 欢迎页面
│   │   ├── home.vue           # 宠物管理页面
│   │   ├── login.vue          # 登录页面
│   │   ├── register.vue       # 注册页面
│   │   └── admin/             # 管理员页面
│   │       ├── dashboard.vue  # 数据统计
│   │       ├── users.vue      # 用户管理
│   │       └── pets.vue       # 宠物管理
│   ├── router/                 # 路由配置
│   └── components/             # 公共组件
├── requirements.txt            # Python依赖
└── package.json               # Node.js依赖
```

## 快速开始

### 环境要求
- Python 3.8+
- Node.js 16+
- MySQL 5.7+

### 后端配置
1. 安装Python依赖：
```bash
pip install -r requirements.txt
```

2. 配置MySQL数据库，修改`src/api/db.py`中的数据库连接信息

3. 初始化管理员账户：
```bash
cd src/api
python init_admin.py
```

4. 启动后端服务：
```bash
python main.py
```

### 前端配置
1. 安装Node.js依赖：
```bash
npm install
```

2. 启动前端开发服务器：
```bash
npm run dev
```

3. 访问 http://localhost:5174 查看应用

## 默认账户
- 管理员用户名：admin
- 管理员密码：admin123

## API接口

### 用户认证
- POST `/login` - 用户登录
- POST `/register` - 用户注册
- POST `/admin/register` - 管理员注册

### 用户管理
- GET `/login/admin/users` - 获取用户列表
- PUT `/login/admin/users/{id}/toggle-admin` - 切换管理员权限
- DELETE `/login/admin/users/{id}` - 删除用户
- POST `/login/admin/register-user` - 管理员添加用户

### 宠物管理
- GET `/pets/pets` - 获取宠物列表
- POST `/pets/pets` - 添加宠物
- PUT `/pets/pets/{id}` - 更新宠物信息
- DELETE `/pets/pets/{id}` - 删除宠物

### 数据统计
- GET `/login/admin/stats` - 获取统计数据

## 主要功能演示

### 1. 欢迎页面
- 系统介绍和功能展示
- 实时统计数据展示
- 快速导航到登录/注册

### 2. 用户登录/注册
- 用户账户创建
- 安全的身份验证
- 记住登录状态

### 3. 宠物管理
- 宠物信息录入和编辑
- 宠物列表展示
- 按种类搜索和筛选
- 宠物删除功能

### 4. 用户管理（管理员）
- 用户列表查看
- 管理员权限设置
- 用户账户删除
- 新用户添加

### 5. 数据统计（管理员）
- 用户总数统计
- 宠物总数统计
- 管理员数量统计
- 今日新增用户

## 开发说明

### 前端开发
- 使用Vue 3 Composition API
- Element Plus组件库
- TypeScript类型安全
- 响应式设计

### 后端开发
- RESTful API设计
- SQLAlchemy数据库操作
- JWT用户认证
- 错误处理和验证

## 安全考虑
- 密码使用哈希加密存储
- JWT令牌认证
- CORS跨域配置
- 输入验证和错误处理

## 后续改进
- 宠物图片上传功能
- 宠物健康记录管理
- 宠物疫苗接种记录
- 用户个人中心
- 宠物分类标签系统
- 数据导出功能

## 许可证
MIT License
