package cc.coopersoft.keycloak.phone.providers.spi;

import org.keycloak.provider.Spi;

public class PhoneSpi implements Spi {

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getName() {
        return "phone";
    }

    @Override
    public Class<PhoneProvider> getProviderClass() {
        return PhoneProvider.class;
    }

    @Override
    public Class<PhoneProviderFactory> getProviderFactoryClass() {
        return PhoneProviderFactory.class;
    }
}
