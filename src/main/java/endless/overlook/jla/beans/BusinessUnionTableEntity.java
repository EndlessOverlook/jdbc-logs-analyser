package endless.overlook.jla.beans;

/**
 * Description:<b>业务表统计实体(联表查询时合并统计，未拆开)</b>
 *
 * @author Ralph
 * @since 2018-4-19 下午8:22:34
 */
public class BusinessUnionTableEntity {

    /** 表命中次数 **/
    private Integer tableHitCount = 1;

    /** 业务表表名 **/
    private String businessTableName;

    /** 解析器类型 **/
    private String parserType;

    public Integer getTableHitCount() {
        return tableHitCount;
    }

    public void setTableHitCount(Integer tableHitCount) {
        this.tableHitCount = tableHitCount;
    }

    public void increaseCount() {
        this.tableHitCount++;
    }

    public String getBusinessTableName() {
        return businessTableName;
    }

    public void setBusinessTableName(String businessTableName) {
        this.businessTableName = businessTableName;
    }

    public String getParserType() {
        return parserType;
    }

    public void setParserType(String parserType) {
        this.parserType = parserType;
    }

    public BusinessUnionTableEntity(String businessTableName,
            String parserType) {
        super();
        this.businessTableName = businessTableName;
        this.parserType = parserType;
    }

}
