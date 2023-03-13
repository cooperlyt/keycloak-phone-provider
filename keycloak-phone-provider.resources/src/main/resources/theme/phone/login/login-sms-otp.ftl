<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        ${msg("authCodePhoneNumber")}
    <#elseif section = "form">

        <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

        <div id="vue-app">
            <div class="alert-error ${properties.kcAlertClass!} pf-m-danger" v-show="errorMessage">
                <div class="pf-c-alert__icon">
                    <span class="${properties.kcFeedbackErrorIcon!}"></span>
                </div>

                <span class="${properties.kcAlertTitleClass!}">{{ errorMessage }}</span>
            </div>
            <div id="kc-form">
                <div id="kc-form-wrapper">
                    <form id="kc-form-login" action="${url.loginAction}" method="post">


                        <div class="${properties.kcFormGroupClass!} row">
                            <div class="${properties.kcLabelWrapperClass!}" style="padding: 0">
                                <label for="code"
                                       class="${properties.kcLabelClass!}">${msg("authenticationCode")}</label>
                            </div>
                            <div class="col-xs-8" style="padding: 0 5px 0 0">
                                <input tabindex="0" id="code" class="${properties.kcInputClass!}" name="code"
                                       type="text" autofocus
                                       autocomplete="off"/>
                            </div>
                            <div class="col-xs-4" style="padding: 0 0 0 5px">
                                <input tabindex="0" style="height: 36px"
                                       class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                       type="button" v-model="sendButtonText"
                                       :disabled='sendButtonText !== initSendButtonText'
                                       v-on:click="sendVerificationCode()"/>
                            </div>
                        </div>

                        <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                            <input type="hidden" id="id-hidden-input" name="credentialId"
                                   <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                            <input tabindex="0"
                                   class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                   name="save" id="kc-login" type="submit" value="${msg("doSubmit")}"/>
                        </div>
                    </form>
                </div>
            </div>

        </div>

        <script type="text/javascript" >



            function req(phoneNumber) {
                const params = {params: {phoneNumber}}
                axios.get(window.location.origin + '/realms/${realm.name}/sms/otp-code', params)
                    .then(res => app.disableSend(res.data.expires_in))
                    .catch(e => app.errorMessage = e.response.data.error);
            }

            var app = new Vue({
                el: '#vue-app',
                data: {
                    errorMessage: '',
                    phoneNumber: '${attemptedPhoneNumber!}',
                    sendButtonText: '${msg("sendVerificationCode")}',
                    initSendButtonText: '${msg("sendVerificationCode")}',
                    disableSend: function (seconds) {
                        if (seconds <= 0) {
                            app.sendButtonText = app.initSendButtonText;
                        } else {
                            const minutes = Math.floor(seconds / 60) + '';
                            const seconds_ = seconds % 60 + '';
                            app.sendButtonText = String(minutes.padStart(2, '0') + ":" + seconds_.padStart(2, '0'));
                            setTimeout(function () {
                                app.disableSend(seconds - 1);
                            }, 1000);
                        }
                    },
                    sendVerificationCode: function () {
                        if (!this.phoneNumber) {
                            this.errorMessage = '${msg("requiredPhoneNumber")}';
                            document.getElementById('phoneNumber').focus();
                            return;
                        }
                        if (this.sendButtonText !== this.initSendButtonText) {
                            return;
                        }
                        req(this.phoneNumber);

                    }
                }
            });


            <#if initSend??>
            window.addEventListener('load', function () {
                app.disableSend(${expires});
            })
            </#if>

        </script>
    <#elseif section = "info">
        ${msg("authCodeInfo")}
    </#if>
</@layout.registrationLayout>
