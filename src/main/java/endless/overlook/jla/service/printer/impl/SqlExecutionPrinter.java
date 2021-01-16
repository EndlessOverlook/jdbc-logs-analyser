package endless.overlook.jla.service.printer.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.config.ConfigLoader;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.constants.JlaSymbolConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import endless.overlook.jla.service.printer.AbstractAnalysedReportPrinter;

/**
 * Description:<b>SQL执行情况输出器</b>
 *
 * @author Ralph
 * @since 2018-4-26 下午3:35:45
 */
public class SqlExecutionPrinter extends AbstractAnalysedReportPrinter {

    /** 日志记录对象 **/
    protected Logger logger = LoggerFactory
            .getLogger(SqlExecutionPrinter.class);

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018-4-26 下午3:40:24
     * @param jdbcLogFile
     *              目标JDBC日志文件
     * @param processCharset
     *              目标JDBC日志文件编码集对象
     * @param analysingDirectory
     *              分析结果文件目录
     * @param configLoader
     *              配置加载器
     */
    public SqlExecutionPrinter(
            File jdbcLogFile, Charset processCharset, File analysingDirectory,
            ConfigLoader configLoader) {
        super(jdbcLogFile, processCharset,
                analysingDirectory, configLoader);
    }

    /**
     * Description:<b>输出分析报告</b>
     * @author Ralph
     * @since 2018-4-26 下午3:35:45
     * @param resultMap
     *              结果集合
     */
    @SuppressWarnings("unchecked")
    @Override
    public void printReport(Map<String, Object> resultMap) {
        try {
            //业务SQL实体统计
            List<BusinessSqlEntity> sqlEntityList = (List<BusinessSqlEntity>) resultMap
                    .get(JlaConstants.C_KEY_BUSINESS_SQLEXECUTION);
            if (CollectionUtils.isNotEmpty(sqlEntityList)) {
                StringBuffer sqlReportName = new StringBuffer(
                        JlaConstants.C_PREFIX_REPORT_ANALYSED);
                sqlReportName.append(JlaConstants.C_PREFIX_REPORT_SQLEXECUTION);
                sqlReportName.append(FilenameUtils.getBaseName(jdbcLogFile
                        .getAbsolutePath()));
                sqlReportName.append(JlaConstants.C_SUFFIX_REPORT_LOG);
                File sqlExecutionResultFile = new File(analysingDirectory,
                        sqlReportName.toString());
                sqlExecutionResultFile.createNewFile();
                logger.info("开始生成[{}]分析结果SqlExecution报告......",
                    sqlExecutionResultFile.getName());

                for (BusinessSqlEntity sqlEntity : sqlEntityList) {
                    Files.append(sqlEntity.getHeadInfo()
                            + JlaSymbolConstants.C_SYMBOL_NEXTLINE,
                        sqlExecutionResultFile, processCharset);
                    Files.append(sqlEntity.getPlainSql()
                            + JlaSymbolConstants.C_SYMBOL_NEXTLINE,
                        sqlExecutionResultFile, processCharset);
                    StringBuffer sqlEntityInfo = new StringBuffer("---业务表：");
                    sqlEntityInfo.append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    sqlEntityInfo.append(sqlEntity.getTableName());
                    sqlEntityInfo
                            .append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    sqlEntityInfo.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    sqlEntityInfo.append("---执行次数：");
                    sqlEntityInfo.append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    sqlEntityInfo.append(sqlEntity.getSqlCount());
                    sqlEntityInfo
                            .append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    sqlEntityInfo.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    sqlEntityInfo.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    Files.append(sqlEntityInfo.toString(),
                        sqlExecutionResultFile, processCharset);
                }
            }
        } catch (IOException e) {
            logger.error("生成{}的SqlExecution报告失败......",
                jdbcLogFile.getName(), e);
        }
    }
}
