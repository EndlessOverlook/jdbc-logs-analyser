package endless.overlook.jla.service.processor;

import endless.overlook.jla.beans.BusinessSqlEntity;
import endless.overlook.jla.config.ConfigLoader;
import endless.overlook.jla.constants.JlaConfigConstants;
import endless.overlook.jla.constants.JlaConstants;
import endless.overlook.jla.constants.JlaNumberConstants;
import endless.overlook.jla.constants.JlaSymbolConstants;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Description:<b>文件遍历处理器</b>
 * <b>Warning:文件结尾至少预留两行空行，否则最后一个SqlEntity会丢失</b>
 *
 * @author Ralph
 * @since 2018-4-7 下午5:54:00
 */
public class IterationProcessor {

    /** 日志记录对象 **/
    private static final Logger logger = LoggerFactory
            .getLogger(IterationProcessor.class);

    /** 当前SQL的当前行号 **/
    private Integer currentLineOfEachSql = 1;

    /** SQL拼接对象 **/
    private StringBuffer plainSqlBuffer = new StringBuffer();

    /** SQL实体 **/
    private BusinessSqlEntity sqlEntity = new BusinessSqlEntity();

    /** 所有业务SQL实体集合 **/
    private List<BusinessSqlEntity> businessSqlEntityList = new CopyOnWriteArrayList<BusinessSqlEntity>();

    /** 配置加载器 **/
    private ConfigLoader configLoader;

    /** 头信息行数 **/
    private Integer headSqlNumber;

    /** JDBC文件类型：1·sqltiming;2·sqlonly **/
    private Integer jdbcFileType;

    /** 需要过滤的字符数组 **/
    private String[] excludeLinesArray;

    /** 时间格式 **/
    private final DateTimeFormatter dateTimeFormat = DateTimeFormat
            .forPattern("yyyy-MM-dd HH:mm:ss");

