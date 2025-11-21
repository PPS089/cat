# 宠物管理系统 API 文档

## 1. 用户管理

### 1.1 用户注册
#### 1.1.1 基本信息
- 请求路径：/user
- 请求方式：POST
- 接口描述：该接口用于用户注册

#### 1.1.2 请求参数
参数格式：application/json
```json
{
  "userName": "用户名",
  "password": "密码",
  "phone": "手机号",
  "email": "邮箱"
}
```

#### 1.1.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "userId": 1,
    "userName": "用户名",
    "token": "JWT令牌",
    "ok": true,
    "message": "注册成功"
  }
}
```

### 1.2 用户登录
#### 1.2.1 基本信息
- 请求路径：/user/login
- 请求方式：POST
- 接口描述：该接口用于用户登录

#### 1.2.2 请求参数
参数格式：application/json
```json
{
  "userName": "用户名",
  "password": "密码"
}
```

#### 1.2.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "userId": 1,
    "userName": "用户名",
    "token": "JWT令牌",
    "ok": true,
    "message": "登录成功"
  }
}
```

### 1.3 获取用户信息
#### 1.3.1 基本信息
- 请求路径：/user/profile
- 请求方式：GET
- 接口描述：该接口用于获取当前登录用户的详细信息

#### 1.3.2 请求参数
无

#### 1.3.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "userId": 1,
    "userName": "用户名",
    "phone": "手机号",
    "email": "邮箱",
    "introduce": "个人介绍",
    "avatar": "头像URL",
    "createTime": "2023-01-01T00:00:00",
    "updateTime": "2023-01-01T00:00:00"
  }
}
```

### 1.4 更新用户信息
#### 1.4.1 基本信息
- 请求路径：/user/profile
- 请求方式：PUT
- 接口描述：该接口用于更新当前登录用户的信息

#### 1.4.2 请求参数
参数格式：multipart/form-data
```
avatar: 图片文件(可选)
userName: 用户名(可选)
phone: 手机号(可选)
introduce: 个人介绍(可选)
email: 邮箱(可选)
```

#### 1.4.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "userId": 1,
    "userName": "用户名",
    "phone": "手机号",
    "email": "邮箱",
    "introduce": "个人介绍",
    "avatar": "头像URL",
    "createTime": "2023-01-01T00:00:00",
    "updateTime": "2023-01-01T00:00:00"
  }
}
```

### 1.5 修改密码
#### 1.5.1 基本信息
- 请求路径：/user/updatePassword
- 请求方式：PUT
- 接口描述：该接口用于修改当前登录用户的密码

#### 1.5.2 请求参数
参数格式：application/json
```json
{
  "oldPassword": "原密码",
  "newPassword": "新密码"
}
```

#### 1.5.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": "密码更新成功"
}
```

### 1.6 获取用户领养记录
#### 1.6.1 基本信息
- 请求路径：/user/adoptions
- 请求方式：GET
- 接口描述：该接口用于分页获取当前用户的领养记录

#### 1.6.2 请求参数
参数格式：query string
```
current_page: 当前页码(默认1)
per_page: 每页数量(默认10，最大100)
```

#### 1.6.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "petId": 1,
        "petName": "宠物名称",
        "adoptionDate": "2023-01-01T00:00:00",
        "status": "已领养"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 1.7 获取用户寄养记录
#### 1.7.1 基本信息
- 请求路径：/user/fosters
- 请求方式：GET
- 接口描述：该接口用于分页获取当前用户的寄养记录

#### 1.7.2 请求参数
参数格式：query string
```
current_page: 当前页码(默认1)
per_page: 每页数量(默认10，最大100)
```

#### 1.7.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "petId": 1,
        "petName": "宠物名称",
        "shelterId": 1,
        "shelterName": "收容所名称",
        "startDate": "2023-01-01T00:00:00",
        "endDate": "2023-01-10T00:00:00",
        "status": "已结束"
      }
    ],
    "total": 10,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 1.8 刷新Token
#### 1.8.1 基本信息
- 请求路径：/user/refresh-token
- 请求方式：POST
- 接口描述：该接口用于刷新JWT令牌

#### 1.8.2 请求参数
无

#### 1.8.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "userId": 1,
    "userName": "用户名",
    "token": "新的JWT令牌",
    "ok": true,
    "message": "刷新成功"
  }
}
```

## 2. 宠物管理

### 2.1 分页查询可领养宠物列表
#### 2.1.1 基本信息
- 请求路径：/pets/info/available
- 请求方式：GET
- 接口描述：该接口用于分页查询可领养的宠物列表

#### 2.1.2 请求参数
参数格式：query string
```
current_page: 当前页码
per_page: 每页数量
breed: 品种(可选)
gender: 性别(male/female，可选)
minAge: 最小年龄(可选)
maxAge: 最大年龄(可选)
```

