package com.payment_process_service.xml;

import com.payment_process_service.util.LocalDateTimeAdapter;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@XmlRootElement(name = "Document")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pacs008 {

    @XmlElement(name = "GrpHdr")
    private GroupHeader grpHdr;

    @XmlElement(name = "PmtInf")
    private PaymentInfo pmtInf;

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
    public static class PaymentInfo {
        private String pmtInfId;
        private String instrId;
        private BigDecimal amt;
        private String ccy;
        private String dbtr;
        private String cdtr;
    }
}
