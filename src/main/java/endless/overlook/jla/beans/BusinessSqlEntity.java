package endless.overlook.jla.beans;

import org.apache.commons.lang3.StringUtils;

/**
 * Description:<b>SQL统计实体</b>
 *
 * @author Ralph
 * @since 2018-4-19 下午8:22:16
 */
public class BusinessSqlEntity {

    /** 出现次数 **/
    private Integer sqlCount = 1;

    /** 头信息 **/
    private String headInfo;

    /** 真正的业务SQL **/
    private String plainSql;

    /** 查询的表名 **/
    private String tableName;

    /** 执行耗时 **/
    private Long durationTime = 0l;

    /** 是否已被过滤 **/
    private Boolean isFiltered = false;

    /** 解析器类型:1·JSqlParser;2·Manual **/
    private String parserType;

    public Integer getSqlCount() {
        return sqlCount;
    }

    public void setSqlCount(Integer sqlCount) {
        this.sqlCount = sqlCount;
    }

    public void increaseCount() {
        this.sqlCount++;
    }

    public String getHeadInfo() {
        return headInfo;
    }

    public void setHeadInfo(String headInfo) {
        this.headInfo = headInfo;
    }

    public String getPlainSql() {
        return plainSql;
    }

    public void setPlainSql(String plainSql) {
        this.plainSql = StringUtils.replace(plainSql, "DBO.", "dbo.");
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = StringUtils.trimToEmpty(tableName);
    }

    public Long getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(Long durationTime) {
        this.durationTime = durationTime;
    }

    public Boolean getIsFiltered() {
        return isFiltered;
    }

    public void setIsFiltered(Boolean isFiltered) {
        this.isFiltered = isFiltered;
    }

    public String getParserType() {
        return parserType;
    }

    public void setParserType(String parserType) {
        this.parserType = parserType;
    }

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018年10月15日 下午4:44:54
     * @param plainSql
     */
    public BusinessSqlEntity() {
        super();
    }

    /**
     * Description:<b>构造函数</b>
     *
     * @author Ralph
     * @since 2018年10月15日 下午4:43:48
     * @param plainSql
     *              SQL语句
     */
    public BusinessSqlEntity(String plainSql) {
        super();
        this.plainSql = plainSql;
    }

}