#### 2.1.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "records": [
      {
        "pid": 1,
        "name": "宠物名称",
        "species": "物种",
        "breed": "品种",
        "age": 2,
        "gender": "性别",
        "imgUrl": "图片URL",
        "status": "UNADOPTED",
        "shelterName": "收容所名称",
        "shelterAddress": "收容所地址"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

### 2.2 获取宠物详情
#### 2.2.1 基本信息
- 请求路径：/pets/info/available/{petId}
- 请求方式：GET
- 接口描述：该接口用于根据ID获取可领养宠物的详细信息

#### 2.2.2 请求参数
路径参数：
```
petId: 宠物ID
```

#### 2.2.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "pid": 1,
    "name": "宠物名称",
    "species": "物种",
    "breed": "品种",
    "age": 2,
    "gender": "性别",
    "imgUrl": "图片URL",
    "status": "UNADOPTED",
    "description": "宠物描述",
    "healthStatus": "健康状态",
    "vaccinated": true,
    "neutered": false,
    "adoptionFee": 200,
    "fosterFee": 50,
    "shelterId": 1,
    "shelterName": "收容所名称",
    "shelterAddress": "收容所地址"
  }
}
```

### 2.3 领养宠物
#### 2.3.1 基本信息
- 请求路径：/pets/adopt
- 请求方式：POST
- 接口描述：该接口用于领养指定ID的宠物

#### 2.3.2 请求参数
参数格式：query string
```
petId: 宠物ID
```

#### 2.3.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "adoptionId": 1,
    "petId": 1,
    "userId": 1,
    "adoptionDate": "2023-01-01T00:00:00",
    "status": "已领养"
  }
}
```

### 2.4 寄养宠物
#### 2.4.1 基本信息
- 请求路径：/pets/{petId}/foster
- 请求方式：POST
- 接口描述：该接口用于将宠物寄养到指定的收容所

#### 2.4.2 请求参数
路径参数：
```
petId: 宠物ID
```

请求体：
```json
{
  "shelterId": 1,
  "startDate": "2023-01-01"
}
```

#### 2.4.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "petId": 1,
    "shelterId": 1,
    "shelterName": "收容所名称",
    "startDate": "2023-01-01T00:00:00",
    "endDate": null,
    "status": "寄养中"
  }
}
```

### 2.5 结束宠物寄养
#### 2.5.1 基本信息
- 请求路径：/pets/{petId}/foster/end
- 请求方式：POST
- 接口描述：该接口用于结束指定宠物的寄养状态

#### 2.5.2 请求参数
路径参数：
```
petId: 宠物ID
```

#### 2.5.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "petId": 1,
    "endDate": "2023-01-10T00:00:00"
  }
}
```

### 2.6 获取宠物领养时间线
#### 2.6.1 基本信息
- 请求路径：/pets/{petId}/adoption-timeline
- 请求方式：GET
- 接口描述：该接口用于获取指定宠物的领养过程时间线

#### 2.6.2 请求参数
路径参数：
```
petId: 宠物ID
```

#### 2.6.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "petId": 1,
    "events": [
      {
        "eventType": "领养申请",
        "eventDate": "2023-01-01T00:00:00",
        "description": "用户提交领养申请"
      },
      {
        "eventType": "领养批准",
        "eventDate": "2023-01-02T00:00:00",
        "description": "领养申请已批准"
      }
    ]
  }
}
```

### 2.7 更新宠物信息
#### 2.7.1 基本信息
- 请求路径：/pets/{petId}
- 请求方式：PUT
- 接口描述：该接口用于更新指定宠物的信息

#### 2.7.2 请求参数
路径参数：
```
petId: 宠物ID
```

请求体：
```json
{
  "name": "宠物名称",
  "breed": "品种",
  "age": 2,
  "gender": "性别",
  "species": "物种",
  "imgUrl": "图片URL",
  "status": "状态",
  "shelterId": 1
}
```

#### 2.7.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "pid": 1,
    "name": "宠物名称",
    "species": "物种",
    "breed": "品种",
    "age": 2,
    "gender": "性别",
    "imgUrl": "图片URL",
    "status": "状态",
    "shelterName": "收容所名称",
    "shelterAddress": "收容所地址"
  }
}
```

### 2.8 获取宠物详情(领养界面)
#### 2.8.1 基本信息
- 请求路径：/pets/details/{id}
- 请求方式：GET
- 接口描述：该接口用于根据ID获取宠物的详细信息(领养界面)

#### 2.8.2 请求参数
路径参数：
```
id: 宠物ID
```

