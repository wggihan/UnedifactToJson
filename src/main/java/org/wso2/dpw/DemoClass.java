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
            smooks.setReaderConfig(new UNEdifactReaderConfigurator("urn:org.milyn.edi.unedifact:d04a-mapping:*"));
            try {
                StringWriter writer = new StringWriter();
                OMElement first = context.getEnvelope().getBody().getFirstElement();
                first.buildWithAttachments();
                String edi = first.getText();
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
