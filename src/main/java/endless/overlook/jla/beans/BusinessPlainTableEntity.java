package endless.overlook.jla.beans;

/**
 * Description:<b>单个业务表统计实体</b>
 *
 * @author Ralph
 * @since 2018-4-20 下午7:41:38
 */
public class BusinessPlainTableEntity {

    /** 业务表名 **/
    private String tableName;

    /** 总命中统计 **/
    private Long hitCount = 1l;

    /** SELECT操作命中统计 **/
    private Long selectHitCount = 0l;

    /** UDPATE操作命中统计 **/
    private Long updateHitCount = 0l;

    /** DELETE操作命中统计 **/
    private Long deleteHitCount = 0l;

    /** INSERT操作命中统计 **/
    private Long insertHitCount = 0l;

    public BusinessPlainTableEntity(String tableName) {
        super();
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public Long getHitCount() {
        return hitCount;
    }

    public void setHitCount(Long hitCount) {
        this.hitCount = hitCount;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getSelectHitCount() {
        return selectHitCount;
    }

    public void setSelectHitCount(Long selectHitCount) {
        this.selectHitCount = selectHitCount;
    }

    public Long getUpdateHitCount() {
        return updateHitCount;
    }

    public void setUpdateHitCount(Long updateHitCount) {
        this.updateHitCount = updateHitCount;
    }

    public Long getDeleteHitCount() {
        return deleteHitCount;
    }

    public void setDeleteHitCount(Long deleteHitCount) {
        this.deleteHitCount = deleteHitCount;
    }

    public Long getInsertHitCount() {
        return insertHitCount;
    }

    public void setInsertHitCount(Long insertHitCount) {
        this.insertHitCount = insertHitCount;
    }

    public void increaseCount() {
        this.hitCount++;
    }

    public void increaseSelectCount() {
        this.selectHitCount++;
    }

    public void increaseUpdateCount() {
        this.updateHitCount++;
    }

    public void increaseDeleteCount() {
        this.deleteHitCount++;
    }

    public void increaseInsertCount() {
        this.insertHitCount++;
    }

}
