# Twilio SMS Sender Provider

To set up Twilio message sender, one has to edit `standalone.xml` (or equivalent) including lines earlier mentioned and these:

```xml
<spi name="messageSenderService">
...
    <provider name="Twilio" enabled="true">
        <properties>
            <property name="accountSid" value="YOUR_ACCOUNT_SID_HERE"/>
            <property name="authToken" value="YOUR_AUTH_TOKEN_HERE"/>
            <property name="twilioPhoneNumber" value="YOUR_PURCHASED_TWILIO_NUMBER"/>
        </properties>
    </provider>
</spi>
```