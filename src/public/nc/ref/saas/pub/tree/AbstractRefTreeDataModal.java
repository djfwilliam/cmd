package nc.ref.saas.pub.tree;

import java.util.Map;

import nc.jdbc.framework.processor.ColumnProcessor;
import nc.ref.saas.pub.AbstractRefDataModal;

/**
 * 
 * @author changlei
 * 
 * 树形参照需要实现的抽象类
 *
 */
public abstract class AbstractRefTreeDataModal extends AbstractRefDataModal
{
    /**
     * 树形参照无需实现此方法
     */
    public int getTotal(Map<String,String> paramMap)throws Exception{
        return -1;
    }
   
}
