package endless.overlook.jla.service.printer.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import endless.overlook.jla.beans.BusinessPlainTableEntity;
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
 * Description:<b>实体表操作情况输出器</b>
 *
 * @author Ralph
 * @since 2018-4-26 下午4:12:21
 */
public class PlainTablePrinter extends AbstractAnalysedReportPrinter {

    /** 日志记录对象 **/
    protected Logger logger = LoggerFactory.getLogger(PlainTablePrinter.class);

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
    public PlainTablePrinter(
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
                    .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_TABLEHITTOPNUMBER);
            Integer topNNumber = null;
            if (StringUtils.isNotBlank(topNNumberString)) {
                topNNumber = Integer.valueOf(topNNumberString);
            } else {
                topNNumber = JlaNumberConstants.N_TEN;
            }
            List<Map.Entry<String, BusinessPlainTableEntity>> plainTableCountMappingsList = (List<Map.Entry<String, BusinessPlainTableEntity>>) resultMap
                    .get(JlaConstants.C_KEY_BUSINESS_PLAINTALBECOUNTMAPPINGS);
            if (CollectionUtils.isNotEmpty(plainTableCountMappingsList)) {
                StringBuffer plainTableReportName = new StringBuffer(
                        JlaConstants.C_PREFIX_REPORT_ANALYSED);
                plainTableReportName
                        .append(JlaConstants.C_PREFIX_REPORT_TOPPLAINTABLE);
                plainTableReportName.append(FilenameUtils
                        .getBaseName(jdbcLogFile.getAbsolutePath()));
                plainTableReportName.append(JlaConstants.C_SUFFIX_REPORT_LOG);
                File plainTableResultFile = new File(analysingDirectory,
                        plainTableReportName.toString());
                plainTableResultFile.createNewFile();
                logger.info("开始生成[{}]分析结果TopPlainTable报告......",
                    plainTableResultFile.getName());

                for (int i = JlaNumberConstants.N_ONE; i <= plainTableCountMappingsList
                        .size(); i++) {
                    if (i >= topNNumber + JlaNumberConstants.N_ONE) {
                        break;
                    }
                    Entry<String, BusinessPlainTableEntity> mappingsEntry = plainTableCountMappingsList
                            .get(i - JlaNumberConstants.N_ONE);
                    BusinessPlainTableEntity plainTableEntity = mappingsEntry
                            .getValue();
                    String index = String.valueOf(i);
                    if (i < JlaNumberConstants.N_TEN) {
                        index = String
                                .valueOf(JlaNumberConstants.N_ZERO) + i;
                    }
                    StringBuffer plainTable = new StringBuffer("---");
                    plainTable
                            .append(JlaSymbolConstants.C_SYMBOL_LEFTPARENTHESIS);
                    plainTable.append(index);
                    plainTable
                            .append(JlaSymbolConstants.C_SYMBOL_RIGHTPARENTHESIS);
                    plainTable.append(" 业务表：<");
                    plainTable.append(mappingsEntry.getKey());
                    plainTable.append(">  操作总次数： ");
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    plainTable.append(plainTableEntity.getHitCount());
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    plainTable.append("---");
                    plainTable
                            .append(JlaSymbolConstants.C_SYMBOL_LEFTPARENTHESIS);
                    plainTable.append("SELECT:");
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    plainTable.append(plainTableEntity.getSelectHitCount());
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_COMMA);
                    plainTable.append(" UDPATE:");
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    plainTable.append(plainTableEntity.getUpdateHitCount());
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_COMMA);
                    plainTable.append(" DELETE:");
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    plainTable.append(plainTableEntity.getDeleteHitCount());
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_COMMA);
                    plainTable.append(" INSERT:");
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_OPENBRACE);
                    plainTable.append(plainTableEntity.getInsertHitCount());
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_CLOSEBRACE);
                    plainTable
                            .append(JlaSymbolConstants.C_SYMBOL_RIGHTPARENTHESIS);
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    plainTable.append(JlaSymbolConstants.C_SYMBOL_NEXTLINE);
                    Files.append(plainTable.toString(), plainTableResultFile,
                        processCharset);
                }
            }
        } catch (IOException e) {
            logger.error("生成{}的PlainTable报告失败......",
                jdbcLogFile.getName(), e);
        }
    }
}
