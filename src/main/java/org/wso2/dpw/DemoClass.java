package org.wso2.dpw;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.milyn.Smooks;
import org.milyn.payload.ByteSource;
import org.milyn.smooks.edi.unedifact.UNEdifactReaderConfigurator;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import javax.xml.transform.stream.StreamResult;

public class DemoClass extends AbstractMediator {

    public boolean mediate(MessageContext context) {
        try {
            Smooks smooks = new Smooks();
//            smooks.setReaderConfig(new UNEdifactReaderConfigurator("urn:org.milyn.edi.unedifact:d03b-mapping:*"));
            smooks.setReaderConfig(
                    new UNEdifactReaderConfigurator("//Users//gihan//Desktop//d04a-mapping-1.7.1.0.jar"));
            try {
                StringWriter writer = new StringWriter();
                String edi = "";
                OMElement first = context.getEnvelope().getBody().getFirstElement();
                if (first != null) {
                    first.buildWithAttachments();
                    edi = first.getText();
                }
                smooks.filterSource(new ByteSource(edi.getBytes(StandardCharsets.US_ASCII)), new StreamResult(writer));
                Iterator itr = context.getEnvelope().getBody().getChildElements();
                while (itr.hasNext()) {
                    OMElement element = (OMElement) itr.next();
                    element.detach();
                }
                OMElement newOne = AXIOMUtil.stringToOM(writer.toString());
                context.getEnvelope().getBody().addChild(newOne);
            } finally {
                smooks.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
