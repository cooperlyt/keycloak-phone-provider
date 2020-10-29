# TotalVoice SMS Sender Provider

To set up TotalVoice message sender, one has to edit `standalone.xml` (or equivalent) including lines earlier mentioned and these:

```xml
<spi name="messageSenderService">
...
    <provider name="TotalVoice" enabled="true">
        <properties>
            <property name="authToken" value="YOUR_AUTH_TOKEN"/>
        </properties>
    </provider>
</spi>
```