package com.payment_api_service.xml;


import com.payment_api_service.util.LocalDateTimeAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.*;

import java.time.LocalDateTime;

@XmlRootElement(name = "Document")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pacs002 {

    @XmlElement(name = "GrpHdr")
    private GroupHeader grpHdr;

    @XmlElement(name = "OrgnlPmtInfAndSts")
    private OriginalPaymentInfo originalPaymentInfo;

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupHeader {
        private String msgId;
        @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
        private LocalDateTime creDtTm;
        private int nbOfTxs;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OriginalPaymentInfo {
        private String originalMsgId;   // msgId of pacs.008
        private String originalInstrId; // instrId of pacs.008
        private String txSts;           // ACSP, RJCT, PDNG
        private String reason;          // optional failure reason
    }
}

