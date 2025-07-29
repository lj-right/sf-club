# sf-club

#### 介绍
sf-club的刷题学习交流项目

#### 软件架构
软件架构说明

### 模块简介
sf-club-auth            	// 鉴权微服务 [3011]
  sf-club-auth-api            // 对外接口层
    api
    req
    resp
  sf-club-auth-common
    config
    enum
    util
  sf-club-auth-application     // 应用层
  sf-club-auth-application-controller
      controller
      convert  // DTO转BO
      dto
      config
      intercepter
  sf-club-auth-application-job
  sf-club-auth-application-mq
  sf-club-auth-domain       // 领域层
  service               // 领域能力
  bo       
  convert      // BO转PO
  util
  sf-club-auth-infra       // 基础设施层
  basic
      entity  
      mapper  
      service  
      util  
  rpc  
  mq
  sf-club-auth-starter    // 启动层，无关于任何业务，纯启动
        
 sf-club-gateway        // 网关微服务 [5000]	

 sf-club-subject        // 题目微服务 [3010]

 sf-club-circle         // 圈子微服务 [3014]

 sf-club-interview      // 面试微服务 [3015]

 sf-club-practice       // 练题微服务 [3013]

 sf-club-wechat         // 微信微服务 [3012]

 sf-club-oss            // oss微服务  [4000]
