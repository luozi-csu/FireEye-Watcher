# 1.项目介绍

FireEye Watcher是一个铝电解槽过热度在线识别系统。它提供Android客户端和Web服务端，客户端拍摄铝电解槽火眼区域视频并上传至服务端，服务端对视频进行处理和识别然后返回过热度情况。

# 2.实现和特点

FireEye Watcher有如下主要特点：

* 客户端采用Android（Java）开发，服务端采用Flask（Python）开发
* 简洁易用的界面，支持视频实时预览，异步更新和查询记录
* 客户端基于Apache HttpComponents Client实现网络通信
* 服务端基于MVC模式，采用MySQL存储数据，提供用户管理和JWTToken鉴权
* 算法模型采用YOLOv5+ConvLSTM
* 提供日志，后台可查看用户访问记录，接口行为等

# 3.项目结构

## 3.1 客户端

客户端源代码集中在目录ui/app/src/main/java/com/luozi/fireeyewatcher，结构如下：

```
./ui/app/src/main/java/com/luozi/fireeyewatcher
├─activity
├─adapter
├─fragment
├─http
├─manager
├─model
├─utils
└─view
```

* activity: 自定义Android页面活动
* adapter: 用于列表的适配器
* fragment: 自定义Android页面碎片
* http: HTTP协议中的一些常量
* manager：管理Activity活动栈
* model：Web交互使用的数据模型
* utils：一些通用的工具类
* view：自定义UI组件

## 3.2 服务端

服务端结构如下：

```
./src
├─algorithm
├─common
├─controller
├─database
├─log
├─middleware
├─model
├─repository
├─service    
└─util
```

* algorithm：算法模块
* common：通用模块，如通用的HTTP响应
* controller：Web接口，基于Flask Blueprint实现
* database：数据库的配置和连接
* log：基于logging的日志组件
* middleware：WSGI中间件
* model：Web交互使用的数据模型
* repository：数据存储的封装，直接与数据库交互
* service：服务接口的实现层
* util：通用的工具，如JWTToken模块

# 4.安装部署

安卓设备（Android 9.0及以上）可直接安装ui/app/release目录下的apk包，服务端部署前需要确保你的主机上有Python3环境。

```
cd src
# 安装依赖
pip install -r requirements.txt
# 运行服务器后台进程
python main.py &
```
