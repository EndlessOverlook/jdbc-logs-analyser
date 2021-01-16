package endless.overlook.jla.service.printer.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import endless.overlook.jla.config.ConfigLoader;
import endless.overlook.jla.constants.JlaConfigConstants;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.constants.JlaNumberConstants;
import endless.overlook.jla.constants.JlaSymbolConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import endless.overlook.jla.service.printer.AbstractAnalysedReportPrinter;

/**
 * Description:<b>模糊查询情况输出器</b>
 *
 * @author Ralph
 * @since 2018-5-7 下午2:03:15
 */
public class FuzzyQueryPrinter extends AbstractAnalysedReportPrinter {

    /** 日志记录对象 **/
    protected Logger logger = LoggerFactory.getLogger(FuzzyQueryPrinter.class);

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
    public FuzzyQueryPrinter(
            File jdbcLogFile, Charset processCharset, File analysingDirectory,
            ConfigLoader configLoader) {
        super(jdbcLogFile, processCharset,
                analysingDirectory, configLoader);
    }

    /**
     * Description:<b>输出分析报告</b>
     * @author Ralph
     * @since 2018-4-26 下午4:12:21
     * @param resultMap
     *              结果集合
     */
    @SuppressWarnings("unchecked")
    @Override
    public void printReport(Map<String, Object> resultMap) {
        try {
            //业务表操作次数统计
            String topNNumberString = configLoader
                    .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_FUZZYQUERYTOPNUMBER);
            Integer topNNumber = null;
            if (StringUtils.isNotBlank(topNNumberString)) {
                topNNumber = Integer.valueOf(topNNumberString);
            } else {
                topNNumber = JlaNumberConstants.N_TEN;
            }
            List<Map.Entry<String, Integer>> fuzzyQueryCountMappingsList = (List<Map.Entry<String, Integer>>) resultMap
                    .get(JlaConstants.C_KEY_BUSINESS_FUZZYQUERYSQLLIST);
            if (CollectionUtils.isNotEmpty(fuzzyQueryCountMappingsList)) {
                StringBuffer fuzzyQueryReportName = new StringBuffer(
                        JlaConstants.C_PREFIX_REPORT_ANALYSED);
                fuzzyQueryReportName
                        .append(JlaConstants.C_PREFIX_REPORT_TOPFUZZYQUERY);
                fuzzyQueryReportName.append(FilenameUtils
                        .getBaseName(jdbcLogFile.getAbsolutePath()));
                fuzzyQueryReportName.append(JlaConstants.C_SUFFIX_REPORT_LOG);
                File fuzzyQueryResultFile = new File(analysingDirectory,
                        fuzzyQueryReportName.toString());
                fuzzyQueryResultFile.createNewFile();
                logger.info("开始生成[{}]分析结果TopFuzzyQuery报告......",
                    fuzzyQueryResultFile.getName());

                for (int i = JlaNumberConstants.N_ONE; i <= fuzzyQueryCountMappingsList
                        .size(); i++) {
                    if (i >= topNNumber + JlaNumberConstants.N_ONE) {
                        break;
                    }
                    Entry<String, Integer> mappingsEntry = fuzzyQueryCountMappingsList
                            .get(i - JlaNumberConstants.N_ONE);
                    String index = String.valueOf(i);
                    if (i < JlaNumberConstants.N_TEN) {
                        index = String
                                .valueOf(JlaNumberConstants.N_ZERO) + i;
                    }
                    StringBuffer fuzzyQuery = new StringBuffer("---");
                    fuzzyQuery
                            .append(JlaSymbolConstants.C_SYMBOL_LEFTPARENTHESIS);
                    fuzzyQuery.append(index);
                    fuzzyQuery
                            .append(JlaSymbolConstants.C_SYMBOL_RIGHTPARENTHESIS);
                    fuzzyQuery.append(" 业务SQL：");
                    fuzzyQuery.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    fuzzyQuery.append(mappingsEntry.getKey());
                    fuzzyQuery.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    fuzzyQuery.append("---执行次数：");
                    fuzzyQuery.append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    fuzzyQuery.append(mappingsEntry.getValue());
                    fuzzyQuery.append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    fuzzyQuery.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    fuzzyQuery.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    Files.append(fuzzyQuery.toString(), fuzzyQueryResultFile,
                        processCharset);
                }
            }
        } catch (IOException e) {
            logger.error("生成{}的FuzzyQuery报告失败......",
                jdbcLogFile.getName(), e);
        }
    }
}
