package cc.coopersoft.keycloak.phone.providers.spi;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class PhoneMessageServiceSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "phoneMessageService";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return PhoneMessageService.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return PhoneMessageServiceProviderFactory.class;
    }
}
