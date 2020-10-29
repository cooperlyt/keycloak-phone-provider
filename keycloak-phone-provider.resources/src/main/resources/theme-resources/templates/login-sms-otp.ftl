<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true displayWide=false; section>
    <#if section = "header">
        ${msg("authCodePhoneNumber")}
    <#elseif section = "form">

      <div id="kc-form">
        <div id="kc-form-wrapper">
          <form id="kc-form-login" action="${url.loginAction}" method="post">
            <div class="${properties.kcFormGroupClass!} row">
              <label for="code" class="${properties.kcLabelClass!}">${msg("authenticationCode")}</label>
              <input tabindex="1" id="code" class="${properties.kcInputClass!}" name="code"
                     type="text" autofocus
                     autocomplete="off"/>
            </div>
            <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
              <input type="hidden" id="id-hidden-input" name="credentialId"
                     <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
              <input tabindex="2"
                     class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                     name="save" id="kc-login" type="submit" value="${msg("doSubmit")}"/>
            </div>
          </form>
        </div>
      </div>
    <#elseif section = "info">
        ${msg("authCodeInfo")}
    </#if>
</@layout.registrationLayout>
