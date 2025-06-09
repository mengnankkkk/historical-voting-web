# shopping-web

## 1. 项目概述

 shopping-web是一个基于 Spring Boot 2.7.18 开发的商城项目，采用微服务架构设计。系统支持用户注册、登录、OAuth2 认证、文件上传等功能。

## 2. 技术栈

- 后端框架：Spring Boot 2.7.18
- 安全框架：Spring Security
- 数据库：MySQL
- 缓存：Redis
- 消息队列：Kafka
- 文件存储：腾讯云 COS
- 认证方式：JWT + OAuth2
- 其他：WebSocket、邮件服务

## 3. 项目结构

```
historical-voting-web/
├── user-service/                 # 用户服务模块
│   ├── src/main/java/
│   │   └── com/historical/voting/user/
│   │       ├── annotation/      # 自定义注解
│   │       ├── config/          # 配置类
│   │       ├── controller/      # 控制器
│   │       ├── entity/          # 实体类
│   │       ├── exception/       # 异常处理
│   │       ├── Factory/         # 工厂类
│   │       ├── interceptor/     # 拦截器
│   │       ├── mapper/          # MyBatis映射
│   │       ├── repository/      # JPA仓库
│   │       ├── service/         # 服务层
│   │       ├── strategy/        # 策略模式实现
│   │       └── util/            # 工具类
│   └── resources/
│       └── application.yml      # 配置文件
```

## 4. 核心功能模块

### 4.1 用户认证模块

#### 4.1.1 JWT认证流程

1. 用户登录流程：
   ```
   用户登录 -> 验证凭据 -> 生成JWT令牌（访问令牌+刷新令牌）-> 返回令牌
   ```

流程：

在serviceimpl中实现login方法：

从数据库中select有没有输入的username,用户注册状态是不是0，用户是否是被封锁，密码是不是对。

然后生成access token 和refreash token

使用redis，opsforvalue.set设置key-vaule和过期时间

把他们放入map集合里面，然后返回token

JWT加密：

采用HS512,然后创建payload，里面存入username。

```java
return Jwts.builder()
    .setClaims(claims)                        // 设置自定义声明
    .setSubject(username)                     // 设置主题，一般也是用户标识
    .setIssuedAt(new Date())                  // 签发时间
    .setExpiration(new Date(System.currentTimeMillis() + expiration))  // 过期时间
    .signWith(key)                            // 使用给定密钥签名（默认 HMAC-SHA256）
    .compact();                               // 构建并返回字符串

```



1. 请求认证流程：

   ```
   请求 -> JwtAuthenticationFilter拦截 -> 验证Token -> 提取用户信息 -> 放行请求
   ```

JwtAuthenticationFilter：

尝试直接使用access token，从requset的头里提取header.startsWith("Bearer ")开头的，然后跳过前面的前缀，获取到token

access token刷新，claims必须要合法，然后从claims里获取username

然后检查token是不是在redis的黑名单中，然后看redis是不是存了这个令牌，get出来，然后requset.setAttribute加到请求上，然后返回response

然后refresh token刷新，

先是从request看到刷新令牌，然后先验证是不是和redis中的相同

相同的话，再生成一个新的access token令牌，然后更新redis中的令牌

把他加到resonse的响应头里，然后去看放不放行请求。



注册的话，就是使用mailSender去发送验证码，只有验证码对才可以进行注册操作，创建新用户，写入数据库。

#### 4.1.2 OAuth2认证流程

1. GitHub OAuth2登录流程：
   ```
   用户点击GitHub登录 -> 重定向到GitHub -> 用户授权 -> 回调接口 -> 创建/更新用户信息 -> 生成JWT
   ```

### 4.2 用户管理模块

