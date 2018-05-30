package kz.bsbnb.usci.core.model;

import java.util.List;

public class EavXmlDmlBasket {
    private List<EavXmlEntity> rows;

    public EavXmlDmlBasket() {
        /*An empty constructor*/
    }

    //region Getters and Setters

    public List<EavXmlEntity> getRows() {
        return rows;
    }

    public void setRows(List<EavXmlEntity> rows) {
        this.rows = rows;
    }

    //endregion

}