#### 2.8.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "pid": 1,
    "name": "宠物名称",
    "species": "物种",
    "breed": "品种",
    "age": 2,
    "gender": "性别",
    "imgUrl": "图片URL",
    "status": "状态",
    "description": "宠物描述",
    "healthStatus": "健康状态",
    "vaccinated": true,
    "neutered": false,
    "adoptionFee": 200,
    "fosterFee": 50,
    "shelterId": 1,
    "shelterName": "收容所名称",
    "shelterAddress": "收容所地址"
  }
}
```

## 3. 宠物事件记录

### 3.1 获取用户的事件记录列表
#### 3.1.1 基本信息
- 请求路径：/events
- 请求方式：GET
- 接口描述：该接口用于获取当前登录用户的所有宠物事件记录列表

#### 3.1.2 请求参数
无

#### 3.1.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "petId": 1,
      "petName": "宠物名称",
      "eventType": "事件类型",
      "eventDate": "2023-01-01T00:00:00",
      "description": "事件描述",
      "mediaFiles": [
        {
          "id": 1,
          "fileName": "文件名",
          "fileUrl": "文件URL",
          "fileType": "文件类型"
        }
      ]
    }
  ]
}
```

### 3.2 创建事件记录
#### 3.2.1 基本信息
- 请求路径：/events
- 请求方式：POST
- 接口描述：该接口用于创建一个新的宠物事件记录

#### 3.2.2 请求参数
参数格式：application/json
```json
{
  "pid": 1,
  "eventType": "事件类型",
  "eventDate": "2023-01-01T00:00:00",
  "description": "事件描述"
}
```

#### 3.2.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "petId": 1,
    "petName": "宠物名称",
    "eventType": "事件类型",
    "eventDate": "2023-01-01T00:00:00",
    "description": "事件描述",
    "mediaFiles": []
  }
}
```

### 3.3 更新事件记录
#### 3.3.1 基本信息
- 请求路径：/events/{eventId}
- 请求方式：PUT
- 接口描述：该接口用于更新指定ID的宠物事件记录

#### 3.3.2 请求参数
路径参数：
```
eventId: 事件记录ID
```

请求体：
```json
{
  "pid": 1,
  "eventType": "事件类型",
  "eventDate": "2023-01-01T00:00:00",
  "description": "事件描述"
}
```

#### 3.3.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "petId": 1,
    "petName": "宠物名称",
    "eventType": "事件类型",
    "eventDate": "2023-01-01T00:00:00",
    "description": "事件描述",
    "mediaFiles": []
  }
}
```

### 3.4 删除事件记录
#### 3.4.1 基本信息
- 请求路径：/events/{eventId}
- 请求方式：DELETE
- 接口描述：该接口用于删除指定ID的宠物事件记录

#### 3.4.2 请求参数
路径参数：
```
eventId: 事件记录ID
```

#### 3.4.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": "删除成功"
}
```

## 4. 媒体文件管理

### 4.1 上传媒体文件
#### 4.1.1 基本信息
- 请求路径：/media/upload
- 请求方式：POST
- 接口描述：该接口用于上传单个或多个媒体文件到事件记录

#### 4.1.2 请求参数
参数格式：multipart/form-data
```
files: 媒体文件数组
recordId: 事件记录ID
```

#### 4.1.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "fileName": "文件名",
      "fileUrl": "文件URL",
      "fileType": "文件类型",
      "recordId": 1
    }
  ]
}
```

### 4.2 删除媒体文件
#### 4.2.1 基本信息
- 请求路径：/media/{mediaId}
- 请求方式：DELETE
- 接口描述：该接口用于根据媒体文件ID删除媒体文件

#### 4.2.2 请求参数
路径参数：
```
mediaId: 媒体文件ID
```

#### 4.2.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": "删除成功"
}
```

### 4.3 获取记录的媒体文件列表
#### 4.3.1 基本信息
- 请求路径：/media/record/{recordId}
- 请求方式：GET
- 接口描述：该接口用于根据记录ID获取关联的媒体文件列表

#### 4.3.2 请求参数
路径参数：
```
recordId: 记录ID
```

#### 4.3.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "fileName": "文件名",
      "fileUrl": "文件URL",
      "fileType": "文件类型",
      "recordId": 1
    }
  ]
}
```

## 5. 健康提醒

### 5.1 获取用户健康提醒列表
#### 5.1.1 基本信息
- 请求路径：/user/health-alerts
- 请求方式：GET
- 接口描述：该接口用于获取当前用户的所有健康提醒列表

#### 5.1.2 请求参数
无

#### 5.1.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "petId": 1,
      "petName": "宠物名称",
      "alertType": "提醒类型",
      "alertDate": "2023-01-01T00:00:00",
      "description": "提醒描述",
      "status": "待处理"
    }
  ]
}
```

### 5.2 创建健康提醒
#### 5.2.1 基本信息
- 请求路径：/user/health-alerts
- 请求方式：POST
- 接口描述：该接口用于创建一个新的健康提醒

#### 5.2.2 请求参数
参数格式：application/json
```json
{
  "petId": 1,
  "alertType": "提醒类型",
  "alertDate": "2023-01-01T00:00:00",
  "description": "提醒描述"
}
```

#### 5.2.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "petId": 1,
    "petName": "宠物名称",
    "alertType": "提醒类型",
    "alertDate": "2023-01-01T00:00:00",
    "description": "提醒描述",
    "status": "待处理"
  }
}
```

