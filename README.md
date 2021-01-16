# JdbcLogsAnalyser

A Tool Which Can Analyse Jdbc Logs

## 一·特性总览
· 支持jdbc.sqltiming日志文件分析<br>
· 支持jdbc.sqlonly日志文件分析<br>
· 支持指定时间段的日志文件分析<br>
· 支持多种维度指标的分析<br>
· 支持外部配置文件覆盖默认配置<br>
· 多文件并行分析<br>
· IO任务与CPU任务并行<br>

## 二·环境准备
· JDK1.8+<br>
· 可运行的JdbcLogsAnalyser程序包(JAR)<br>
· 待分析的JDBC日志文件<br>
## 三·使用方法
· 修改JAR中props/config.properties中的相关配置，或者在JAR同级创建props/config.properties配置文件覆盖默认配置<br>
· 将待分析的Jdbc日志文件(可多个)放入指定路径下<br>
· 运行JAR，等待分析结果<br>
## 四·未来规划
· jdbc.sqltiming和jdbc.sqlonly日志文件类型自动识别<br>
· 多个文件分析结果汇总，CSV结果展示<br>