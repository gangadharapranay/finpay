package com.payment_process_service.xml;

import jakarta.xml.bind.annotation.*;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupHeader {
        private String msgId;
        private LocalDateTime creDtTm;
        private int nbOfTxs;
    }

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
