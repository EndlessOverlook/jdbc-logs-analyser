package endless.overlook.jla.service.printer.impl;

import com.google.common.io.Files;
import endless.overlook.jla.beans.BusinessUnionTableEntity;
import endless.overlook.jla.config.ConfigLoader;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.constants.JlaSymbolConstants;
import endless.overlook.jla.service.printer.AbstractAnalysedReportPrinter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Description:<b>业务表操作情况输出器</b>
 *
 * @author Ralph
 * @since 2018-4-26 下午4:06:27
 */
public class BusinessTablePrinter extends AbstractAnalysedReportPrinter {

    /** 日志记录对象 **/
    protected Logger logger = LoggerFactory
            .getLogger(BusinessTablePrinter.class);

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
    public BusinessTablePrinter(File jdbcLogFile, Charset processCharset,
            File analysingDirectory, ConfigLoader configLoader) {
        super(jdbcLogFile, processCharset, analysingDirectory, configLoader);
    }

    /**
     * Description:<b>输出分析报告</b>
     * @author Ralph
     * @since 2018-4-26 下午4:06:27
     * @param resultMap
     *              结果集合
     */
    @SuppressWarnings("unchecked")
    @Override
    public void printReport(Map<String, Object> resultMap) {
        try {
            //业务表联查统计
            List<BusinessUnionTableEntity> tableEntityList = (List<BusinessUnionTableEntity>) resultMap
                    .get(JlaConstants.C_KEY_BUSINESS_UNIONTABLEENTITY);
            if (CollectionUtils.isNotEmpty(tableEntityList)) {
                StringBuffer businessTableReportName = new StringBuffer(
                        JlaConstants.C_PREFIX_REPORT_ANALYSED);
                businessTableReportName
                        .append(JlaConstants.C_PREFIX_REPORT_BUSINESSUNIONTABLE);
                businessTableReportName.append(FilenameUtils
                        .getBaseName(jdbcLogFile.getAbsolutePath()));
                businessTableReportName
                        .append(JlaConstants.C_SUFFIX_REPORT_LOG);
                File businessTableResultFile = new File(analysingDirectory,
                        businessTableReportName.toString());
                businessTableResultFile.createNewFile();
                logger.info("开始生成[{}]分析结果BusinessUnionTable报告......",
                        businessTableResultFile.getName());

                for (BusinessUnionTableEntity tableEntity : tableEntityList) {
                    StringBuffer businessUnionTable = new StringBuffer(
                            "---业务表：");
                    businessUnionTable
                            .append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    businessUnionTable
                            .append(tableEntity.getBusinessTableName());
                    businessUnionTable
                            .append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    businessUnionTable.append("---解析器：");
                    businessUnionTable
                            .append(JlaSymbolConstants.C_SYMBOL_LEFTPARENTHESIS);
                    businessUnionTable.append(tableEntity.getParserType());
                    businessUnionTable
                            .append(JlaSymbolConstants.C_SYMBOL_RIGHTPARENTHESIS);
                    businessUnionTable
                            .append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    businessUnionTable.append("---操作次数：");
                    businessUnionTable
                            .append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    businessUnionTable.append(tableEntity.getTableHitCount());
                    businessUnionTable
                            .append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    businessUnionTable
                            .append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    businessUnionTable
                            .append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    Files.append(businessUnionTable.toString(),
                            businessTableResultFile, processCharset);
                }
            }
        } catch (IOException e) {
            logger.error("生成{}的BusinessTable报告失败......", jdbcLogFile.getName(),
                    e);
        }
    }
}
