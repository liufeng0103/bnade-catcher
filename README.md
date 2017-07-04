# bnade-catcher
bnade的后台，数据采集，统计，分析，归档等

重构之前的程序，打算把各方面写的详细点，让想学习的朋友更容易的阅读和使用。也期待更多的朋友加入我们

程序还在改造中，请参考老程序https://github.com/liufeng0103/bnade

item和bonus关系请参考doc下的itembonus.csv文件，[文件地址](https://gist.github.com/erorus/35705144b1a4ad015924)由https://theunderminejournal.com/作者[erorus](https://github.com/erorus)分享

原来的数据库设计为一个游戏服务器一张表, 现在新建了v2目录使用分区表来重新实现

对于NoSQL数据库,之前有考虑过MongoDB,由于租的服务器配置有限,无法带动，继续使用MySQL

## 项目环境
- java 1.8
- mysql 5.7
- maven 3.3.9
- dbutils jdbc的封装
- druid 数据库连接池
- logback 日志框架

## 项目安装
1. 安装mysql5.7，创建数据库bnade
2. 使用doc目录下的bnade.sql建表(未完成)，由于使用了MySQL5.7的中文检索插件，修改配置文件添加ngram_token_size=1，默认为2
3. 使用doc目录下的insert.sql导入数据(未完成)
4. 使用doc目录下的item.sql导入物品信息，第一行有数据导出的日期