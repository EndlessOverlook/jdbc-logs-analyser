package endless.overlook.jla.constants;

/**
 * Description:<b>公用常量类</b>
 *
 * @author Ralph
 * @since 2018-4-19 下午8:23:14
 */
public interface JlaConstants {

    /** 日志分析结果报告文件前缀:Analysing_ **/
    String C_PREFIX_REPORT_ANALYSING = "Analysing_";

    /** 日志分析结果报告文件前缀:Analysed_ **/
    String C_PREFIX_REPORT_ANALYSED = "Analysed_";

    /** 日志分析结果报告文件前缀：SQL执行统计 **/
    String C_PREFIX_REPORT_SQLEXECUTION = "SqlExecution_";

    /** 日志分析结果报告文件前缀：业务表查询统计 **/
    String C_PREFIX_REPORT_BUSINESSUNIONTABLE = "BusinessUnionTable_";

    /** 日志分析结果报告文件前缀：实体表查询统计 **/
    String C_PREFIX_REPORT_TOPPLAINTABLE = "TopPlainTable_";

    /** 日志分析结果报告文件前缀：执行耗时查询统计 **/
    String C_PREFIX_REPORT_TOPEXECUTIONDURATION = "TopExecutionDuration_";

    /** 日志分析结果报告文件前缀：模糊查询统计 **/
    String C_PREFIX_REPORT_TOPFUZZYQUERY = "TopFuzzyQuery_";

    /** 日志分析器包路径前缀 **/
    String C_PREFIX_ANALYSERS = "com.thunisoft.jla.service.analyser.impl.";

    /** 日志输出器包路径前缀 **/
    String C_PREFIX_PRINTERS = "com.thunisoft.jla.service.printer.impl.";

    /** 日志分析结果报告文件后缀 **/
    String C_SUFFIX_REPORT_LOG = ".log";

    /** 日志文件格式后缀 **/
    String C_SUFFIX_FILE_LOG = "log";

    /** JDBC日志文件格式标识 **/
    String C_SUFFIX_FILE_JDBC = "_jdbc_";

    /** SqlOnly日志文件格式标识 **/
    String C_SUFFIX_FILE_SQLONLYFILE = "_sqlonlyfile_";

    /** 业务标识：SQL统计实体集合 **/
    String C_KEY_BUSINESS_SQLEXECUTION = "businessSqlExecution";

    /** 业务标识：TABLE统计实体集合 **/
    String C_KEY_BUSINESS_UNIONTABLEENTITY = "businessUnionTableEntityList";

    /** 业务标识：TABLE表名数量映射 **/
    String C_KEY_BUSINESS_PLAINTALBECOUNTMAPPINGS = "plainTableCountMappings";

    /** 业务标识：所有业务SQL执行集合 **/
    String C_KEY_BUSINESS_BUSINESSSQLENTITYLIST = "businessSqlEntityList";

    /** 业务标识：模糊查询SQL执行集合 **/
    String C_KEY_BUSINESS_FUZZYQUERYSQLLIST = "fuzzyQuerySqlList";

    /** SQL解析器类型:JSqlParser **/
    String C_PROPERTY_PARSERTYPE_JSQLPARSER = "JSqlParser";

    /** SQL解析器类型:Manual **/
    String C_PROPERTY_PARSERTYPE_MANUAL = "Manual";

    /** SQL解析器类型:Batch **/
    String C_PROPERTY_PARSERTYPE_BATCH = "Batch";

}
