package org.wso2.dpw;

import com.google.gson.Gson;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.milyn.edi.unedifact.d04a.CUSDEC.Cusdec;
import org.milyn.edi.unedifact.d04a.D04AInterchangeFactory;
import org.milyn.smooks.edi.unedifact.model.r41.UNEdifactInterchange41;
import org.milyn.smooks.edi.unedifact.model.r41.UNEdifactMessage41;
import org.milyn.smooks.edi.unedifact.model.r41.UNH41;
import org.milyn.smooks.edi.unedifact.model.r41.UNT41;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonToEdi extends AbstractMediator {

    public boolean mediate(MessageContext context) {
        try {
            OMElement first = context.getEnvelope().getBody().getFirstElement();

            first.buildWithAttachments();
            String st = JsonUtil.jsonPayloadToString(((Axis2MessageContext) context).getAxis2MessageContext());
            Gson gson = new Gson();
            Cusdec cusDecMessage = gson.fromJson(st, Cusdec.class);

            List<UNEdifactMessage41> messages = new ArrayList<>();
            UNEdifactMessage41 message = new UNEdifactMessage41();

            UNH41 uNH41 = new UNH41();
            uNH41.setMessageRefNum("test");
            message.setMessageHeader(uNH41);

            UNT41 uNT41 = new UNT41();
            message.setMessageTrailer(uNT41);

            message.setMessage(cusDecMessage);
            messages.add(message);

            UNEdifactInterchange41 interchange = new UNEdifactInterchange41();
            interchange.setMessages(messages);
            D04AInterchangeFactory factory = D04AInterchangeFactory.getInstance();
            StringWriter ediOutStream = new StringWriter();
            factory.toUNEdifact(interchange, ediOutStream);

            Iterator itr = context.getEnvelope().getBody().getChildElements();
            while (itr.hasNext()) {
                OMElement element = (OMElement) itr.next();
                element.detach();
            }
            JsonUtil.removeJsonStream(((Axis2MessageContext) context).getAxis2MessageContext());
            String om = ediOutStream.toString();
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement tt = fac.createOMElement(BaseConstants.DEFAULT_TEXT_WRAPPER);
            tt.setText(om);
            context.getEnvelope().getBody().addChild(tt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
