package nc.ref.saas.pub.tree;

import java.util.Map;

import nc.jdbc.framework.processor.ColumnProcessor;
import nc.ref.saas.pub.AbstractRefDataModal;

/**
 * 
 * @author changlei
 * 
 * ���β�����Ҫʵ�ֵĳ�����
 *
 */
public abstract class AbstractRefTreeDataModal extends AbstractRefDataModal
{
    /**
     * ���β�������ʵ�ִ˷���
     */
    public int getTotal(Map<String,String> paramMap)throws Exception{
        return -1;
    }
   
}
