package com.promo.otp.service;

import org.jsmpp.bean.*;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class SmsService {
    private static final Logger logger = LogManager.getLogger(SmsService.class);
    private final String host;
    private final int port;
    private final String systemId;
    private final String password;
    private final String systemType;
    private final String sourceAddress;

    public SmsService() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("sms.properties")) {
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load sms.properties", e);
        }
        host = props.getProperty("smpp.host");
        port = Integer.parseInt(props.getProperty("smpp.port"));
        systemId = props.getProperty("smpp.system_id");
        password = props.getProperty("smpp.password");
        systemType = props.getProperty("smpp.system_type");
        sourceAddress = props.getProperty("smpp.source_addr");
    }

    public void sendCode(String phoneNumber, String code) {
        SMPPSession session = new SMPPSession();
        try {
            BindParameter bindParam = new BindParameter(
                    BindType.BIND_TX,
                    systemId,
                    password,
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress
            );
            session.connectAndBind(host, port, bindParam);
            String message = "Your OTP code: " + code;
            session.submitShortMessage(
                    systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    sourceAddress,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    phoneNumber,
                    new ESMClass(),
                    (byte) 0,
                    (byte) 1,
                    null,
                    null,
                    new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT),
                    (byte) 0,
                    new GeneralDataCoding(Alphabet.ALPHA_DEFAULT),
                    (byte) 0,
                    message.getBytes(StandardCharsets.UTF_8)
            );
            logger.info("SMS sent to {} with code {}", phoneNumber, code);
        } catch (Exception e) {
            logger.error("Failed to send SMS", e);
        } finally {
            if (session != null && session.getSessionState().isBound()) {
                session.unbindAndClose();
            }
        }
    }
}