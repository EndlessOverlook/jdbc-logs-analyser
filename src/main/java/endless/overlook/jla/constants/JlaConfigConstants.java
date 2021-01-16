package endless.overlook.jla.constants;

/**
 * Description:<b>系统配置常量类</b>
 *
 * @author Ralph
 * @since 2018-4-19 下午8:23:04
 */
public interface JlaConfigConstants {

    /** 默认配置文件路径 **/
    String C_NAME_JLA_PATH_CONFIG = "props/config";

    /** 配置键：JDBC日志文件目录路径 **/
    String C_KEY_JLA_JDBCLOGFILES_DIRECTORYPATH = "jdbclogfiles.directoryPath";

    /** 配置键：待过滤的JDBC日志文件名称集合 **/
    String C_KEY_JLA_JDBCLOGFILES_EXCLUDEFILES = "jdbclogfiles.excludeFiles";

    /** 配置键：JDBC日志文件类型：1·sqltiming;2·sqlonly **/
    String C_KEY_JLA_JDBCLOGFILES_FILETYPE = "jdbclogfiles.fileType";

    /** 配置键：JDBC日志文件类型：1·sqltiming **/
    Integer C_KEY_JLA_JDBCLOGFILES_FILETYPE_SQLTIMING = 1;

    /** 配置键：待过滤的JDBC日志行标识 **/
    String C_KEY_JLA_JDBCLOGFILES_EXCLUDELINES = "jdbclogfiles.excludeLines";

    /** 配置键：JDBC日志文件头文件截止行数 **/
    String C_KEY_JLA_JDBCLOGFILES_HEADSQLNUMBERS = "jdbclogfiles.headSqlNumbers";

    /** 配置键：JDBC日志分析报告中单表操作次数总和TOP N **/
    String C_KEY_JLA_JDBCLOGFILES_TABLEHITTOPNUMBER = "jdbclogfiles.tableHitTopNumber";

    /** 配置键：JDBC日志分析报告中执行耗时TOP N **/
    String C_KEY_JLA_JDBCLOGFILES_DURATIONTOPNUMBER = "jdbclogfiles.durationTopNumber";

    /** 配置键：JDBC日志分析报告中模糊查询次数TOP N **/
    String C_KEY_JLA_JDBCLOGFILES_FUZZYQUERYTOPNUMBER = "jdbclogfiles.fuzzyQueryTopNumber";

    /** 配置键：JDBC日志文件编码格式 **/
    String C_KEY_JLA_JDBCLOGFILES_PROCESSCHARSET = "jdbclogfiles.processCharset";

    /** 配置键：JDBC日志文件分析任务执行超时时间 **/
    String C_KEY_JLA_JDBCLOGFILES_PROCESSTIMEOUT = "jdbclogfiles.processTimeout";

    /** 配置键：JDBC日志文件分析起始时间 **/
    String C_KEY_JLA_JDBCLOGFILES_BEGINTIME = "jdbclogfiles.beginTime";

    /** 配置键：JDBC日志文件分析结束时间 **/
    String C_KEY_JLA_JDBCLOGFILES_ENDTIME = "jdbclogfiles.endTime";

    /** 配置键：JDBC日志文件分析器 **/
    String C_KEY_JLA_JDBCLOGFILES_ANALYSERS = "jdbclogfiles.analysers";

    /** 配置键：JDBC日志文件输出器 **/
    String C_KEY_JLA_JDBCLOGFILES_PRINTERS = "jdbclogfiles.printers";

}
