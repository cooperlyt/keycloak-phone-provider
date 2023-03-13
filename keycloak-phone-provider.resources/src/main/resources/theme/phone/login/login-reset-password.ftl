<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true displayMessage=!messagesPerField.existsError('username','code','phoneNumber'); section>
    <#if section = "header">
        ${msg("emailForgotTitle")}
    <#elseif section = "form">

        <#if supportPhone??>
            <script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

            <style>
                [v-cloak] > * {
                    display: none;
                }

                [v-cloak]::before {
                    content: "loading...";
                }
            </style>
        </#if>

        <div id="vue-app">
            <div v-cloak>
                <form id="kc-reset-password-form" class="${properties.kcFormClass!}" action="${url.loginAction}"
                      method="post">
                    <#if supportPhone??>
                        <div class="${properties.kcFormClass!}">
                            <div class="alert-error ${properties.kcAlertClass!} pf-m-danger" v-show="errorMessage">
                                <div class="pf-c-alert__icon">
                                    <span class="${properties.kcFeedbackErrorIcon!}"></span>
                                </div>

                                <span class="${properties.kcAlertTitleClass!}">{{ errorMessage }}</span>
                            </div>


                            <div class="${properties.kcFormGroupClass!}">
                                <div class="${properties.kcLabelWrapperClass!}">
                                    <ul class="nav nav-pills nav-justified">
                                        <li role="presentation" v-bind:class="{ active: !phoneActivated }"
                                            v-on:click="phoneActivated = false"><a
                                                    href="#">${msg("usernameOrEmail")}</a>
                                        </li>
                                        <li role="presentation" v-bind:class="{ active: phoneActivated }"
                                            v-on:click="phoneActivated = true"><a href="#">${msg("phoneNumber")}</a>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>

                        <input type="hidden" id="phoneActivated" name="phoneActivated" v-model="phoneActivated">
                    </#if>

                    <div <#if supportPhone??> v-if="!phoneActivated" </#if> >
                        <div class="${properties.kcFormGroupClass!}">
                            <div class="${properties.kcLabelWrapperClass!}">
                                <label for="username"
                                       class="${properties.kcLabelClass!}">${msg("usernameOrEmail")}</label>
                            </div>
                            <div class="${properties.kcInputWrapperClass!}">
                                <input type="text" id="username" name="username" class="${properties.kcInputClass!}"
                                       autofocus
                                       value="${(auth.attemptedUsername!'')}"
                                       aria-invalid="<#if messagesPerField.existsError('username')>true</#if>"/>
                                <#if messagesPerField.existsError('username')>
                                    <span id="input-error-username" class="${properties.kcInputErrorMessageClass!}"
                                          aria-live="polite">
                                    ${kcSanitize(messagesPerField.get('username'))?no_esc}
                        </span>
                                </#if>
                            </div>
                        </div>
                    </div>

                    <#if supportPhone??>
                        <div v-if="phoneActivated">
                            <div class="${properties.kcFormGroupClass!}">
                                <div class="${properties.kcLabelWrapperClass!}">

                                <label for="phoneNumber"
                                       class="${properties.kcLabelClass!}">${msg("phoneNumber")}</label>
                                </div>
                                <div class="${properties.kcInputWrapperClass!}">
                                <input type="text" id="phoneNumber" name="phoneNumber" v-model="phoneNumber"
                                       aria-invalid="<#if messagesPerField.existsError('code','phoneNumber')>true</#if>"
                                       class="${properties.kcInputClass!}" autofocus/>

                                <#if messagesPerField.existsError('code','phoneNumber')>
                                    <span id="input-error" class="${properties.kcInputErrorMessageClass!}"
                                          aria-live="polite">
                                    ${kcSanitize(messagesPerField.getFirstError('phoneNumber','code'))?no_esc}
                                    </span>
                                </#if>
                                </div>
                            </div>

                            <div class="${properties.kcFormGroupClass!} row">
                                <div class="${properties.kcLabelWrapperClass!}" style="padding: 0">
                                    <label for="code"
                                           class="${properties.kcLabelClass!}">${msg("verificationCode")}</label>
                                </div>
                                <div class="col-xs-8" style="padding: 0 5px 0 0">
                                    <input type="text" id="code" name="code"
                                           aria-invalid="<#if messagesPerField.existsError('code','phoneNumber')>true</#if>"
                                           class="${properties.kcInputClass!}" autocomplete="off"/>
                                </div>
                                <div class="col-xs-4" style="padding: 0 0 0 5px">
                                    <input tabindex="0" style="height: 36px"
                                           class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                           type="button" v-model="sendButtonText"
                                           :disabled='sendButtonText !== initSendButtonText'
                                           v-on:click="sendVerificationCode()"/>
                                </div>
                            </div>

                        </div>
                    </#if>

                    <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                        <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                            <div class="${properties.kcFormOptionsWrapperClass!}">
                                <span><a href="${url.loginUrl}">${kcSanitize(msg("backToLogin"))?no_esc}</a></span>
                            </div>
                        </div>

                        <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                            <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                   type="submit" value="${msg("doSubmit")}"/>
                        </div>
                    </div>

                </form>
            </div>
        </div>


        <#if supportPhone??>
            <script type="text/javascript">

                function req(phoneNumber) {
                    const params = {params: {phoneNumber}}
                    axios.get(window.location.origin + '/realms/${realm.name}/sms/reset-code', params)
                        .then(res => app.disableSend(res.data.expires_in))
                        .catch(e => app.errorMessage = e.response.data.error);
                }

                var app = new Vue({
                    el: '#vue-app',
                    data: {
                        errorMessage: '',
                        freezeSendCodeSeconds: 0,
                        phoneActivated: <#if attemptedPhoneActivated??>true<#else>false</#if>,
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

                            const phoneNumber = document.getElementById('phoneNumber').value.trim();
                            if (!phoneNumber) {
                                this.errorMessage = '${msg("requiredPhoneNumber")}';
                                document.getElementById('phoneNumber').focus();
                                return;
                            }
                            if (this.sendButtonText !== this.initSendButtonText) {
                                return;
                            }
                            req(phoneNumber);

                        }
                    }
                });
            </script>
        </#if>

    <#elseif section = "info" >
        <#if realm.duplicateEmailsAllowed>
            ${msg("emailInstructionUsername")}
        <#else>
            ${msg("emailInstruction")}
        </#if>
        <#if supportPhone??>
            ${msg("phoneInstruction")}
        </#if>
    </#if>
</@layout.registrationLayout>