### 5.3 更新健康提醒
#### 5.3.1 基本信息
- 请求路径：/user/health-alerts/{healthId}
- 请求方式：PUT
- 接口描述：该接口用于更新指定的健康提醒信息

#### 5.3.2 请求参数
路径参数：
```
healthId: 健康提醒ID
```

请求体：
```json
{
  "petId": 1,
  "alertType": "提醒类型",
  "alertDate": "2023-01-01T00:00:00",
  "description": "提醒描述"
}
```

#### 5.3.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "petId": 1,
    "petName": "宠物名称",
    "alertType": "提醒类型",
    "alertDate": "2023-01-01T00:00:00",
    "description": "提醒描述",
    "status": "待处理"
  }
}
```

### 5.4 删除健康提醒
#### 5.4.1 基本信息
- 请求路径：/user/health-alerts/{healthId}
- 请求方式：DELETE
- 接口描述：该接口用于删除指定的健康提醒

#### 5.4.2 请求参数
路径参数：
```
healthId: 健康提醒ID
```

#### 5.4.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": "删除成功"
}
```

## 6. 文章管理

### 6.1 获取文章列表
#### 6.1.1 基本信息
- 请求路径：/articles
- 请求方式：GET
- 接口描述：该接口用于分页获取所有文章

#### 6.1.2 请求参数
参数格式：query string
```
currentPage: 当前页码
pageSize: 每页数量
```

#### 6.1.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "文章标题",
        "content": "文章内容",
        "author": "作者",
        "publishDate": "2023-01-01T00:00:00",
        "coverImage": "封面图片URL"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

### 6.2 获取文章详情
#### 6.2.1 基本信息
- 请求路径：/articles/{id}
- 请求方式：GET
- 接口描述：该接口用于根据ID获取指定文章的详细内容

#### 6.2.2 请求参数
路径参数：
```
id: 文章ID
```

#### 6.2.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "title": "文章标题",
    "content": "文章内容",
    "author": "作者",
    "publishDate": "2023-01-01T00:00:00",
    "coverImage": "封面图片URL",
    "tags": ["标签1", "标签2"]
  }
}
```

## 7. 登录历史记录

### 7.1 获取登录历史记录
#### 7.1.1 基本信息
- 请求路径：/login-history
- 请求方式：GET
- 接口描述：该接口用于获取当前用户最近七天的登录历史记录

#### 7.1.2 请求参数
无

#### 7.1.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 1,
      "loginTime": "2023-01-01T00:00:00",
      "ipAddress": "192.168.1.1",
      "device": "设备信息",
      "location": "登录地点"
    }
  ]
}
```

### 7.2 清除登录历史记录
#### 7.2.1 基本信息
- 请求路径：/login-history/clear
- 请求方式：DELETE
- 接口描述：该接口用于清除当前用户的所有登录历史记录

#### 7.2.2 请求参数
无

#### 7.2.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": null
}
```

## 8. 收容所管理

### 8.1 获取所有收容所列表
#### 8.1.1 基本信息
- 请求路径：/shelters
- 请求方式：GET
- 接口描述：该接口用于获取系统中所有收容所的列表信息

#### 8.1.2 请求参数
无

#### 8.1.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "sid": 1,
      "name": "收容所名称",
      "location": "收容所地址",
      "phone": "联系电话",
      "email": "邮箱"
    }
  ]
}
```

## 9. 寄养管理

### 9.1 删除寄养记录
#### 9.1.1 基本信息
- 请求路径：/fosters/delete/{id}
- 请求方式：DELETE
- 接口描述：该接口用于根据ID删除寄养记录

#### 9.1.2 请求参数
路径参数：
```
id: 寄养记录ID
```

#### 9.1.3 响应数据
参数格式：application/json
```json
{
  "code": 1,
  "msg": "success",
  "data": "寄养记录删除成功"
}
```

## 通用响应格式说明

所有API接口的响应都遵循以下格式：

```json
{
  "code": 1,        // 状态码，1表示成功，其他表示失败
  "msg": "success", // 响应消息
  "data": {}        // 响应数据，具体结构根据接口而定
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 1 | 成功 |
| 401001 | 登录失败 |
| 401002 | Token无效或已过期 |
| 403001 | 权限不足 |
| 404001 | 资源不存在 |
| 400001 | 请求参数错误 |
| 500001 | 服务器内部错误 |