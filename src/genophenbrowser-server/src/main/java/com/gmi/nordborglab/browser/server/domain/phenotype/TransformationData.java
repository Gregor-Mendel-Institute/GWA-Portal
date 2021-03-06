package com.gmi.nordborglab.browser.server.domain.phenotype;

import com.gmi.nordborglab.browser.server.math.Transformations;
import com.gmi.nordborglab.browser.shared.proxy.TransformationDataProxy;
import com.gmi.nordborglab.browser.shared.util.Normality;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 2/25/13
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransformationData {


    private TransformationDataProxy.TYPE type;
    private List<Double> values;
    private Double shapiroPval;
    private Double pseudoHeritability;


    public TransformationData() {

    }

    public TransformationData(TransformationDataProxy.TYPE type, List<Double> values, Double pseudoHeritability) {
        this.type = type;
        this.values = values;
        this.pseudoHeritability = pseudoHeritability;
    }

    public TransformationDataProxy.TYPE getType() {
        return type;
    }

    public void setType(TransformationDataProxy.TYPE type) {
        this.type = type;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }


    public Double getShapiroPval() {
        if (shapiroPval == null)
        {
            shapiroPval = Transformations.calculateShapiroPval(values);
            if (shapiroPval > 0.0) {
                shapiroPval = Normality.getRoundedValue(Normality.calculateScoreFromPValue(shapiroPval));
            }
        }
        return shapiroPval;
    }

    public void setShapiroPval(Double shapiroPval) {
        this.shapiroPval = shapiroPval;
    }

    public Double getPseudoHeritability() {
        return pseudoHeritability;
    }

    public void setPseudoHeritability(Double pseudoHeritability) {
        this.pseudoHeritability = pseudoHeritability;
    }
}
