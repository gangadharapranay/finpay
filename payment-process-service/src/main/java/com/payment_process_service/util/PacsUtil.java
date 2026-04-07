package com.payment_process_service.util;

import com.payment_process_service.model.Payment;
import com.payment_process_service.xml.Pacs002;
import com.payment_process_service.xml.Pacs008;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.time.LocalDateTime;

@Component
public class PacsUtil {
    public String generatePacs002(Payment payment, Pacs008 pacs008, String status, String reason) {
        try {
            Pacs002 pacs002 = Pacs002.builder()
                    .grpHdr(Pacs002.GroupHeader.builder()
                            .msgId("status-" + payment.getId().toString())
                            .creDtTm(LocalDateTime.now())
                            .nbOfTxs(1)
                            .build())
                    .originalPaymentInfo(Pacs002.OriginalPaymentInfo.builder()
                            .originalMsgId(pacs008.getGrpHdr().getMsgId())
                            .originalInstrId(pacs008.getPmtInf().getInstrId())
                            .txSts(status)
                            .reason(reason)
                            .build())
                    .build();

            JAXBContext context = JAXBContext.newInstance(Pacs002.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter sw = new StringWriter();
            marshaller.marshal(pacs002, sw);
            return sw.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate pacs.002 XML", e);
        }
    }
}
