# 宠物领养平台 API 文档

## 目录

1. [用户管理 API](#用户管理-api)
2. [宠物管理 API](#宠物管理-api)
3. [宠物信息查询 API](#宠物信息查询-api)
4. [宠物记录 API](#宠物记录-api)
5. [寄养管理 API](#寄养管理-api)
6. [收容所管理 API](#收容所管理-api)
7. [文章管理 API](#文章管理-api)
8. [健康提醒 API](#健康提醒-api)
9. [登录历史 API](#登录历史-api)

---

## 用户管理 API

### 1. 用户注册

**接口地址：** `POST /user`

**功能描述：** 用户注册新账号

**请求参数：**
```json
{
  "username": "用户名",
  "password": "密码",
  "email": "邮箱",
  "phone": "手机号"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

**测试方法：**
```bash
curl -X POST http://localhost:8080/api/user \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","email":"test@example.com","phone":"13800138000"}'
```

**注意：** 在前端代码中，由于Vite代理配置，实际请求路径为 `/api/users`，Vite会将其代理到 `http://localhost:8080/api/users`

---

### 2. 用户登录

**接口地址：** `POST /user/login`

**功能描述：** 用户登录获取访问令牌

**请求参数：**
```json
{
  "username": "用户名",
  "password": "密码"
}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 3600
  }
}
```

**测试方法：**
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'
```

---

### 3. 获取用户个人资料

**接口地址：** `GET /user/profile`

**功能描述：** 获取当前登录用户的个人资料

**请求头：**
```
Authorization: Bearer {token}
```

**响应示例：**
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com",
    "phone": "13800138000",
    "avatar": "avatar_url",
    "createTime": "2023-01-01T00:00:00"
  }
}
```

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer your_token_here"
```

---

### 4. 更新用户个人资料

**接口地址：** `PUT /user/profile`

**功能描述：** 更新当前登录用户的个人资料

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
```json
{
  "email": "新邮箱",
  "phone": "新手机号",
  "avatar": "新头像URL"
}
```

**测试方法：**
```bash
curl -X PUT http://localhost:8080/api/user/profile \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{"email":"newemail@example.com","phone":"13900139000"}'
```

---

### 5. 更改密码

**接口地址：** `PUT /user/updatePassword`

**功能描述：** 更改当前登录用户的密码

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
```json
{
  "oldPassword": "旧密码",
  "newPassword": "新密码"
}
```

**测试方法：**
```bash
curl -X PUT http://localhost:8080/api/user/updatePassword \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{"oldPassword":"123456","newPassword":"654321"}'
```

---

### 6. 获取用户领养记录

**接口地址：** `GET /user/adoptions`

**功能描述：** 获取当前用户的宠物领养记录

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/user/adoptions \
  -H "Authorization: Bearer your_token_here"
```

---

### 7. 获取用户寄养记录

**接口地址：** `GET /user/fosters`

**功能描述：** 获取当前用户的宠物寄养记录

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/user/fosters \
  -H "Authorization: Bearer your_token_here"
```

---

### 8. 刷新令牌

**接口地址：** `POST /user/refresh-token`

**功能描述：** 使用刷新令牌获取新的访问令牌

**请求参数：**
```json
{
  "refreshToken": "刷新令牌"
}
```

**测试方法：**
```bash
curl -X POST http://localhost:8080/api/user/refresh-token \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"your_refresh_token_here"}'
```

---

## 宠物管理 API

### 1. 领养宠物

**接口地址：** `POST /pets/adopt`

**功能描述：** 领养指定的宠物

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
```json
{
  "petId": 1,
  "adoptionReason": "领养原因",
  "adoptionDate": "2023-01-01"
}
```

**测试方法：**
```bash
curl -X POST http://localhost:8080/api/pets/adopt \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{"petId":1,"adoptionReason":"喜欢小动物","adoptionDate":"2023-01-01"}'
```

---

### 2. 寄养宠物

**接口地址：** `POST /pets/{petId}/foster`

**功能描述：** 申请寄养指定的宠物

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
```json
{
  "fosterReason": "寄养原因",
  "fosterDuration": "寄养时长",
  "fosterStartDate": "2023-01-01"
}
```

**测试方法：**
```bash
curl -X POST http://localhost:8080/api/pets/1/foster \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{"fosterReason":"临时照顾","fosterDuration":"1个月","fosterStartDate":"2023-01-01"}'
```

---

### 3. 删除寄养记录

**接口地址：** `DELETE /fosters/delete/{id}`

**功能描述：** 删除指定的寄养记录

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X DELETE http://localhost:8080/api/fosters/delete/1 \
  -H "Authorization: Bearer your_token_here"
```

---

### 4. 结束寄养

**接口地址：** `POST /fosters/{id}/end`

**功能描述：** 结束指定宠物的寄养

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X POST http://localhost:8080/api/fosters/1/end \
  -H "Authorization: Bearer your_token_here"
```

---

### 5. 获取宠物领养时间线

**接口地址：** `GET /pets/{petId}/adoption-timeline`

**功能描述：** 获取指定宠物的领养时间线信息

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/pets/1/adoption-timeline
```

---

### 6. 更新宠物信息

**接口地址：** `PUT /pets/{petId}`

**功能描述：** 更新指定宠物的信息

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
```json
{
  "petName": "新宠物名",
  "petAge": 2,
  "petGender": "雄性",
  "petType": "狗",
  "petDescription": "宠物描述"
}
```

**测试方法：**
```bash
curl -X PUT http://localhost:8080/api/pets/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{"petName":"小白","petAge":2,"petGender":"雄性","petType":"狗","petDescription":"可爱的狗狗"}'
```

---

### 7. 获取宠物详情

**接口地址：** `GET /pets/details/{id}`

**功能描述：** 获取指定宠物的详细信息

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/pets/details/1
```

---

## 宠物信息查询 API

### 1. 分页查询宠物列表

**接口地址：** `GET /pets/info/available`

**功能描述：** 分页查询可领养的宠物列表

**请求参数：**
- `current_page`: 当前页码 (必需)
- `per_page`: 每页数量 (必需)

**测试方法：**
```bash
curl -X GET "http://localhost:8080/api/pets/info/available?current_page=1&per_page=10"
```

---

### 2. 根据ID获取宠物详情

**接口地址：** `GET /pets/info/available/{petId}`

**功能描述：** 根据宠物ID获取宠物详情

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/pets/info/available/1
```

---

## 宠物记录 API

### 1. 获取用户事件记录列表

**接口地址：** `GET /events`

**功能描述：** 获取当前用户的事件记录列表

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/events \
  -H "Authorization: Bearer your_token_here"
```

---

### 2. 创建事件记录

**接口地址：** `POST /events`

**功能描述：** 创建新的事件记录

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
```json
{
  "eventType": "事件类型",
  "eventDescription": "事件描述",
  "eventDate": "2023-01-01"
}
```

**测试方法：**
```bash
curl -X POST http://localhost:8080/api/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{"eventType":"喂食","eventDescription":"给宠物喂食","eventDate":"2023-01-01"}'
```

---

### 3. 更新事件记录

**接口地址：** `PUT /events/{eventId}`

**功能描述：** 更新指定的事件记录

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
```json
{
  "eventType": "新事件类型",
  "eventDescription": "新事件描述",
  "eventDate": "2023-01-02"
}
```

**测试方法：**
```bash
curl -X PUT http://localhost:8080/api/events/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{"eventType":"洗澡","eventDescription":"给宠物洗澡","eventDate":"2023-01-02"}'
```

---

### 4. 删除事件记录

**接口地址：** `DELETE /events/{eventId}`

**功能描述：** 删除指定的事件记录

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X DELETE http://localhost:8080/api/events/1 \
  -H "Authorization: Bearer your_token_here"
```

---

## 寄养管理 API

### 1. 删除寄养记录

**接口地址：** `DELETE /fosters/delete/{id}`

**功能描述：** 删除指定的寄养记录

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X DELETE http://localhost:8080/api/fosters/delete/1 \
  -H "Authorization: Bearer your_token_here"
```

---

## 收容所管理 API

### 1. 获取所有收容所列表

**接口地址：** `GET /shelters`

**功能描述：** 获取所有收容所的列表信息

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/shelters
```

---

## 文章管理 API

### 1. 分页获取文章列表

**接口地址：** `GET /articles`

**功能描述：** 分页获取文章列表

**请求参数：**
- `currentPage`: 当前页码 (必需)
- `pageSize`: 每页数量 (必需)

**测试方法：**
```bash
curl -X GET "http://localhost:8080/api/articles?currentPage=1&pageSize=10"
```

**注意：** 此接口在后端代码中尚未实现，仅作为API文档参考

---

### 2. 获取文章详情

**接口地址：** `GET /articles/{id}`

**功能描述：** 根据ID获取文章详情

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/articles/1
```

**注意：** 此接口在后端代码中尚未实现，仅作为API文档参考

---

## 健康提醒 API

### 1. 获取用户的健康提醒列表

**接口地址：** `GET /user/health-alerts`

**功能描述：** 获取当前用户的健康提醒列表

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/user/health-alerts \
  -H "Authorization: Bearer your_token_here"
```

---

### 2. 更新健康提醒状态

**接口地址：** `PUT /user/health-alerts/{healthId}/status`

**功能描述：** 更新指定健康提醒的状态

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
- `status`: 新状态 (必需)

**测试方法：**
```bash
curl -X PUT "http://localhost:8080/api/user/health-alerts/1/status?status=completed" \
  -H "Authorization: Bearer your_token_here"
```

**注意：** 此接口在后端代码中尚未实现，仅作为API文档参考

---

### 3. 创建健康提醒

**接口地址：** `POST /user/health-alerts`

**功能描述：** 创建新的健康提醒

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
```json
{
  "alertType": "提醒类型",
  "alertDescription": "提醒描述",
  "alertDate": "2023-01-01",
  "alertTime": "10:00"
}
```

**测试方法：**
```bash
curl -X POST http://localhost:8080/api/user/health-alerts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{"alertType":"疫苗接种","alertDescription":"宠物疫苗接种提醒","alertDate":"2023-01-01","alertTime":"10:00"}'
```

**注意：** 此接口在后端代码中尚未实现，仅作为API文档参考

---

### 4. 更新健康提醒

**接口地址：** `PUT /user/health-alerts/{healthId}`

**功能描述：** 更新指定的健康提醒

**请求头：**
```
Authorization: Bearer {token}
```

**请求参数：**
```json
{
  "alertType": "新提醒类型",
  "alertDescription": "新提醒描述",
  "alertDate": "2023-01-02",
  "alertTime": "11:00"
}
```

**测试方法：**
```bash
curl -X PUT http://localhost:8080/api/user/health-alerts/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token_here" \
  -d '{"alertType":"体检","alertDescription":"宠物体检提醒","alertDate":"2023-01-02","alertTime":"11:00"}'
```

**注意：** 此接口在后端代码中尚未实现，仅作为API文档参考

---

### 5. 删除健康提醒

**接口地址：** `DELETE /user/health-alerts/{healthId}`

**功能描述：** 删除指定的健康提醒

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X DELETE http://localhost:8080/api/user/health-alerts/1 \
  -H "Authorization: Bearer your_token_here"
```

**注意：** 此接口在后端代码中尚未实现，仅作为API文档参考

---

## 登录历史 API

### 1. 获取登录历史记录

**接口地址：** `GET /login-history`

**功能描述：** 获取当前用户的登录历史记录

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X GET http://localhost:8080/api/login-history \
  -H "Authorization: Bearer your_token_here"
```

---

### 2. 清除登录历史记录

**接口地址：** `DELETE /login-history/clear`

**功能描述：** 清除当前用户的登录历史记录

**请求头：**
```
Authorization: Bearer {token}
```

**测试方法：**
```bash
curl -X DELETE http://localhost:8080/api/login-history/clear \
  -H "Authorization: Bearer your_token_here"
```

---

## 通用响应格式

所有API响应都遵循以下统一格式：

```json
{
  "code": 200,      // 状态码：200表示成功，其他表示失败
  "msg": "成功",    // 响应消息
  "data": {}        // 响应数据，具体结构根据接口而定
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，需要登录 |
| 403 | 禁止访问，权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 测试工具推荐

1. **Postman**: 图形化API测试工具，支持保存请求集合和环境变量
2. **cURL**: 命令行工具，适合快速测试
3. **Insomnia**: 另一款流行的API测试工具
4. **Swagger UI**: 如果项目集成了Swagger，可以直接在浏览器中测试

## 测试注意事项

1. **认证**: 大部分API需要先登录获取token，然后在请求头中添加`Authorization: Bearer {token}`
2. **参数验证**: 确保请求参数格式正确，特别是日期格式
3. **错误处理**: 注意检查API返回的错误码和错误信息
4. **跨域**: 如果前端和后端部署在不同域名，需要配置CORS
5. **环境**: 确保测试环境与API文档中的地址一致，或根据实际情况调整