#### 主要接口

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // 发送验证码
    @PostMapping("/register/send-code")
    
    // 用户注册
    @PostMapping("/register")
    
    // 用户登录
    @PostMapping("/login")
    
    // 用户登出
    @PostMapping("/logout")
    
    // 获取用户信息
    @GetMapping("/profile")
}
```

### 4.3 文件存储模块

- 支持图片、视频上传
- 支持生成缩略图
- 文件存储位置可配置（本地/腾讯云COS）

FileUploadConfig.java:

```
MultipartConfigFactory factory = new MultipartConfigFactory();
```

然后设置单个的文件大小，传输的总大小

FileUtils.java:





### 4.3 优惠券模块

#### 4.3.1 优惠券类型
- 满减券：满足指定金额后减免固定金额
- 折扣券：按比例折扣
- 无门槛券：直接减免固定金额
- 限时券：在指定时间段内有效

#### 4.3.2 主要接口
```java
@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    // 创建优惠券
    @PostMapping("/create")
    
    // 发放优惠券
    @PostMapping("/distribute")
    
    // 查询用户优惠券
    @GetMapping("/user/{userId}")
    
    // 使用优惠券
    @PostMapping("/use")
    
    // 查询优惠券详情
    @GetMapping("/{couponId}")
}
```

#### 4.3.3 优惠券业务流程

1. 优惠券创建流程：
   ```
   管理员创建优惠券 -> 设置优惠券规则 -> 设置发放策略 -> 保存优惠券信息
   ```

2. 优惠券发放流程：
   ```
   触发发放条件 -> 检查发放规则 -> 创建用户优惠券记录 -> 发送优惠券到账通知
   ```

3. 优惠券使用流程：
   ```
   用户选择优惠券 -> 验证使用条件 -> 计算优惠金额 -> 标记优惠券已使用 -> 应用优惠
   ```

### 4.4 视频处理模块

#### 4.4.1 功能特性
- 视频转码：支持多种格式转换
- 视频压缩：自适应码率压缩
- 视频切片：支持HLS协议切片
- 水印添加：支持图片水印和文字水印
- 缩略图生成：自动生成视频预览图

#### 4.4.2 主要接口
```java
@RestController
@RequestMapping("/api/videos")
public class VideoController {
    // 上传视频
    @PostMapping("/upload")
    
    // 视频转码
    @PostMapping("/transcode")
    
    // 获取视频信息
    @GetMapping("/{videoId}")
    
    // 获取播放地址
    @GetMapping("/{videoId}/play")
    
    // 生成缩略图
    @PostMapping("/{videoId}/thumbnail")
}
```

#### 4.4.3 视频处理流程

1. 视频上传流程：
   ```
   上传原始视频 -> 存储临时文件 -> 视频信息提取 -> 触发异步处理 -> 返回上传结果
   ```

2. 视频转码流程：
   ```
   读取原始视频 -> 检查转码参数 -> 执行转码任务 -> 生成多清晰度版本 -> 更新视频状态
   ```

3. 视频播放流程：
   ```
   请求视频播放 -> 验证访问权限 -> 获取用户网络状态 -> 智能选择清晰度 -> 返回播放地址
   ```

### 4.5 消息队列使用

#### 4.5.1 Kafka主题设计
```
video-upload-topic：视频上传消息
video-transcode-topic：视频转码消息
coupon-distribute-topic：优惠券发放消息
notification-topic：通知消息
```

#### 4.5.2 消息处理流程

1. 视频处理消息：
   ```
   生产者：
   视频上传完成 -> 发送转码消息 -> 发送缩略图生成消息
   
   消费者：
   接收处理消息 -> 执行对应任务 -> 更新处理状态 -> 发送结果通知
   ```

2. 优惠券消息：
   ```
   生产者：
   触发优惠券发放 -> 发送发放消息 -> 记录发送状态
   
   消费者：
   接收发放消息 -> 检查发放规则 -> 执行发放操作 -> 发送结果通知
   ```

## 5. 安全配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    // 安全过滤链配置
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // 配置不需要认证的路径
        - /api/users/register/**
        - /api/users/login/**
        - /api/users/oauth2/**
        - 静态资源
        
        // 配置需要认证的路径
        - 其他所有API接口
    }
}
```

## 6. 关键业务流程

### 6.1 用户注册流程

