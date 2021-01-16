package endless.overlook.jla.service.printer.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.config.ConfigLoader;
import endless.overlook.jla.constants.JlaConfigConstants;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.constants.JlaNumberConstants;
import endless.overlook.jla.constants.JlaSymbolConstants;
import endless.overlook.jla.service.printer.AbstractAnalysedReportPrinter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 * Description:<b>执行耗时情况输出器</b>
 *
 * @author Ralph
 * @since 2018-4-26 下午4:18:55
 */
public class ExecutionDurationPrinter extends AbstractAnalysedReportPrinter {

    /** 日志记录对象 **/
    protected Logger logger = LoggerFactory
            .getLogger(ExecutionDurationPrinter.class);

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
    public ExecutionDurationPrinter(
            File jdbcLogFile, Charset processCharset, File analysingDirectory,
            ConfigLoader configLoader) {
        super(jdbcLogFile, processCharset,
                analysingDirectory, configLoader);
    }

    /**
     * Description:<b>输出分析报告</b>
     * @author Ralph
     * @since 2018-4-26 下午4:18:55
     * @param resultMap
     *              结果集合
     */
    @SuppressWarnings("unchecked")
    @Override
    public void printReport(Map<String, Object> resultMap) {
        try {
            String sqlFileType = configLoader.getConfig(
                JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_FILETYPE);
            if (!StringUtils.equals(sqlFileType,
                String.valueOf(
                    JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_FILETYPE_SQLTIMING))) {
                logger.warn(
                    "配置文件config.properties中[{}]配置为[{}],不输出TopExcecutionDuration报告......",
                    JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_FILETYPE,
                    sqlFileType);
                return;
            }
            //执行耗时统计
            String durationTopNNumberString = configLoader
                    .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_DURATIONTOPNUMBER);
            Integer durationTopNNumber = null;
            if (StringUtils.isNotBlank(durationTopNNumberString)) {
                durationTopNNumber = Integer.valueOf(durationTopNNumberString);
            } else {
                durationTopNNumber = JlaNumberConstants.N_TEN;
            }
            List<BusinessSqlEntity> longestDurationSqlList = (List<BusinessSqlEntity>) resultMap
                    .get(JlaConstants.C_KEY_BUSINESS_BUSINESSSQLENTITYLIST);
            if (CollectionUtils.isNotEmpty(longestDurationSqlList)) {
                StringBuffer executionDurationReportName = new StringBuffer(
                        JlaConstants.C_PREFIX_REPORT_ANALYSED);
                executionDurationReportName
                        .append(JlaConstants.C_PREFIX_REPORT_TOPEXECUTIONDURATION);
                executionDurationReportName.append(FilenameUtils
                        .getBaseName(jdbcLogFile.getAbsolutePath()));
                executionDurationReportName
                        .append(JlaConstants.C_SUFFIX_REPORT_LOG);
                File executionDurationFile = new File(analysingDirectory,
                        executionDurationReportName.toString());
                executionDurationFile.createNewFile();
                logger.info("开始生成[{}]分析结果TopExecutionDuration报告......",
                    executionDurationFile.getName());

                for (int i = JlaNumberConstants.N_ONE; i <= longestDurationSqlList
                        .size(); i++) {
                    if (i >= durationTopNNumber
                            + JlaNumberConstants.N_ONE) {
                        break;
                    }
                    BusinessSqlEntity businessSqlEntity = longestDurationSqlList
                            .get(i - JlaNumberConstants.N_ONE);
                    String index = String.valueOf(i);
                    if (i < JlaNumberConstants.N_TEN) {
                        index = String
                                .valueOf(JlaNumberConstants.N_ZERO) + i;
                    }
                    StringBuffer duration = new StringBuffer(
                            JlaSymbolConstants.C_SYMBOL_LEFTPARENTHESIS);
                    duration.append(index);
                    duration.append(JlaSymbolConstants.C_SYMBOL_RIGHTPARENTHESIS);
                    duration.append(" 业务SQL：");
                    duration.append(businessSqlEntity.getHeadInfo());
                    duration.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    duration.append("---");
                    duration.append(businessSqlEntity.getPlainSql());
                    duration.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    duration.append("---执行耗时：");
                    duration.append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    duration.append(businessSqlEntity.getDurationTime());
                    duration.append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    duration.append(" ms");
                    duration.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    duration.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    Files.append(duration.toString(), executionDurationFile,
                        processCharset);
                }
            }
        } catch (IOException e) {
            logger.error("生成{}的ExecutionDuration报告失败......",
                jdbcLogFile.getName(), e);
        }

    }

}
