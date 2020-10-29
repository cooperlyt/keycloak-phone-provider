package cc.coopersoft.keycloak.providers.sms.spi.impl;

import cc.coopersoft.keycloak.providers.sms.spi.TokenCodeService;
import cc.coopersoft.keycloak.providers.sms.constants.TokenCodeType;
import cc.coopersoft.keycloak.providers.sms.exception.MessageSendException;
import cc.coopersoft.keycloak.providers.sms.representations.TokenCodeRepresentation;
import cc.coopersoft.keycloak.providers.sms.spi.MessageSenderService;
import cc.coopersoft.keycloak.providers.sms.spi.PhoneMessageService;
import org.jboss.logging.Logger;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.ServiceUnavailableException;
import java.time.Instant;

public class PhoneMessageServiceImpl implements PhoneMessageService {

    private static final Logger logger = Logger.getLogger(PhoneMessageServiceImpl.class);
    private final KeycloakSession session;
    private final String realmName;
    private final String service;
    private final int tokenExpiresIn;

    PhoneMessageServiceImpl(KeycloakSession session, Scope config) {
        this.session = session;
        this.realmName = session.getContext().getRealm().getName();
        this.service = session.listProviderIds(MessageSenderService.class)
                .stream().filter(s -> s.equals(config.get("service")))
                .findFirst().orElse(
                        session.listProviderIds(MessageSenderService.class)
                                .stream().findFirst().orElse("")
                );
        this.tokenExpiresIn = config.getInt("tokenExpiresIn", 60);
    }

    @Override
    public void close() {
    }

    private TokenCodeService getTokenCodeService() {
        return session.getProvider(TokenCodeService.class);
    }

    @Override
    public int sendVerificationCode(String phoneNumber) {

        if (getTokenCodeService().isAbusing(phoneNumber, TokenCodeType.VERIFY)) {
            throw new ForbiddenException("You requested the maximum number of messages the last hour");
        }

        TokenCodeRepresentation ongoing = getTokenCodeService().ongoingProcess(phoneNumber, TokenCodeType.VERIFY);
        if (ongoing != null) {
            logger.info(String.format("No need of sending a new verification code for %s", phoneNumber));
            return (int) (ongoing.getExpiresAt().getTime() - Instant.now().toEpochMilli()) / 1000;
        }

        TokenCodeRepresentation token = TokenCodeRepresentation.forPhoneNumber(phoneNumber);
        //final String MESSAGE = String.format("%s - verification code: %s", realmName, token.getCode());

        try {
            session.getProvider(MessageSenderService.class, service).sendMessage(TokenCodeType.VERIFY,realmName,phoneNumber,token.getCode(),tokenExpiresIn);
            //session.getProvider(MessageSenderService.class, service).sendMessage(phoneNumber, MESSAGE);
            getTokenCodeService().persistCode(token, TokenCodeType.VERIFY, tokenExpiresIn);

            logger.info(String.format("Sent verification code to %s over %s", phoneNumber, service));

        } catch (MessageSendException e) {

            logger.error(String.format("Message sending to %s failed with %s: %s",
                    phoneNumber, e.getErrorCode(), e.getErrorMessage()));
            throw new ServiceUnavailableException("Internal server error");
        }

        return tokenExpiresIn;
    }

    @Override
    public int sendAuthenticationCode(String phoneNumber) {

        if (getTokenCodeService().isAbusing(phoneNumber, TokenCodeType.OTP)) {
            throw new ForbiddenException("You requested the maximum number of messages the last hour");
        }

        TokenCodeRepresentation ongoing = getTokenCodeService().ongoingProcess(phoneNumber, TokenCodeType.OTP);
        if (ongoing != null) {
            logger.info(String.format("No need of sending a new OTP code for %s", phoneNumber));
            return (int) (ongoing.getExpiresAt().getTime() - Instant.now().toEpochMilli()) / 1000;
        }

        TokenCodeRepresentation token = TokenCodeRepresentation.forPhoneNumber(phoneNumber);
        //final String MESSAGE = String.format("%s - authentication code: %s", realmName, token.getCode());

        try {
            session.getProvider(MessageSenderService.class, service).sendMessage(TokenCodeType.OTP,realmName,phoneNumber,token.getCode(),tokenExpiresIn);
            //session.getProvider(MessageSenderService.class, service).sendMessage(phoneNumber, MESSAGE);
            getTokenCodeService().persistCode(token, TokenCodeType.OTP, tokenExpiresIn);

            logger.info(String.format("Sent OTP code to %s over %s", phoneNumber, service));

        } catch (MessageSendException e) {

            logger.error(String.format("Message sending to %s failed with %s: %s",
                    phoneNumber, e.getErrorCode(), e.getErrorMessage()));
            throw new ServiceUnavailableException("Internal server error");
        }

        return tokenExpiresIn;
    }
}