1. 用户提交邮箱地址
2. 系统生成验证码并发送邮件
3. 用户提交注册信息（包含验证码）
4. 系统验证信息：
   - 验证码是否正确
   - 用户名是否已存在
   - 邮箱是否已注册
5. 创建用户账号
6. 返回注册成功响应

### 6.2 文件上传流程

1. 客户端发起上传请求
2. 系统验证文件：
   - 文件大小
   - 文件类型
   - 文件格式
3. 根据配置选择存储方式：
   - 本地存储：保存到配置的本地路径
   - 腾讯云COS：上传到指定bucket
4. 生成访问URL
5. 返回文件访问地址

### 6.3 视频处理业务流程

1. 视频上传及处理：
   - 客户端分片上传视频
   - 服务端合并分片
   - 提取视频元信息
   - 生成唯一标识
   - 触发异步处理任务

2. 视频转码处理：
   - 读取原始视频文件
   - 按配置参数进行转码
   - 生成多种清晰度版本
   - 生成HLS切片文件
   - 上传处理结果到存储

3. 播放流程优化：
   - 根据网络状况自动选择清晰度
   - 支持断点续播
   - 实现预加载机制
   - 自动重试失败请求

### 6.4 优惠券业务流程

1. 优惠券创建及发放：
   - 设置优惠券基本信息
   - 配置使用规则
   - 设置发放策略
   - 执行发放任务
   - 发送发放通知

2. 优惠券核销流程：
   - 提交优惠券使用请求
   - 验证优惠券有效性
   - 检查使用条件
   - 计算优惠金额
   - 执行核销操作
   - 更新优惠券状态

## 7. 配置说明

### 7.1 应用配置

```yaml
server:
  port: 8080

spring:
  application:
    name: user-service
  
  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/historical_voting
    username: root
    password: 123456
  
  # Redis配置
  redis:
    host: localhost
    port: 6379
  
  # 邮件服务配置
  mail:
    host: smtp.163.com
    port: 465
    
  # OAuth2配置
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
```

### 7.2 JWT配置

```yaml
jwt:
  access-token:
    expiration: 1800000  # 30分钟
  refresh-token:
    expiration: 604800000  # 7天
```

### 7.3 视频处理配置

```yaml
video:
  processing:
    # 转码配置
    transcode:
      formats: ["mp4", "hls"]
      qualities: ["360p", "720p", "1080p"]
      thread-pool-size: 5
    
    # 水印配置
    watermark:
      enabled: true
      position: "bottom-right"
      text: "Historical Voting"
    
    # 存储配置
    storage:
      temp-path: "/tmp/videos"
      output-path: "/data/videos"
      thumbnail-path: "/data/thumbnails"
```

### 7.4 优惠券配置

```yaml
coupon:
  # 发放配置
  distribution:
    max-per-user: 10
    batch-size: 1000
    
  # 使用规则
  usage:
    max-discount: 1000
    min-amount: 0
    
  # 过期配置
  expiration:
    default-days: 30
    check-interval: 3600
```

## 8. 开发指南

### 8.1 本地开发环境搭建

1. 克隆项目
2. 配置数据库
3. 配置Redis
4. 配置邮件服务
5. 配置OAuth2（可选）
6. 运行应用

### 8.2 接口测试

可以通过以下方式访问Swagger文档：
- 启动应用后访问：`http://localhost:8080/swagger-ui.html`
- 查看API文档：`http://localhost:8080/v2/api-docs`

## 9. 部署说明

### 9.1 环境要求

- JDK 8+
- MySQL 5.7+
- Redis 5+
- Maven 3.6+

### 9.2 部署步骤

1. 打包应用：`mvn clean package`
2. 配置环境变量
3. 运行应用：`java -jar user-service.jar`

## 10. 注意事项

1. 安全性考虑
   - 所有密码必须加密存储
   - 敏感配置使用环境变量
   - 定期更换JWT密钥

2. 性能优化
   - 合理使用缓存
   - 长期令牌存储在Redis
   - 文件上传大小限制

3. 可维护性
   - 遵循代码规范
   - 添加适当的注释
   - 保持日志完整性 