    /** 分析时间区间 **/
    private DateTime[] processTimePeriod = new DateTime[JlaNumberConstants.N_TWO];

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018-4-11 下午1:59:08
     * @param configLoader
     *              配置加载器
     */
    public IterationProcessor(ConfigLoader configLoader) {
        super();
        this.configLoader = configLoader;
        String jdbcFileTypeValue = configLoader
                .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_FILETYPE);
        jdbcFileType = JlaNumberConstants.N_TWO;
        if (StringUtils.isNotBlank(jdbcFileTypeValue) && StringUtils
                .isNumeric(jdbcFileTypeValue)) {
            jdbcFileType = Integer.valueOf(jdbcFileTypeValue);
        }
        String headSqlNumberValue = configLoader.getConfig(
                JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_HEADSQLNUMBERS);
        headSqlNumber = JlaNumberConstants.N_ONE;
        if (StringUtils.isNotBlank(headSqlNumberValue)) {
            headSqlNumber = Integer.valueOf(headSqlNumberValue);
        }
        String excludeLines = configLoader.getConfig(
                JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_EXCLUDELINES);
        excludeLinesArray = excludeLines
                .split(JlaSymbolConstants.C_SYMBOL_SEPERATOR_COMMAORSEMICOLON);
        String beginTimeString = configLoader
                .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_BEGINTIME);
        String endTimeString = configLoader
                .getConfig(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_ENDTIME);
        if (StringUtils.isNotBlank(beginTimeString) && StringUtils
                .isNotBlank(endTimeString)) {
            DateTime beginTime = null, endTime = null;
            try {
                beginTime = DateTime.parse(beginTimeString, dateTimeFormat);
                endTime = DateTime.parse(endTimeString, dateTimeFormat);
            } catch (IllegalArgumentException e) {
                logger.error("解析分析区间失败......", e);
            } catch (Exception e) {
                logger.error("解析分析区间失败......", e);
            }
            if (beginTime != null && endTime != null) {
                processTimePeriod[JlaNumberConstants.N_ZERO] = beginTime;
                processTimePeriod[JlaNumberConstants.N_ONE] = endTime;
                if (beginTime.isAfter(endTime)) {
                    ArrayUtils.reverse(processTimePeriod);
                }
            }
        }
    }

    /**
     * Description:<b>文件每一行的处理函数</b>
     * @author Ralph
     * @since 上午10:27:19
     * @param currentLine
     *              文件的每一行内容
     * @return
     *              是否继续处理下一行，因为是遍历，所以一定返回true
     * @throws IOException
     */
    public boolean processLine(String currentLine) {
        if (jdbcFileType != JlaNumberConstants.N_TWO && StringUtils
                .isBlank(currentLine)) {
            return true;
        }
        currentLine = StringUtils.trimToEmpty(currentLine);
        boolean ifContinueIteration = true;
        if (jdbcFileType
                .equals(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_FILETYPE_SQLTIMING)) {
            //sqltiming文件使用{executed in或者{FAILED after分隔每条SQL
            ifContinueIteration =
                    StringUtils.isNotBlank(currentLine) && !StringUtils
                            .startsWithIgnoreCase(currentLine, "{executed in")
                            && !StringUtils
                            .startsWithIgnoreCase(currentLine, "{FAILED after");
        } else {
            //sqlonly文件使用空行分隔每条SQL
            ifContinueIteration = StringUtils.isNotBlank(currentLine);
        }
        if (ifContinueIteration) {
            for (String excludeLine : excludeLinesArray) {
                if (StringUtils.isBlank(excludeLine)) {
                    continue;
                }
                if (StringUtils.contains(currentLine, excludeLine)) {
                    logger.warn("当前行[{}]包含应过滤字符[{}]，已被过滤......", currentLine,
                            excludeLine);
                    return true;
                }
            }
            //若指定了有效的统计区间，需要过滤
            if (processTimePeriod[JlaNumberConstants.N_ZERO] != null
                    && processTimePeriod[JlaNumberConstants.N_ONE] != null) {
                if (currentLineOfEachSql == JlaNumberConstants.N_ONE) {
                    String[] patternsArray = currentLine
                            .split(JlaSymbolConstants.C_SYMBOL_SPACE);
                    if (ArrayUtils.isNotEmpty(patternsArray)) {
                        String ymdPattern = patternsArray[JlaNumberConstants.N_ZERO];
                        String hmsPattern = patternsArray[JlaNumberConstants.N_ONE];
                        if (StringUtils.contains(hmsPattern,
                                JlaSymbolConstants.C_SYMBOL_DOT)) {
                            hmsPattern = StringUtils
                                    .substringBeforeLast(hmsPattern,
                                            JlaSymbolConstants.C_SYMBOL_DOT);
                        }
                        DateTime currentLineTime = null;
                        try {
                            currentLineTime = DateTime.parse(ymdPattern
                                    + JlaSymbolConstants.C_SYMBOL_SPACE
                                    + hmsPattern, dateTimeFormat);
                        } catch (IllegalArgumentException e) {
                            logger.error("解析当前SQL发出时间{}失败......", ymdPattern
                                    + JlaSymbolConstants.C_SYMBOL_SPACE
                                    + hmsPattern, e);
                        } catch (Exception e) {
                            logger.error("解析当前SQL发出时间{}失败......", ymdPattern
                                    + JlaSymbolConstants.C_SYMBOL_SPACE
                                    + hmsPattern, e);
                        }
                        if (currentLineTime != null) {
                            if (!((currentLineTime.isAfter(
                                    processTimePeriod[JlaNumberConstants.N_ZERO])
                                    || currentLineTime.isEqual(
                                    processTimePeriod[JlaNumberConstants.N_ZERO]))
                                    && (currentLineTime.isBefore(
                                    processTimePeriod[JlaNumberConstants.N_ONE])
                                    || currentLineTime.isEqual(
                                    processTimePeriod[JlaNumberConstants.N_ONE])))) {
                                sqlEntity.setIsFiltered(true);
                                currentLineOfEachSql++;
                                return true;
                            }
                        }
                    }
                }
            }
            if (currentLineOfEachSql <= headSqlNumber) {
                //TODO SQL头信息为多行时，信息会拼接不全，但是不影响主要展示信息，暂不处理
                sqlEntity.setHeadInfo(currentLine);
            } else if (currentLineOfEachSql
                    == headSqlNumber + JlaNumberConstants.N_ONE) {
                //存在具体业务SQL开头没有连接号的情况
                String firstWord = StringUtils.trimToEmpty(StringUtils
                        .substringBefore(currentLine,
                                JlaSymbolConstants.C_SYMBOL_DOT));
                if (StringUtils.isNumeric(firstWord)) {
                    //去掉业务SQL前面的连接号
                    plainSqlBuffer.append(
                            /**
                             * 因为每一行都进行了trimToEmpty，若上一行结尾是表名，下一行开始是简称
                             * 不加空格会改变业务表表名，所以每行拼接时提前加一个空格
                             */
                            JlaSymbolConstants.C_SYMBOL_SPACE + StringUtils
                                    .substring(currentLine, StringUtils
                                            .indexOfIgnoreCase(currentLine,
                                                    ". ")
                                            + JlaNumberConstants.N_TWO));
                } else {
                    plainSqlBuffer.append(currentLine);
                }
            } else {
                plainSqlBuffer.append(JlaSymbolConstants.C_SYMBOL_SPACE
                        + currentLine);
            }
            currentLineOfEachSql++;
        } else {
            //被过滤的SQL实体不处理
            if (sqlEntity.getIsFiltered()) {
                sqlEntity = new BusinessSqlEntity();
                plainSqlBuffer = new StringBuffer();
                currentLineOfEachSql = JlaNumberConstants.N_ONE;
                return true;
            }
            sqlEntity.setPlainSql(
                    StringUtils.trimToEmpty(plainSqlBuffer.toString()));

            //解析SQL执行耗时统计
            parseDuration(currentLine, sqlEntity);
            //解析业务表表名
            parseTableName(sqlEntity);

            businessSqlEntityList.add(sqlEntity);
            sqlEntity = new BusinessSqlEntity();
            plainSqlBuffer = new StringBuffer();
            currentLineOfEachSql = JlaNumberConstants.N_ONE;
        }
        return true;
    }

    /**
     * Description:<b>解析SQL执行耗时统计</b>
     * @author Ralph
     * @since 2018年10月15日 下午4:47:58
     * @param currentLine
     *              SQL当前行内容
     * @param sqlEntity
     *              SQL实体
     */
    private void parseDuration(String currentLine,
            BusinessSqlEntity sqlEntity) {
        if (jdbcFileType
                .equals(JlaConfigConstants.C_KEY_JLA_JDBCLOGFILES_FILETYPE_SQLTIMING)) {
            String currentDuration = StringUtils.trimToEmpty(currentLine)
                    .split(JlaSymbolConstants.C_SYMBOL_SPACE)[JlaNumberConstants.N_TWO];
            if (StringUtils.isNotBlank(currentDuration) && StringUtils
                    .isNumeric(currentDuration)) {
                Long currentDurationTime = Long.valueOf(currentDuration);
                sqlEntity.setDurationTime(currentDurationTime);
            }
        }
    }

    /**
     * Description:<b>解析业务表表名</b>
     * @author Ralph
     * @since 2018年10月15日 下午2:17:43
     */
    private void parseTableName(BusinessSqlEntity sqlEntity) {
        if (StringUtils.isBlank(sqlEntity.getPlainSql())) {
            /**
             * 日志中若存在Exception异常
             * 异常堆栈信息(at net.sourceforge.jtds.jdbc.SQLDiagnostic)上面
             * 可能会有一个空行，此空行会被解析为一个空的SqlEntity，需要过滤掉
             */
            logger.warn("SqlEntity中的SQL为空,日志文件中存在异常信息,已被过滤……");
            return;
        }
        if (StringUtils
                .startsWithIgnoreCase(sqlEntity.getPlainSql(), "BATCHING")
                || StringUtils
                .startsWithIgnoreCase(sqlEntity.getPlainSql(), "SET")
                || StringUtils
                .startsWithIgnoreCase(sqlEntity.getPlainSql(), "{CALL")
                || StringUtils
                .startsWithIgnoreCase(sqlEntity.getPlainSql(), "EXEC")
                || StringUtils
                .containsIgnoreCase(sqlEntity.getPlainSql(), "ESCAPE")) {
            //手动解析业务表表名
            setTableNameManually(sqlEntity);
        } else {
            //通过JSqlparser解析业务表表名
            setTableNameViaJSqlParser(sqlEntity);
        }
    }

    /**
     * Description:<b>通过JSqlParser提取业务表表名</b>
     * @author Ralph
     * @since 2018年10月15日 下午2:15:24
     * @param sqlEntity
     *              SQL实体
     */
    private void setTableNameViaJSqlParser(BusinessSqlEntity sqlEntity) {
        try {
            Statement statement = CCJSqlParserUtil
                    .parse(sqlEntity.getPlainSql());
            String operateTableNames = StringUtils.EMPTY;
            if (statement instanceof Insert) {
                Insert insertStatement = (Insert) statement;
                Table table = insertStatement.getTable();
                operateTableNames = StringUtils
                        .join(table, JlaSymbolConstants.C_SYMBOL_COMMA);
            } else if (statement instanceof Delete) {
                Delete deleteStatement = (Delete) statement;
                Table table = deleteStatement.getTable();
                operateTableNames = StringUtils
                        .join(table, JlaSymbolConstants.C_SYMBOL_COMMA);
            } else if (statement instanceof Truncate) {
                Truncate truncateStatement = (Truncate) statement;
                Table table = truncateStatement.getTable();
                operateTableNames = StringUtils
                        .join(table, JlaSymbolConstants.C_SYMBOL_COMMA);
            } else if (statement instanceof Update) {
                Update updateStatement = (Update) statement;
                List<Table> tableNameList = (List<Table>) updateStatement
                        .getTables();
                operateTableNames = StringUtils
                        .join(tableNameList, JlaSymbolConstants.C_SYMBOL_COMMA);
            } else if (statement instanceof Select) {
                TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                List<String> tableNames = tablesNamesFinder
                        .getTableList(statement);
                operateTableNames = StringUtils
                        .join(tableNames, JlaSymbolConstants.C_SYMBOL_COMMA);
            }
            sqlEntity.setTableName(operateTableNames);
            sqlEntity.setParserType(
                    JlaConstants.C_PROPERTY_PARSERTYPE_JSQLPARSER);
        } catch (JSQLParserException e) {
            logger.warn("自动解析SQL提取业务表名失败！手动提取......\n{}",
                    sqlEntity.getPlainSql(), e);
            setTableNameManually(sqlEntity);
            logger.warn("手动提取SQL[{}]的业务表表名[{}]......", sqlEntity.getPlainSql(),
                    sqlEntity.getTableName());
        }
    }

    /**
     * Description:<b>给SQL实体的表名字段赋值</b>
     * @author Ralph
     * @since 下午2:45:41
     * @param sqlEntity
     *              SQL实体
     */
    private void setTableNameManually(BusinessSqlEntity sqlEntity) {
        String tableName = StringUtils.EMPTY;
        String plainSql = StringUtils.trimToEmpty(plainSqlBuffer.toString());
        if (StringUtils.startsWithIgnoreCase(plainSql, "BATCHING")) {
            sqlEntity.setParserType(JlaConstants.C_PROPERTY_PARSERTYPE_BATCH);
            sqlEntity.setTableName(JlaConstants.C_PROPERTY_PARSERTYPE_BATCH);
            String[] batchSqlStringArray = plainSql
                    .split(" \\{WARNING: Statement used to run SQL\\} ");
            for (int i = JlaNumberConstants.N_ONE;
                 i < batchSqlStringArray.length; i++) {
                String batchSql = StringUtils.EMPTY;
                if (i == batchSqlStringArray.length
                        - JlaNumberConstants.N_ONE) {
                    batchSql = batchSqlStringArray[i];
                } else {
                    batchSql = StringUtils
                            .substringBeforeLast(batchSqlStringArray[i],
                                    JlaSymbolConstants.C_SYMBOL_SPACE);
                }
                BusinessSqlEntity subSqlEntity = new BusinessSqlEntity(
                        batchSql);
                //批量执行中的子SQL的头信息同Batch的头信息
                subSqlEntity.setHeadInfo(sqlEntity.getHeadInfo());
                //批量执行中的子SQL没有记录单条执行时间
                parseTableName(subSqlEntity);
                //放到结果集合List中，子SQL中操作表的次数也应该记录
                businessSqlEntityList.add(subSqlEntity);
            }
        } else if (StringUtils.startsWithIgnoreCase(plainSql, "SELECT")
                || StringUtils.startsWithIgnoreCase(plainSql, "SET ROWCOUNT")
                || StringUtils.startsWithIgnoreCase(plainSql,
                "SET TRANSACTION ISOLATION LEVEL")) {
            //TODO UNION可能是在子查询或者EXISTS子句中
            if (StringUtils.containsIgnoreCase(plainSql, "UNION") || StringUtils
                    .containsIgnoreCase(plainSql, "UNION ALL")) {
                String[] unionPatterns = plainSql.split("UNION ALL |UNION ");
                StringBuffer tableNameBuffer = new StringBuffer();
                for (String unionPatter : unionPatterns) {
                    tableNameBuffer.append(getTableNameBySelect(unionPatter));
                    tableNameBuffer.append(",");
                }
                tableName = StringUtils.trimToEmpty(tableNameBuffer.toString());
            } else {
                tableName = getTableNameBySelect(plainSql);
            }
        } else if (StringUtils.startsWithIgnoreCase(plainSql, "INSERT INTO")) {
            tableName = StringUtils
                    .substringBetween(plainSql, "INSERT INTO ", "(");
        } else if (StringUtils.startsWithIgnoreCase(plainSql, "UPDATE")) {
            tableName = StringUtils
                    .substringBetween(plainSql, "UPDATE ", "SET");
        } else if (StringUtils.startsWithIgnoreCase(plainSql, "DELETE")) {
            if (StringUtils.indexOfIgnoreCase(plainSql, "FROM ") != -1) {
                if (StringUtils.indexOfIgnoreCase(plainSql, "WHERE ") != -1) {
                    tableName = StringUtils
                            .substringBetween(plainSql, "FROM ", "WHERE ");
                } else {
                    tableName = StringUtils.substringAfter(plainSql, "FROM ");
                }
                //TODO 优化联表查询的情况
            }
        } else if (StringUtils.startsWithIgnoreCase(plainSql, "{CALL")) {
            tableName = StringUtils.substringBetween(plainSql, "{CALL ", "(");
        }
        if (StringUtils.isNotBlank(tableName)) {
            String[] tableNamePatterns = tableName.split(" ");
            StringBuffer plainTableNameBuffer = new StringBuffer();
            if (tableNamePatterns.length > 1) {
                for (String tableNamePattern : tableNamePatterns) {
                    if (StringUtils.isBlank(tableNamePattern)) {
                        continue;
                    }
                    if (StringUtils.contains(tableNamePattern,
                            JlaSymbolConstants.C_SYMBOL_COMMA)) {
                        //存在多表等值联查时，后面没有跟空格的情况，截取逗号后面的部分
                        tableNamePattern = StringUtils
                                .substringAfter(tableNamePattern,
                                        JlaSymbolConstants.C_SYMBOL_COMMA);
                    }
                    tableNamePattern = StringUtils.replace(tableNamePattern,
                            JlaSymbolConstants.C_SYMBOL_COMMA,
                            StringUtils.EMPTY);
                    if (StringUtils.indexOf(tableNamePattern, "DBO.") != -1) {
                        plainTableNameBuffer.append(StringUtils
                                .substringAfter(tableNamePattern, "DBO."));
                        plainTableNameBuffer.append(",");
                    } else if (StringUtils.indexOf(tableNamePattern, "dbo.")
                            != -1) {
                        plainTableNameBuffer.append(StringUtils
                                .substringAfter(tableNamePattern, "dbo."));
                        plainTableNameBuffer.append(",");
                    } else if (StringUtils.indexOf(tableNamePattern, "..")
                            != -1) {
                        plainTableNameBuffer.append(StringUtils
                                .substringAfter(tableNamePattern, ".."));
                        plainTableNameBuffer.append(",");
                    } else if (StringUtils
                            .indexOfIgnoreCase(tableNamePattern, "T_") != -1) {
                        plainTableNameBuffer.append(tableNamePattern);
                        plainTableNameBuffer.append(",");
                    }
                }
                sqlEntity.setTableName(StringUtils
                        .substringBeforeLast(plainTableNameBuffer.toString(),
                                ","));
                sqlEntity.setParserType(
                        JlaConstants.C_PROPERTY_PARSERTYPE_MANUAL);
            } else if (StringUtils.indexOfIgnoreCase(tableName, "DBO.") != -1) {
                sqlEntity.setTableName(
                        StringUtils.substringAfter(tableName, "DBO."));
                sqlEntity.setParserType(
                        JlaConstants.C_PROPERTY_PARSERTYPE_MANUAL);
            } else if (StringUtils.indexOfIgnoreCase(tableName, "..") != -1) {
                sqlEntity.setTableName(
                        StringUtils.substringAfter(tableName, ".."));
                sqlEntity.setParserType(
                        JlaConstants.C_PROPERTY_PARSERTYPE_MANUAL);
            } else {
                sqlEntity.setTableName(tableName);
                sqlEntity.setParserType(
                        JlaConstants.C_PROPERTY_PARSERTYPE_MANUAL);
            }
        }
    }

    /**
     * Description:<b>为SELECT类型操作获取操作的业务表名</b>
     * @author Ralph
     * @since 上午10:26:34
     * @param plainSql
     *              业务SQL
     * @return
     *              操作的业务表名
     */
    private String getTableNameBySelect(String plainSql) {
        String tableName = StringUtils.EMPTY;
        if (StringUtils.containsIgnoreCase(plainSql, "FROM ")) {
            //TODO  需要兼容EXISTS语句的情况
            if (StringUtils.containsIgnoreCase(plainSql, "WHERE ")) {
                tableName = getSelectTableName(plainSql, tableName);
                //TODO 暂时仅支持识别where子句下仅一层子查询的表名拼接
                Integer firstWhereIndex = StringUtils
                        .indexOfIgnoreCase(plainSql, "WHERE ");
                String whereClauseSql = StringUtils.substring(plainSql,
                        firstWhereIndex + JlaNumberConstants.N_SIX);
                tableName = getSelectTableName(whereClauseSql, tableName);
            } else if (StringUtils.containsIgnoreCase(plainSql, "ORDER BY ")) {
                tableName = StringUtils
                        .substringBetween(plainSql, "FROM ", "ORDER BY ");
            } else if (StringUtils.containsIgnoreCase(plainSql, "GROUP BY ")) {
                tableName = StringUtils
                        .substringBetween(plainSql, "FROM ", "GROUP BY ");
            } else {
                tableName = StringUtils.substringAfter(plainSql, "FROM ");
            }
            //TODO 优化联表查询的情况
        }
        return tableName;
    }

    /**
     * Description:<b>获取select语句中的表名</b>
     * @author Ralph
     * @since 2018-4-13 下午3:34:28
     * @param plainSql
     *              格式化后的SQL
     * @param tableName
     *              表名拼接字符串
     * @return
     *              拼接后的表名字段
     */
    private String getSelectTableName(String plainSql, String tableName) {
        if (StringUtils.containsIgnoreCase(plainSql, "WHERE ") && StringUtils
                .containsIgnoreCase(plainSql, "FROM ")) {
            Integer firstWhereIndex = StringUtils
                    .indexOfIgnoreCase(plainSql, "WHERE ");
            String selectfromClauseSql = StringUtils
                    .substring(plainSql, JlaNumberConstants.N_ZERO,
                            firstWhereIndex);
            if (StringUtils.isNotBlank(selectfromClauseSql)) {
                Integer lastFromIndex = StringUtils
                        .lastIndexOfIgnoreCase(selectfromClauseSql, "FROM ");
                //取整个SQL第一个where和它之前字句中最后一个select之间的为主查询的表名
                String thisTableName = StringUtils.substring(plainSql,
                        lastFromIndex + JlaNumberConstants.N_FIVE,
                        firstWhereIndex);
                //TODO 存在YWST.. T_ZX_AJ(点后面多个空格)的特殊情况
                if (StringUtils.containsIgnoreCase(thisTableName,
                        JlaSymbolConstants.C_SYMBOL_HASH)) {
                    //临时表的表名不拼接
                    return tableName;
                }
                if (StringUtils.isNotBlank(tableName)) {
                    return tableName + JlaSymbolConstants.C_SYMBOL_COMMA
                            + thisTableName;
                } else {
                    return thisTableName;
                }
            }
        }
        return tableName;
    }

    /**
     * Description:<b>返回遍历文件后的结果集合</b>
     * @author Ralph
     * @since 上午10:26:06
     * @return
     *              遍历文件后的结果集合
     */
    public List<BusinessSqlEntity> getResult() {
        return businessSqlEntityList;
    }

}
